// models/ChatModels.kt
package com.example.dam.models

import com.google.gson.annotations.SerializedName

/**
 * Représente un chat de groupe lié à une sortie
 */
data class ChatResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("members") val members: List<ChatMember>,
    @SerializedName("lastMessage") val lastMessage: LastMessage?,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar") val avatar: String?
)

/**
 * Informations d'un membre du chat
 */
data class ChatMember(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?
)

/**
 * Dernier message du chat (pour preview)
 */
data class LastMessage(
    @SerializedName("_id") val id: String,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("senderId") val senderId: String,
    @SerializedName("type") val type: String, // "text", "image", etc.
    @SerializedName("content") val content: String,
    @SerializedName("readBy") val readBy: List<String>,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("createdAt") val createdAt: String?
)

/**
 * Modèle pour afficher dans l'UI (conversion du ChatResponse)
 */
data class ChatGroupUI(
    val id: String,
    val name: String,
    val emoji: String,
    val participantsCount: Int,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val time: String,
    val timestamp: String?, // ✅ AJOUTÉ: Timestamp original pour recalcul en temps réel
    val unreadCount: Int,
    val sortieId: String,
    val memberAvatars: List<String>
)

/**
 * Extension pour convertir ChatResponse en ChatGroupUI
 */
fun ChatResponse.toChatGroupUI(currentUserId: String): ChatGroupUI {
    // Récupérer l'auteur du dernier message
    val lastMessageAuthor = if (lastMessage != null) {
        val sender = members.find { it.id == lastMessage.senderId }
        sender?.firstName ?: "Inconnu"
    } else {
        ""
    }

    // Formater le temps (simplifié - à adapter selon tes besoins)
    val time = lastMessage?.createdAt?.let { formatTime(it) } ?: ""

    // Compter les messages non lus (à implémenter selon ta logique)
    val unreadCount = 0 // TODO: Implémenter la logique de comptage

    // Emoji par défaut (peut être récupéré depuis la sortie)
    val emoji = "🚴"

    return ChatGroupUI(
        id = id,
        name = name ?: "Groupe sans nom",
        emoji = emoji,
        participantsCount = members.size,
        lastMessage = lastMessage?.content ?: "Aucun message",
        lastMessageAuthor = lastMessageAuthor,
        time = time,
        timestamp = lastMessage?.createdAt, // ✅ AJOUTÉ: Conserver le timestamp original
        unreadCount = unreadCount,
        sortieId = sortieId,
        memberAvatars = members.mapNotNull { it.avatar }
    )
}

/**
 * Fonction helper pour formater le temps écoulé depuis un timestamp ISO 8601
 * Retourne: "maintenant", "2 mins", "1 hour", "3 hours", "2 jours", etc.
 */
fun formatTime(timestamp: String): String {
    return try {
        // Parser le timestamp ISO 8601: "2025-11-23T15:50:52.870Z"
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val messageDate = inputFormat.parse(timestamp)

        if (messageDate != null) {
            val now = System.currentTimeMillis()
            val messageTime = messageDate.time
            val diffInMillis = now - messageTime

            // Calculer les différences en unités de temps
            val seconds = diffInMillis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            when {
                seconds < 60 -> "maintenant"
                minutes < 2 -> "1 min"
                minutes < 60 -> "$minutes mins"
                hours < 2 -> "1 hour"
                hours < 24 -> "$hours hours"
                days < 2 -> "hier"
                days < 7 -> "$days jours"
                weeks < 2 -> "1 semaine"
                weeks < 4 -> "$weeks semaines"
                months < 2 -> "1 mois"
                months < 12 -> "$months mois"
                years < 2 -> "1 an"
                else -> "$years ans"
            }
        } else {
            "maintenant"
        }
    } catch (e: Exception) {
        "maintenant"
    }
}