package com.example.dam.services

import android.content.Context
import android.util.Log
import com.example.dam.repository.NotificationRepository
import com.example.dam.utils.NotificationHelper
import com.example.dam.utils.Result
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.*

/**
 * Service de polling léger avec Coroutines pour un intervalle < 15 minutes
 * Alternative à WorkManager qui a une limite de 15 minutes minimum
 *
 * Utilise un Job Kotlin coroutine pour poller toutes les 10-30 secondes
 */
object NotificationPollingService {

    private const val TAG = "NotificationPolling"
    private var pollingJob: Job? = null
    private val repository = NotificationRepository()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ✅ Track notifications already shown to avoid repeating
    private val shownNotificationIds = mutableSetOf<String>()

    /**
     * Démarre le polling avec un intervalle personnalisé
     * @param context Context de l'application
     * @param intervalSeconds Intervalle en secondes (par défaut 15s)
     */
    fun startPolling(context: Context, intervalSeconds: Long = 15) {
        // Si déjà en cours, ne pas redémarrer
        if (pollingJob?.isActive == true) {
            Log.d(TAG, "⚠️ Polling already active")
            return
        }

        Log.d(TAG, "🚀 Starting notification polling (every ${intervalSeconds}s)")

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    pollNotifications(context)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Polling error", e)
                }

                // Attendre avant le prochain poll
                delay(intervalSeconds * 1000)
            }
        }
    }

    /**
     * Arrête le polling
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        // ✅ Clear shown notifications when stopping (fresh start next time)
        shownNotificationIds.clear()
        Log.d(TAG, "🛑 Polling stopped and notification history cleared")
    }

    /**
     * Vérifie si le polling est actif
     */
    fun isPollingActive(): Boolean {
        return pollingJob?.isActive == true
    }

    /**
     * Effectue un poll unique des notifications
     */
    private suspend fun pollNotifications(context: Context) {
        // Récupérer le token
        val token = UserPreferences.getToken(context)
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No token found, skipping poll")
            stopPolling() // Arrêter si pas de token
            return
        }

        Log.d(TAG, "📡 Polling notifications...")

        // Récupérer les notifications
        when (val result = repository.getNotifications(token)) {
            is Result.Success -> {
                val notifications = result.data

                if (notifications.isEmpty()) {
                    Log.d(TAG, "✅ No new notifications")
                } else {
                    // ✅ Filter out notifications we've already shown
                    val newNotifications = notifications.filter { notif ->
                        !shownNotificationIds.contains(notif.id)
                    }

                    if (newNotifications.isEmpty()) {
                        Log.d(TAG, "📭 ${notifications.size} notification(s) but all already shown")
                    } else {
                        Log.d(TAG, "📬 ${newNotifications.size} NEW notification(s) to display")

                        // Afficher chaque notification sur le thread principal
                        withContext(Dispatchers.Main) {
                            newNotifications.forEach { notification ->
                                // ✅ Only show if type is not null
                                if (notification.type != null) {
                                    NotificationHelper.showNotification(context, notification)
                                    // ✅ Mark as shown to prevent repetition
                                    shownNotificationIds.add(notification.id)
                                    Log.d(TAG, "   ✅ Shown: ${notification.title} (ID: ${notification.id})")
                                } else {
                                    Log.w(TAG, "   ⚠️ Skipped notification with null type: ${notification.id}")
                                }
                            }
                        }
                    }

                    // ❌ DON'T mark as read automatically!
                    // Notifications should only be marked as read when user clicks on them
                    // Otherwise, they won't appear in the NotificationScreen (which loads unreadOnly=true)
                }
            }
            is Result.Error -> {
                Log.e(TAG, "❌ Failed to fetch notifications: ${result.message}")

                // Si token expiré, arrêter le polling
                if (result.message.contains("401") || result.message.contains("Token expiré")) {
                    Log.w(TAG, "🔐 Token expired, stopping polling")
                    stopPolling()
                }
            }
            is Result.Failure -> {
                Log.e(TAG, "❌ Exception while fetching notifications: ${result.message.message}", result.message)
            }
            is Result.Loading -> {
                // État de chargement, ne rien faire
                Log.d(TAG, "⏳ Loading notifications...")
            }
        }
    }

    /**
     * Effectue un poll immédiat (one-shot)
     * Utile pour rafraîchir manuellement
     */
    suspend fun pollNow(context: Context) {
        pollNotifications(context)
    }
}

