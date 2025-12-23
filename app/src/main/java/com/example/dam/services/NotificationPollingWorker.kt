package com.example.dam.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.dam.repository.NotificationRepository
import com.example.dam.utils.NotificationHelper
import com.example.dam.utils.Result
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker pour le polling des notifications en arrière-plan
 * Note: WorkManager a une limite de 15 minutes minimum pour les tâches périodiques
 * Pour un polling plus fréquent (10-30s), utilisez un foreground service
 */
class NotificationPollingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = NotificationRepository()
    private val TAG = "NotificationPolling"

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Starting notification polling...")

                // Récupérer le token
                val token = UserPreferences.getToken(applicationContext)
                if (token.isNullOrEmpty()) {
                    Log.w(TAG, "⚠️ No token found, skipping polling")
                    return@withContext Result.success()
                }

                // Récupérer les notifications
                when (val result = repository.getNotifications(token)) {
                    is com.example.dam.utils.Result.Success -> {
                        val notifications = result.data

                        if (notifications.isEmpty()) {
                            Log.d(TAG, "✅ No new notifications")
                        } else {
                            Log.d(TAG, "📬 ${notifications.size} new notification(s)")

                            // Afficher chaque notification
                            notifications.forEach { notification ->
                                NotificationHelper.showNotification(applicationContext, notification)

                                // Marquer comme lue
                                repository.markAsRead(token, notification.id)
                            }
                        }

                        Result.success()
                    }
                    is com.example.dam.utils.Result.Error -> {
                        Log.e(TAG, "❌ Polling failed: ${result.message}")

                        // Si le token est expiré (401), arrêter le polling
                        if (result.message.contains("401") || result.message.contains("Token expiré")) {
                            Log.w(TAG, "🔐 Token expired, stopping polling")
                            cancelPolling(applicationContext)
                        }

                        Result.retry()
                    }
                    is com.example.dam.utils.Result.Failure -> {
                        Log.e(TAG, "❌ Worker exception: ${result.message.message}", result.message)
                        Result.failure()
                    }
                    is com.example.dam.utils.Result.Loading -> {
                        Log.d(TAG, "⏳ Loading notifications...")
                        Result.success()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Worker exception", e)
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "notification_polling"

        /**
         * Démarre le polling périodique des notifications
         * @param context Context de l'application
         * @param intervalMinutes Intervalle en minutes (minimum 15 pour WorkManager)
         */
        fun startPolling(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Nécessite une connexion internet
                .build()

            val pollingRequest = PeriodicWorkRequestBuilder<NotificationPollingWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Ne pas redémarrer si déjà en cours
                pollingRequest
            )

            Log.d("NotificationPolling", "✅ Polling started (every $intervalMinutes minutes)")
        }

        /**
         * Arrête le polling des notifications
         */
        fun cancelPolling(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d("NotificationPolling", "🛑 Polling cancelled")
        }

        /**
         * Vérifie si le polling est actif
         */
        fun isPollingActive(context: Context): Boolean {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            return workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
        }
    }
}

