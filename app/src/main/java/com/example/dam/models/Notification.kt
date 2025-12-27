package com.example.dam.models

import com.google.gson.annotations.SerializedName

/**
 * Modèle de données pour les notifications reçues du backend
 */
data class Notification(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("type")
    val type: NotificationType,

    @SerializedName("data")
    val data: NotificationData,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("readAt")
    val readAt: String?
)

/**
 * Données additionnelles de la notification pour le deep linking
 */
data class NotificationData(
    @SerializedName("type")
    val type: String,

    // For new_publication
    @SerializedName("publicationId")
    val publicationId: String? = null,

    @SerializedName("authorId")
    val authorId: String? = null,

    @SerializedName("authorName")
    val authorName: String? = null,

    // For chat_message
    @SerializedName("messageId")
    val messageId: String? = null,

    @SerializedName("chatId")
    val chatId: String? = null,

    @SerializedName("sortieId")
    val sortieId: String? = null,

    @SerializedName("senderId")
    val senderId: String? = null,

    @SerializedName("senderName")
    val senderName: String? = null,

    @SerializedName("chatName")
    val chatName: String? = null,

    // For new_sortie
    @SerializedName("sortieType")
    val sortieType: String? = null,

    @SerializedName("creatorId")
    val creatorId: String? = null,

    @SerializedName("creatorName")
    val creatorName: String? = null,

    // For participation
    @SerializedName("sortieTitle")
    val sortieTitle: String? = null,

    @SerializedName("participationId")
    val participationId: String? = null,

    // For test
    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * Types de notifications supportés
 */
enum class NotificationType {
    @SerializedName("new_publication")
    NEW_PUBLICATION,

    @SerializedName("chat_message")
    CHAT_MESSAGE,

    @SerializedName("new_sortie")
    NEW_SORTIE,

    @SerializedName("participation_accepted")
    PARTICIPATION_ACCEPTED,

    @SerializedName("participation_rejected")
    PARTICIPATION_REJECTED,

    @SerializedName("test")
    TEST
}

/**
 * Réponse du endpoint unread count
 */
data class UnreadCountResponse(
    @SerializedName("count")
    val count: Int
)

/**
 * Réponse du endpoint mark as read
 */
data class MarkAsReadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

