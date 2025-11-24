package com.example.dam.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    FILE,

    @SerializedName("system")
    SYSTEM;

    // ✅ AJOUTÉ: Convertir vers lowercase pour l'envoi au backend
    fun toLowerCaseString(): String = this.name.lowercase()
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
 * ✅ CORRIGÉ: senderId peut être un objet OU une string OU null
 */
data class MessageResponse(
    @SerializedName("_id") val _id: String,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("sortieId") val sortieId: String,

    // ✅ CORRIGÉ: senderId peut être un objet (populé) ou null (messages système)
    @SerializedName("senderId") val senderId: SenderInfo? = null,

    @SerializedName("sender") val sender: SenderInfo? = null,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String?,
    @SerializedName("mediaUrl") val mediaUrl: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("videoUrl") val videoUrl: String?,
    @SerializedName("audioUrl") val audioUrl: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("mediaDuration") val mediaDuration: Double?,
    @SerializedName("fileSize") val fileSize: Long?,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("mimeType") val mimeType: String?,
    @SerializedName("location") val location: LocationData?,
    @SerializedName("readBy") val readBy: List<String>,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("status") val status: String?,
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
    val timestamp: String, // ✅ AJOUTÉ: Timestamp original pour tri chronologique
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
 * ✅ CORRIGÉ: Conversion sécurisée d'un message serveur vers MessageUI
 */
fun MessageResponse.toMessageUI(currentUserId: String?): MessageUI {
    // ✅ Gérer les deux cas: senderId peut être un objet OU null
    val senderInfo = this.senderId ?: this.sender
    val actualSenderId = senderInfo?.id

    // Déterminer le nom de l'auteur
    val author = when {
        senderInfo != null -> {
            listOfNotNull(
                senderInfo.firstName?.trim(),
                senderInfo.lastName?.trim()
            ).joinToString(" ").takeIf { it.isNotBlank() } ?: "Utilisateur"
        }
        this.type.lowercase() == "system" -> "Système"
        else -> "Utilisateur"
    }

    val avatar = senderInfo?.avatar
    val isMe = currentUserId != null && actualSenderId != null && actualSenderId == currentUserId

    // Parser le type de message
    val messageType = when (this.type.lowercase()) {
        "image" -> MessageType.IMAGE
        "system" -> MessageType.SYSTEM
        "audio" -> MessageType.AUDIO
        "video" -> MessageType.VIDEO
        "location" -> MessageType.LOCATION
        "file" -> MessageType.FILE
        else -> MessageType.TEXT
    }

    // Parser le statut du message
    val messageStatus = when (this.status?.lowercase()) {
        "sending" -> MessageStatus.SENDING
        "sent" -> MessageStatus.SENT
        "delivered" -> MessageStatus.DELIVERED
        "read" -> MessageStatus.READ
        "failed" -> MessageStatus.FAILED
        else -> if (messageType == MessageType.SYSTEM) MessageStatus.READ else MessageStatus.SENT
    }

    val time = formatMessageTime(this.createdAt)

    return MessageUI(
        id = this._id,
        author = author,
        authorAvatar = avatar,
        content = this.content,
        imageUrl = this.imageUrl ?: this.mediaUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        location = this.location,
        time = time,
        timestamp = this.createdAt, // ✅ AJOUTÉ: Timestamp original pour tri
        isMe = isMe,
        status = messageStatus,
        type = messageType
    )
}

/**
 * Formater l'heure du message
 */
private fun formatMessageTime(timestamp: String): String {
    return try {
        // Format ISO 8601: "2025-11-23T15:50:52.870Z"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp.substring(11, 16)
    } catch (e: Exception) {
        try {
            // Fallback: extraire directement l'heure
            timestamp.substring(11, 16)
        } catch (e: Exception) {
            "..."
        }
    }
}