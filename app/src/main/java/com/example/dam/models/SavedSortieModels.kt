package com.example.dam.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Entity for saved sorties in local database (Room)
 */
@Entity(tableName = "saved_sorties")
data class SavedSortieEntity(
    @PrimaryKey
    val sortieId: String,
    val userId: String,
    val savedAt: Long = System.currentTimeMillis(),
    val isSyncedWithServer: Boolean = false,
    // Store complete sortie data as JSON for offline access
    val sortieDataJson: String
)

/**
 * Request to save sortie on server
 */
data class SaveSortieRequest(
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("userId") val userId: String
)

/**
 * Response from server for saved sortie
 */
data class SaveSortieResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("savedAt") val savedAt: String
)

/**
 * Request to share sortie to chat
 */
data class ShareSortieToChatRequest(
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("senderId") val senderId: String
)

