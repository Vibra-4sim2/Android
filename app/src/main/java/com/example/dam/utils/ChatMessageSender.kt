package com.example.dam.utils

import android.content.Context
import android.util.Log
import com.example.dam.models.CreateMessageDto
import com.example.dam.models.MessageType
import com.example.dam.repository.MessageRepository
import com.example.dam.remote.SocketService

/**
 * ✅ FIXED SOLUTION FOR SHARING TO CHAT
 *
 * This singleton sends share messages via the backend API (MessageRepository)
 * which saves them to the database. The WebSocket will then notify all connected users.
 *
 * Previously tried to send via SocketService only, which didn't persist to database.
 */
object ChatMessageSender {
    private const val TAG = "ChatMessageSender"
    private val messageRepository = MessageRepository()

    /**
     * Share a sortie in a chat
     *
     * @param sortieId ID of the sortie to share
     * @param context Android context
     * @param onResult Callback with result (success/failure)
     */
    suspend fun shareSortieInChat(
        sortieId: String,
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            Log.d(TAG, "📤 Starting shareSortieInChat for sortieId: $sortieId")

            // 1. Get userId and token
            val userId = UserPreferences.getUserId(context)
            val token = UserPreferences.getToken(context)

            if (userId == null || token == null) {
                Log.e(TAG, "❌ No userId or token found")
                onResult(false, "Not authenticated")
                return
            }

            Log.d(TAG, "✅ User authenticated: $userId")

            // 2. Create message DTO
            val messageDto = CreateMessageDto(
                type = MessageType.TEXT,
                content = "SHARED_SORTIE:$sortieId"
            )

            Log.d(TAG, "📨 Sending message via MessageRepository to backend API")
            Log.d(TAG, "   Type: ${messageDto.type}")
            Log.d(TAG, "   Content: ${messageDto.content}")

            // 3. Send via backend API (which will save to DB and notify via WebSocket)
            val result = messageRepository.sendMessage(sortieId, "Bearer $token", messageDto)

            result.onSuccess { messageResponse ->
                Log.d(TAG, "✅ Message sent and saved to database: ${messageResponse._id}")
                onResult(true, null)
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to send message: ${error.message}")
                onResult(false, error.message)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in shareSortieInChat", e)
            onResult(false, e.message)
        }
    }

    /**
     * Share a publication in a chat
     *
     * @param publicationId ID of the publication to share
     * @param sortieId ID of the sortie (chat) to share in
     * @param publicationData Optional publication details (author, content, image, date)
     * @param context Android context
     * @param onResult Callback with result (success/failure)
     */
    suspend fun sharePublicationInChat(
        publicationId: String,
        sortieId: String,
        context: Context,
        publicationData: Map<String, String>? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            Log.d(TAG, "📤 Starting sharePublicationInChat")
            Log.d(TAG, "   publicationId: $publicationId")
            Log.d(TAG, "   sortieId: $sortieId")

            // 1. Get userId and token
            val userId = UserPreferences.getUserId(context)
            val token = UserPreferences.getToken(context)

            if (userId == null || token == null) {
                Log.e(TAG, "❌ No userId or token found")
                onResult(false, "Not authenticated")
                return
            }

            Log.d(TAG, "✅ User authenticated: $userId")

            // 2. Create message content with publication details if available
            val messageContent = if (publicationData != null) {
                val author = publicationData["author"] ?: ""
                val content = publicationData["content"] ?: ""
                val image = publicationData["image"] ?: ""
                val date = publicationData["date"] ?: ""
                "SHARED_PUBLICATION:$publicationId\nAUTHOR:$author\nCONTENT:$content\nIMAGE:$image\nDATE:$date"
            } else {
                "SHARED_PUBLICATION:$publicationId"
            }

            // 3. Create message DTO
            val messageDto = CreateMessageDto(
                type = MessageType.TEXT,
                content = messageContent
            )

            Log.d(TAG, "📨 Sending message via MessageRepository to backend API")
            Log.d(TAG, "   Type: ${messageDto.type}")
            Log.d(TAG, "   Content preview: ${messageContent.take(100)}...")

            // 4. Send via backend API (which will save to DB and notify via WebSocket)
            val result = messageRepository.sendMessage(sortieId, "Bearer $token", messageDto)

            result.onSuccess { messageResponse ->
                Log.d(TAG, "✅ Message sent and saved to database: ${messageResponse._id}")
                onResult(true, null)
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to send message: ${error.message}")
                onResult(false, error.message)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in sharePublicationInChat", e)
            onResult(false, e.message)
        }
    }
}
