// repository/MessageRepository.kt
package com.example.dam.repository

import android.util.Log
import com.example.dam.models.*
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.Result

/**
 * Repository pour gérer les opérations liées aux messages
 */
class MessageRepository {

    private val messageApi = RetrofitInstance.messageApi
    private val TAG = "MessageRepository"

    /**
     * Récupère les messages d'une sortie
     * @param sortieId ID de la sortie
     * @param token Token JWT (format: "Bearer xxx")
     * @param page Numéro de page
     * @param limit Nombre de messages par page
     * @return Result contenant la réponse paginée ou une erreur
     */
    suspend fun getMessages(
        sortieId: String,
        token: String,
        page: Int = 1,
        limit: Int = 50
    ): Result<MessagesResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching messages for sortie: $sortieId (page: $page)")

            val response = messageApi.getMessagesBySortie(sortieId, token, page, limit)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Messages fetched: ${response.body()!!.messages.size}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching messages: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Envoie un nouveau message
     * @param sortieId ID de la sortie
     * @param token Token JWT
     * @param messageDto Contenu du message
     * @return Result contenant le message créé ou une erreur
     */
    suspend fun sendMessage(
        sortieId: String,
        token: String,
        messageDto: CreateMessageDto
    ): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending message to sortie: $sortieId")
            Log.d(TAG, "Message type: ${messageDto.type}, content: ${messageDto.content}")

            val response = messageApi.sendMessage(sortieId, token, messageDto)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Message sent successfully: ${response.body()!!._id}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending message: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Upload un fichier média
     * @param file Fichier à uploader
     * @param token Token JWT
     * @return Result contenant les infos du fichier uploadé
     */
    suspend fun uploadMedia(
        file: File,
        token: String
    ): Result<UploadResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Uploading media: ${file.name}")

            // Déterminer le type MIME
            val mimeType = when (file.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "mp4" -> "video/mp4"
                "mp3" -> "audio/mpeg"
                else -> "application/octet-stream"
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = messageApi.uploadMedia(token, body)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Media uploaded: ${response.body()!!.url}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur upload: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception uploading media: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Marque un message comme lu
     * @param messageId ID du message
     * @param token Token JWT
     */
    suspend fun markAsRead(
        messageId: String,
        token: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = messageApi.markAsRead(messageId, token)

            if (response.isSuccessful) {
                Log.d(TAG, "Message $messageId marked as read")
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to mark as read"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking as read: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Supprime un message
     * @param messageId ID du message
     * @param token Token JWT
     */
    suspend fun deleteMessage(
        messageId: String,
        token: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = messageApi.deleteMessage(messageId, token)

            if (response.isSuccessful) {
                Log.d(TAG, "Message $messageId deleted")
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete message"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting message: ${e.message}", e)
            Result.failure(e)
        }
    }
}