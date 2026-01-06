package com.example.dam.repository

import android.util.Log
import com.example.dam.models.MarkAsReadResponse
import com.example.dam.models.Notification
import com.example.dam.models.UnreadCountResponse
import com.example.dam.remote.RetrofitInstance
import com.example.dam.utils.Result

/**
 * Repository pour gérer les notifications
 */
class NotificationRepository {

    private val api = RetrofitInstance.notificationApi
    private val TAG = "NotificationRepository"

    /**
     * Récupère les notifications non lues
     * @param token Token JWT (sans le préfixe "Bearer")
     * @param unreadOnly Filtrer uniquement les non lues
     * @param limit Nombre maximum de notifications
     */
    suspend fun getNotifications(
        token: String,
        unreadOnly: Boolean = true,
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<Notification>> {
        return try {
            Log.d(TAG, "📡 Polling notifications... (unreadOnly=$unreadOnly, limit=$limit)")

            val response = api.getNotifications(
                authorization = "Bearer $token",
                unreadOnly = unreadOnly,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                val notifications = response.body() ?: emptyList()
                Log.d(TAG, "✅ Received ${notifications.size} notifications")

                notifications.forEachIndexed { index, notif ->
                    Log.d(TAG, "   [$index] ${notif.type} - ${notif.title}")
                }

                Result.Success(notifications)
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ Failed to fetch notifications: $errorMsg")

                if (response.code() == 401) {
                    Result.Error("Token expiré, veuillez vous reconnecter")
                } else {
                    Result.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching notifications", e)
            Result.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    /**
     * Marque une notification comme lue
     * @param token Token JWT (sans le préfixe "Bearer")
     * @param notificationId ID de la notification
     */
    suspend fun markAsRead(
        token: String,
        notificationId: String
    ): Result<MarkAsReadResponse> {
        return try {
            Log.d(TAG, "📝 Marking notification as read: $notificationId")

            val response = api.markAsRead(
                authorization = "Bearer $token",
                notificationId = notificationId
            )

            if (response.isSuccessful) {
                val result = response.body()!!
                Log.d(TAG, "✅ ${result.message}")
                Result.Success(result)
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ Failed to mark as read: $errorMsg")
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while marking as read", e)
            Result.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }

    /**
     * Récupère le nombre de notifications non lues
     * @param token Token JWT (sans le préfixe "Bearer")
     */
    suspend fun getUnreadCount(token: String): Result<Int> {
        return try {
            Log.d(TAG, "🔢 Fetching unread count...")

            val response = api.getUnreadCount(
                authorization = "Bearer $token"
            )

            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                Log.d(TAG, "✅ Unread count: $count")
                Result.Success(count)
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "❌ Failed to fetch unread count: $errorMsg")
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching unread count", e)
            Result.Error("Erreur réseau: ${e.localizedMessage}")
        }
    }
}

