// models/ConversationModels.kt
package com.example.dam.models

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ MODÈLES POUR LE CHAT PERSONNEL (1-1)
 * Complètement séparé des modèles de chat de groupe
 */

// ==================== USER ====================
data class ConversationUser(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatar") val avatar: String?
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: "Utilisateur"

    companion object {
        fun fromJson(json: JSONObject): ConversationUser {
            // ✅ CORRECTION: Le backend envoie "name" et non "firstName"/"lastName"
            return ConversationUser(
                id = json.getString("_id"),
                name = json.optString("name").takeIf { it.isNotEmpty() },
                email = json.optString("email").takeIf { it.isNotEmpty() },
                avatar = json.optString("avatar").takeIf { it.isNotEmpty() }
            )
        }
    }
}

// ==================== MESSAGE TYPES ====================
enum class DirectMessageType {
    @SerializedName("text") TEXT,
    @SerializedName("image") IMAGE,
    @SerializedName("video") VIDEO,
    @SerializedName("audio") AUDIO,
    @SerializedName("file") FILE,
    @SerializedName("location") LOCATION;

    override fun toString(): String = name.lowercase()

    companion object {
        fun fromString(type: String): DirectMessageType {
            return when (type.lowercase()) {
                "text" -> TEXT
                "image" -> IMAGE
                "video" -> VIDEO
                "audio" -> AUDIO
                "file" -> FILE
                "location" -> LOCATION
                else -> TEXT
            }
        }
    }
}

enum class DirectMessageStatus {
    @SerializedName("sent") SENT,
    @SerializedName("delivered") DELIVERED,
    @SerializedName("read") READ,
    @SerializedName("failed") FAILED;

    override fun toString(): String = name.lowercase()

    companion object {
        fun fromString(status: String): DirectMessageStatus {
            return when (status.lowercase()) {
                "sent" -> SENT
                "delivered" -> DELIVERED
                "read" -> READ
                "failed" -> FAILED
                else -> SENT
            }
        }
    }
}

// ==================== LOCATION ====================
data class MessageLocation(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String?
) {
    companion object {
        fun fromJson(json: JSONObject): MessageLocation {
            return MessageLocation(
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                address = json.optString("address").takeIf { it.isNotEmpty() }
            )
        }
    }
}

// ==================== DIRECT MESSAGE ====================
data class DirectMessage(
    @SerializedName("_id") val id: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("senderId") val senderId: ConversationUser?,
    @SerializedName("recipientId") val recipientId: String,
    @SerializedName("type") val type: DirectMessageType,
    @SerializedName("content") val content: String?,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("mediaDuration") val mediaDuration: Int? = null,
    @SerializedName("fileSize") val fileSize: Long? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("mimeType") val mimeType: String? = null,
    @SerializedName("location") val location: MessageLocation? = null,
    @SerializedName("replyTo") val replyTo: String? = null,
    @SerializedName("tempId") val tempId: String? = null,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("readAt") val readAt: String? = null,
    @SerializedName("status") val status: DirectMessageStatus,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) {
    companion object {
        fun fromJson(json: JSONObject): DirectMessage {
            // ✅ senderId peut être un objet populé ou un string ID
            val sender = when {
                json.has("senderId") && !json.isNull("senderId") -> {
                    val senderValue = json.get("senderId")
                    when (senderValue) {
                        is JSONObject -> ConversationUser.fromJson(senderValue)
                        is String -> ConversationUser(senderValue, null, null, null)
                        else -> null
                    }
                }
                else -> null
            }

            val typeString = json.optString("type", "text")
            val type = DirectMessageType.fromString(typeString)

            val statusString = json.optString("status", "sent")
            val status = DirectMessageStatus.fromString(statusString)

            val locationJson = json.optJSONObject("location")
            val location = locationJson?.let { MessageLocation.fromJson(it) }

            // ✅ recipientId peut être un objet ou un string
            val recipientId = when {
                json.has("recipientId") && !json.isNull("recipientId") -> {
                    val recipientValue = json.get("recipientId")
                    when (recipientValue) {
                        is JSONObject -> recipientValue.optString("_id", "")
                        is String -> recipientValue
                        else -> ""
                    }
                }
                else -> ""
            }

            // ✅ conversationId peut être un objet ou un string
            val conversationId = when {
                json.has("conversationId") && !json.isNull("conversationId") -> {
                    val convValue = json.get("conversationId")
                    when (convValue) {
                        is JSONObject -> convValue.optString("_id", "")
                        is String -> convValue
                        else -> ""
                    }
                }
                else -> ""
            }

            return DirectMessage(
                id = json.optString("_id", ""),
                conversationId = conversationId,
                senderId = sender,
                recipientId = recipientId,
                type = type,
                content = json.optString("content").takeIf { it.isNotEmpty() },
                mediaUrl = json.optString("mediaUrl").takeIf { it.isNotEmpty() },
                thumbnailUrl = json.optString("thumbnailUrl").takeIf { it.isNotEmpty() },
                mediaDuration = json.optInt("mediaDuration", -1).takeIf { it > 0 },
                fileSize = json.optLong("fileSize", -1).takeIf { it > 0 },
                fileName = json.optString("fileName").takeIf { it.isNotEmpty() },
                mimeType = json.optString("mimeType").takeIf { it.isNotEmpty() },
                location = location,
                replyTo = json.optString("replyTo").takeIf { it.isNotEmpty() },
                tempId = json.optString("tempId").takeIf { it.isNotEmpty() },
                isRead = json.optBoolean("isRead", false),
                readAt = json.optString("readAt").takeIf { it.isNotEmpty() },
                status = status,
                createdAt = json.optString("createdAt", ""),
                updatedAt = json.optString("updatedAt", "")
            )
        }
    }
}

// ==================== CONVERSATION ====================
data class Conversation(
    @SerializedName("_id") val id: String,
    @SerializedName("participants") val participants: List<ConversationUser>,
    @SerializedName("lastMessage") val lastMessage: DirectMessage?,
    @SerializedName("unreadCount") val unreadCount: Map<String, Int>,
    @SerializedName("mutedBy") val mutedBy: Map<String, Boolean>? = null,
    @SerializedName("deletedBy") val deletedBy: Map<String, Boolean>? = null,
    @SerializedName("lastReadAt") val lastReadAt: Map<String, String>? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) {
    /**
     * Get the other participant (not current user)
     */
    fun getOtherParticipant(currentUserId: String): ConversationUser? {
        return participants.firstOrNull { it.id != currentUserId }
            ?: participants.firstOrNull() // Si on a qu'un participant (format simplifié), retourner celui-là
    }

    /**
     * Get unread count for current user
     */
    fun getUnreadCountForUser(currentUserId: String): Int {
        // Essayer d'abord avec l'userId réel
        return unreadCount[currentUserId]
            // Sinon essayer avec "current" (format simplifié)
            ?: unreadCount["current"]
            ?: 0
    }

    /**
     * Check if conversation is muted for current user
     */
    fun isMutedForUser(currentUserId: String): Boolean {
        // Essayer d'abord avec l'userId réel
        return mutedBy?.get(currentUserId)
            // Sinon essayer avec "current" (format simplifié)
            ?: mutedBy?.get("current")
            ?: false
    }

    companion object {
        fun fromJson(json: JSONObject): Conversation {
            // ✅ CORRECTION: Le backend peut envoyer soit "participants" soit "otherUser"
            val participants = when {
                // Format complet avec participants array
                json.has("participants") -> {
                    val participantsArray = json.getJSONArray("participants")
                    (0 until participantsArray.length()).map { i ->
                        ConversationUser.fromJson(participantsArray.getJSONObject(i))
                    }
                }
                // Format simplifié avec otherUser uniquement (depuis conversationsList)
                json.has("otherUser") -> {
                    val otherUserJson = json.getJSONObject("otherUser")
                    listOf(ConversationUser.fromJson(otherUserJson))
                }
                else -> emptyList()
            }

            // ✅ CORRECTION: unreadCount peut être un Int ou un Object
            val unreadCount = mutableMapOf<String, Int>()

            if (json.has("unreadCount")) {
                try {
                    // Essayer de lire comme un Int (format simplifié de conversationsList)
                    val simpleCount = json.getInt("unreadCount")
                    // On stocke avec une clé temporaire car on ne connaît pas l'userId ici
                    unreadCount["current"] = simpleCount
                } catch (e: Exception) {
                    // Si ce n'est pas un Int, essayer comme JSONObject
                    try {
                        val unreadCountJson = json.getJSONObject("unreadCount")
                        unreadCountJson.keys().forEach { key ->
                            val keyString = key as String
                            unreadCount[keyString] = unreadCountJson.getInt(keyString)
                        }
                    } catch (e2: Exception) {
                        // Ignorer si le parsing échoue
                    }
                }
            }

            val lastMessageJson = json.optJSONObject("lastMessage")
            val lastMessage = lastMessageJson?.let { DirectMessage.fromJson(it) }

            val mutedByJson = json.optJSONObject("mutedBy")
            val mutedBy = mutableMapOf<String, Boolean>()
            mutedByJson?.keys()?.forEach { key ->
                val keyString = key as String
                mutedBy[keyString] = mutedByJson.getBoolean(keyString)
            }

            // ✅ Gérer isMuted qui peut être un Boolean direct dans conversationsList
            if (json.has("isMuted") && !json.has("mutedBy")) {
                val isMuted = json.getBoolean("isMuted")
                mutedBy["current"] = isMuted
            }

            // ✅ Gérer conversationId ou _id
            val id = json.optString("conversationId", json.optString("_id", ""))

            return Conversation(
                id = id,
                participants = participants,
                lastMessage = lastMessage,
                unreadCount = unreadCount,
                mutedBy = mutedBy,
                createdAt = json.optString("createdAt", ""),
                updatedAt = json.optString("updatedAt", "")
            )
        }
    }
}

// ==================== UI MODEL ====================
data class ConversationUI(
    val id: String,
    val otherUser: ConversationUser,
    val lastMessage: String,
    val lastMessageTime: String,
    val lastMessageTimestamp: String,
    val unreadCount: Int,
    val isMuted: Boolean
) {
    companion object {
        fun fromConversation(conversation: Conversation, currentUserId: String): ConversationUI {
            val otherUser = conversation.getOtherParticipant(currentUserId)
                ?: ConversationUser("", "Utilisateur", null, null)

            val lastMessageContent = conversation.lastMessage?.let { msg ->
                when (msg.type) {
                    DirectMessageType.TEXT -> msg.content ?: "Message"
                    DirectMessageType.IMAGE -> "📷 Photo"
                    DirectMessageType.VIDEO -> "🎥 Vidéo"
                    DirectMessageType.AUDIO -> "🎤 Message vocal"
                    DirectMessageType.FILE -> "📎 Fichier"
                    DirectMessageType.LOCATION -> "📍 Position"
                }
            } ?: "Aucun message"

            val time = conversation.lastMessage?.createdAt?.let {
                formatTime(it)
            } ?: ""

            return ConversationUI(
                id = conversation.id,
                otherUser = otherUser,
                lastMessage = lastMessageContent,
                lastMessageTime = time,
                lastMessageTimestamp = conversation.lastMessage?.createdAt ?: "",
                unreadCount = conversation.getUnreadCountForUser(currentUserId),
                isMuted = conversation.isMutedForUser(currentUserId)
            )
        }

        private fun formatTime(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val messageDate = inputFormat.parse(timestamp)

                if (messageDate != null) {
                    val now = System.currentTimeMillis()
                    val messageTime = messageDate.time
                    val diffInMillis = now - messageTime

                    val seconds = diffInMillis / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24

                    when {
                        seconds < 60 -> "maintenant"
                        minutes < 2 -> "1 min"
                        minutes < 60 -> "$minutes mins"
                        hours < 2 -> "1 hour"
                        hours < 24 -> "$hours hours"
                        days < 2 -> "hier"
                        days < 7 -> "$days jours"
                        else -> {
                            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                            outputFormat.format(messageDate)
                        }
                    }
                } else {
                    "maintenant"
                }
            } catch (e: Exception) {
                "maintenant"
            }
        }
    }
}

// ==================== DIRECT MESSAGE UI ====================
data class DirectMessageUI(
    val id: String,
    val authorName: String,
    val authorAvatar: String?,
    val senderId: String,
    val content: String?,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val audioDuration: Int?,
    val fileName: String?,
    val fileSize: Long?,
    val mimeType: String?,
    val location: MessageLocation?,
    val time: String,
    val timestamp: String,
    val isMe: Boolean,
    val status: DirectMessageStatus,
    val type: DirectMessageType,
    val isRead: Boolean
) {
    companion object {
        fun fromDirectMessage(message: DirectMessage, currentUserId: String): DirectMessageUI {
            val author = message.senderId?.displayName ?: "Utilisateur"
            val isMe = message.senderId?.id == currentUserId

            val time = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(message.createdAt)
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }

            return DirectMessageUI(
                id = message.id,
                authorName = author,
                authorAvatar = message.senderId?.avatar,
                senderId = message.senderId?.id ?: "",
                content = message.content,
                mediaUrl = message.mediaUrl,
                thumbnailUrl = message.thumbnailUrl,
                audioDuration = message.mediaDuration,
                fileName = message.fileName,
                fileSize = message.fileSize,
                mimeType = message.mimeType,
                location = message.location,
                time = time,
                timestamp = message.createdAt,
                isMe = isMe,
                status = message.status,
                type = message.type,
                isRead = message.isRead
            )
        }
    }
}

