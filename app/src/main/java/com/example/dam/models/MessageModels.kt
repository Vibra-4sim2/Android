// models/MessageModels.kt
package com.example.dam.models

import com.google.gson.annotations.SerializedName

/**
 * Types de messages supportés
 */
enum class MessageType {
    @SerializedName("text")
    TEXT,

    @SerializedName("image")
    IMAGE,

    @SerializedName("video")
    VIDEO,

    @SerializedName("audio")
    AUDIO,

    @SerializedName("location")
    LOCATION,

    @SerializedName("file")
    FILE
}

/**
 * Réponse contenant les messages paginés
 */
data class MessagesResponse(
    @SerializedName("messages") val messages: List<MessageResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("hasMore") val hasMore: Boolean
)

/**
 * Message reçu du backend
 */
data class MessageResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("senderId") val senderId: SenderInfo,
    @SerializedName("type") val type: MessageType,
    @SerializedName("content") val content: String?,
    @SerializedName("mediaUrl") val mediaUrl: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("mediaDuration") val mediaDuration: Double?,
    @SerializedName("fileSize") val fileSize: Long?,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("mimeType") val mimeType: String?,
    @SerializedName("location") val location: LocationData?,
    @SerializedName("readBy") val readBy: List<String>,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?
)

/**
 * Information de l'expéditeur
 */
data class SenderInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?
)

/**
 * Données de localisation
 */
data class LocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String?,
    @SerializedName("name") val name: String?
)

/**
 * DTO pour créer un message
 */
data class CreateMessageDto(
    @SerializedName("type") val type: MessageType,
    @SerializedName("content") val content: String? = null,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("mediaDuration") val mediaDuration: Double? = null,
    @SerializedName("fileSize") val fileSize: Long? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("mimeType") val mimeType: String? = null,
    @SerializedName("location") val location: LocationData? = null,
    @SerializedName("replyTo") val replyTo: String? = null
)

/**
 * Réponse de l'upload de média
 */
data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("url") val url: String,
    @SerializedName("publicId") val publicId: String,
    @SerializedName("duration") val duration: Double?,
    @SerializedName("format") val format: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("size") val size: Long,
    @SerializedName("originalName") val originalName: String
)

/**
 * Modèle UI pour afficher un message
 */
data class MessageUI(
    val id: String,
    val author: String,
    val authorAvatar: String?,
    val content: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val location: LocationData?,
    val time: String,
    val isMe: Boolean,
    val status: MessageStatus,
    val type: MessageType
)

/**
 * Statut d'un message
 */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Extension pour convertir MessageResponse en MessageUI
 */
fun MessageResponse.toMessageUI(currentUserId: String): MessageUI {
    val isMe = senderId.id == currentUserId
    val authorName = if (isMe) {
        "Moi"
    } else {
        "${senderId.firstName ?: ""} ${senderId.lastName ?: ""}".trim()
            .ifEmpty { senderId.email }
    }

    // Formater le temps
    val time = formatMessageTime(createdAt)

    // Déterminer le statut
    val status = when {
        isDeleted -> MessageStatus.FAILED
        readBy.contains(currentUserId) -> MessageStatus.READ
        readBy.isNotEmpty() -> MessageStatus.DELIVERED
        else -> MessageStatus.SENT
    }

    return MessageUI(
        id = id,
        author = authorName,
        authorAvatar = senderId.avatar,
        content = content,
        imageUrl = if (type == MessageType.IMAGE) mediaUrl else null,
        videoUrl = if (type == MessageType.VIDEO) mediaUrl else null,
        audioUrl = if (type == MessageType.AUDIO) mediaUrl else null,
        location = location,
        time = time,
        isMe = isMe,
        status = status,
        type = type
    )
}

/**
 * Formater l'heure du message
 */
private fun formatMessageTime(timestamp: String): String {
    // TODO: Implémenter le formatage correct avec SimpleDateFormat
    // Pour l'instant retourne une valeur simple
    return try {
        // Extraire l'heure de "2025-11-23T15:50:52.870Z"
        val time = timestamp.substring(11, 16) // "15:50"
        time
    } catch (e: Exception) {
        "..."
    }
}