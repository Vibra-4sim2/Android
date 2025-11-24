// repository/ChatRepository.kt
package com.example.dam.repository

import android.util.Log
import com.example.dam.models.ChatResponse
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result

/**
 * Repository pour gérer les opérations liées aux chats
 */
class ChatRepository {

    private val chatApi = RetrofitInstance.chatApi
    private val TAG = "ChatRepository"

    /**
     * Récupère le chat d'une sortie spécifique
     * @param sortieId ID de la sortie
     * @param token Token JWT (format: "Bearer xxx")
     * @return Result contenant le ChatResponse ou une erreur
     */
    suspend fun getChatBySortie(
        sortieId: String,
        token: String
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching chat for sortie: $sortieId")

            val response = chatApi.getChatBySortie(sortieId, token)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Chat fetched successfully: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching chat: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère tous les chats de l'utilisateur connecté
     * @param token Token JWT (format: "Bearer xxx")
     * @return Result contenant la liste des chats ou une erreur
     */
    suspend fun getMyChats(
        token: String
    ): Result<List<ChatResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching user's chats")

            val response = chatApi.getMyChats(token)

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Chats fetched successfully: ${response.body()?.size} chats")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching chats: ${e.message}", e)
            Result.failure(e)
        }
    }
}