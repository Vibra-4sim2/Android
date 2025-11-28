// remote/ChatApiService.kt
package com.example.dam.remote

import com.example.dam.models.ChatResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Interface API pour gérer les chats de groupe
 */
interface ChatApiService {

    /**
     * Récupère le chat associé à une sortie spécifique
     * @param sortieId ID de la sortie
     * @param token Token JWT d'authentification
     * @return ChatResponse contenant les détails du chat
     */
    @GET("chats/sortie/{sortieId}")
    suspend fun getChatBySortie(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String
    ): Response<ChatResponse>

    /**
     * Récupère tous les chats dont l'utilisateur est membre
     * @param token Token JWT d'authentification
     * @return Liste des chats
     */
    @GET("chats/my-chats")
    suspend fun getMyChats(
        @Header("Authorization") token: String
    ): Response<List<ChatResponse>>
}