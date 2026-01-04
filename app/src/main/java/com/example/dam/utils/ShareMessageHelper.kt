package com.example.dam.utils

import android.content.Context
import android.util.Log
import com.example.dam.models.CreateMessageDto
import com.example.dam.models.MessageType
import com.example.dam.remote.SocketService

/**
 * ✅ Helper object for sharing content to chats without creating new ViewModels
 * This ensures messages are properly sent and received via the existing Socket connection
 */
object ShareMessageHelper {

    private const val TAG = "ShareMessageHelper"

    /**
     * Share a sortie to a chat
     * @param sortieId The ID of the sortie to share
     * @param chatSortieId The ID of the sortie/chat to send the message to
     * @param sortieData The sortie data (title, creator, image, etc.)
     * @param context Android context
     */
    fun shareSortieToChat(
        sortieId: String,
        chatSortieId: String,
        sortieData: SortieShareData,
        context: Context
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 SHARING SORTIE TO CHAT")
        Log.d(TAG, "Sortie ID: $sortieId")
        Log.d(TAG, "Chat Sortie ID: $chatSortieId")
        Log.d(TAG, "Sortie Title: ${sortieData.title}")
        Log.d(TAG, "========================================")

        try {
            // Create structured share message
            val shareMessage = buildString {
                append("SHARED_SORTIE:$sortieId\n")
                append("TITLE:${sortieData.title}\n")
                append("CREATOR:${sortieData.creator}\n")
                append("IMAGE:${sortieData.imageUrl}\n")
                append("DATE:${sortieData.date}\n")
                append("TYPE:${sortieData.type}")
            }

            Log.d(TAG, "Message content:")
            Log.d(TAG, shareMessage)

            if (!SocketService.isConnected()) {
                Log.e(TAG, "❌ Socket not connected! Attempting to connect...")
                val token = UserPreferences.getToken(context)
                if (!token.isNullOrEmpty()) {
                    SocketService.connect(token)
                    // Wait a bit for connection
                    Thread.sleep(1000)
                }
            }

            if (SocketService.isConnected()) {
                val messageDto = CreateMessageDto(
                    type = MessageType.TEXT,
                    content = shareMessage
                )

                SocketService.sendMessage(chatSortieId, messageDto)
                Log.d(TAG, "✅ Share message sent via Socket.IO")
            } else {
                Log.e(TAG, "❌ Could not connect to Socket.IO")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sharing sortie: ${e.message}", e)
        }

        Log.d(TAG, "========================================")
    }

    /**
     * Share a publication to a chat
     * @param publicationId The ID of the publication to share
     * @param chatSortieId The ID of the sortie/chat to send the message to
     * @param publicationData The publication data
     * @param context Android context
     */
    fun sharePublicationToChat(
        publicationId: String,
        chatSortieId: String,
        publicationData: PublicationShareData,
        context: Context
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 SHARING PUBLICATION TO CHAT")
        Log.d(TAG, "Publication ID: $publicationId")
        Log.d(TAG, "Chat Sortie ID: $chatSortieId")
        Log.d(TAG, "========================================")

        try {
            val shareMessage = buildString {
                append("SHARED_PUBLICATION:$publicationId\n")
                append("CONTENT:${publicationData.content}\n")
                append("CREATOR:${publicationData.creator}\n")
                if (!publicationData.imageUrl.isNullOrEmpty()) {
                    append("IMAGE:${publicationData.imageUrl}\n")
                }
                append("DATE:${publicationData.date}")
            }

            Log.d(TAG, "Message content:")
            Log.d(TAG, shareMessage)

            if (!SocketService.isConnected()) {
                Log.e(TAG, "❌ Socket not connected! Attempting to connect...")
                val token = UserPreferences.getToken(context)
                if (!token.isNullOrEmpty()) {
                    SocketService.connect(token)
                    Thread.sleep(1000)
                }
            }

            if (SocketService.isConnected()) {
                val messageDto = CreateMessageDto(
                    type = MessageType.TEXT,
                    content = shareMessage
                )

                SocketService.sendMessage(chatSortieId, messageDto)
                Log.d(TAG, "✅ Share message sent via Socket.IO")
            } else {
                Log.e(TAG, "❌ Could not connect to Socket.IO")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sharing publication: ${e.message}", e)
        }

        Log.d(TAG, "========================================")
    }

    /**
     * Data class for sortie sharing
     */
    data class SortieShareData(
        val title: String,
        val creator: String,
        val imageUrl: String,
        val date: String,
        val type: String
    )

    /**
     * Data class for publication sharing
     */
    data class PublicationShareData(
        val content: String,
        val creator: String,
        val imageUrl: String?,
        val date: String
    )
}

