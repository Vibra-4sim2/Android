package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.Notification
import com.example.dam.repository.NotificationRepository
import com.example.dam.services.NotificationPollingService
import com.example.dam.utils.Result
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les notifications
 */
class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()
    private val TAG = "NotificationViewModel"

    // État des notifications
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Nombre de notifications non lues
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Message d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Démarre le service de polling
     * @param context Context de l'application
     * @param intervalSeconds Intervalle en secondes (défaut: 15s)
     */
    fun startPolling(context: Context, intervalSeconds: Long = 15) {
        Log.d(TAG, "🚀 Starting notification polling")
        NotificationPollingService.startPolling(context, intervalSeconds)
    }

    /**
     * Arrête le service de polling
     */
    fun stopPolling() {
        Log.d(TAG, "🛑 Stopping notification polling")
        NotificationPollingService.stopPolling()
    }

    /**
     * Vérifie si le polling est actif
     */
    fun isPollingActive(): Boolean {
        return NotificationPollingService.isPollingActive()
    }

    /**
     * Charge les notifications manuellement (pour affichage dans l'UI)
     */
    fun loadNotifications(context: Context, unreadOnly: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val token = UserPreferences.getToken(context)
            if (token.isNullOrEmpty()) {
                _errorMessage.value = "Non authentifié"
                _isLoading.value = false
                return@launch
            }

            when (val result = repository.getNotifications(token, unreadOnly)) {
                is Result.Success -> {
                    _notifications.value = result.data
                    Log.d(TAG, "✅ Loaded ${result.data.size} notifications")
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    Log.e(TAG, "❌ Failed to load notifications: ${result.message}")
                }
                is Result.Failure -> {
                    _errorMessage.value = "Erreur: ${result.message.message}"
                    Log.e(TAG, "❌ Exception: ${result.message.message}", result.message)
                }
                is Result.Loading -> {
                    // Déjà en chargement
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Charge le nombre de notifications non lues
     */
    fun loadUnreadCount(context: Context) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isNullOrEmpty()) return@launch

            when (val result = repository.getUnreadCount(token)) {
                is Result.Success -> {
                    _unreadCount.value = result.data
                    Log.d(TAG, "✅ Unread count: ${result.data}")
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Failed to load unread count: ${result.message}")
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Exception loading unread count: ${result.message.message}", result.message)
                }
                is Result.Loading -> {
                    // En chargement
                }
            }
        }
    }

    /**
     * Marque une notification comme lue (sans la retirer de la liste)
     */
    fun markAsRead(context: Context, notificationId: String) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isNullOrEmpty()) return@launch

            when (val result = repository.markAsRead(token, notificationId)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Notification marked as read")

                    // Mettre à jour la notification localement (sans la retirer)
                    _notifications.value = _notifications.value.map { notif ->
                        if (notif.id == notificationId) {
                            notif.copy(isRead = true)
                        } else {
                            notif
                        }
                    }

                    // Décrémenter le compteur
                    if (_unreadCount.value > 0) {
                        _unreadCount.value -= 1
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Failed to mark as read: ${result.message}")
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Exception marking as read: ${result.message.message}", result.message)
                }
                is Result.Loading -> {
                    // En chargement
                }
            }
        }
    }

    /**
     * Rafraîchit immédiatement les notifications (pull-to-refresh)
     */
    fun refreshNotifications(context: Context) {
        viewModelScope.launch {
            NotificationPollingService.pollNow(context)
            loadNotifications(context, unreadOnly = true)
            loadUnreadCount(context)
        }
    }

    /**
     * Retire une notification de la liste ET l'archive côté backend (bouton X)
     */
    fun removeNotificationFromList(context: Context, notificationId: String) {
        // Immediate UI update - remove from list first for instant feedback
        _notifications.value = _notifications.value.filter { it.id != notificationId }
        _unreadCount.value = _notifications.value.count { !it.isRead }

        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ No token found, notification removed locally only")
                return@launch
            }

            // Appeler markAsRead pour archiver côté backend (async, non-blocking)
            when (val result = repository.markAsRead(token, notificationId)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Notification archived on backend")
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Failed to archive notification on backend: ${result.message}")
                    // Already removed from UI, so user won't see it again
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Exception archiving notification: ${result.message.message}", result.message)
                    // Already removed from UI, graceful degradation
                }
                is Result.Loading -> {
                    // En chargement
                }
            }
        }
    }

    /**
     * Nettoie les ressources au destroy
     */
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
