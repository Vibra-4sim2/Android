package com.example.dam.remote

import com.example.dam.models.MarkAsReadResponse
import com.example.dam.models.Notification
import com.example.dam.models.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les notifications
 */
interface NotificationApiService {

    /**
     * Récupère les notifications (polling endpoint)
     * @param authorization Token JWT au format "Bearer <token>"
     * @param unreadOnly Filtrer uniquement les notifications non lues
     * @param limit Nombre maximum de notifications à retourner
     * @param offset Pagination offset
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String,
        @Query("unreadOnly") unreadOnly: Boolean = true,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<List<Notification>>

    /**
     * Marque une notification comme lue
     * @param authorization Token JWT au format "Bearer <token>"
     * @param notificationId ID de la notification
     */
    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Header("Authorization") authorization: String,
        @Path("id") notificationId: String
    ): Response<MarkAsReadResponse>

    /**
     * Récupère le nombre de notifications non lues
     * @param authorization Token JWT au format "Bearer <token>"
     */
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") authorization: String
    ): Response<UnreadCountResponse>
}

