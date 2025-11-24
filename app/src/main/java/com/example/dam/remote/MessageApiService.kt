// remote/MessageApiService.kt
package com.example.dam.remote

import com.example.dam.models.CreateMessageDto
import com.example.dam.models.MessageResponse
import com.example.dam.models.MessagesResponse
import com.example.dam.models.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface API pour la gestion des messages
 */
interface MessageApiService {

    /**
     * Récupère les messages d'un chat par sortieId
     * @param sortieId ID de la sortie
     * @param token Token JWT
     * @param page Numéro de page (pagination)
     * @param limit Nombre de messages par page
     * @return Liste paginée de messages
     */
    @GET("messages/sortie/{sortieId}")
    suspend fun getMessagesBySortie(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MessagesResponse>

    /**
     * Récupère les messages d'un chat par chatId
     * @param chatId ID du chat
     * @param token Token JWT
     * @param page Numéro de page
     * @param limit Nombre de messages par page
     * @return Liste paginée de messages
     */
    @GET("messages/chat/{chatId}")
    suspend fun getMessagesByChatId(
        @Path("chatId") chatId: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MessagesResponse>

    /**
     * Envoie un nouveau message
     * @param sortieId ID de la sortie
     * @param token Token JWT
     * @param message Contenu du message
     * @return Message créé
     */
    @POST("messages/sortie/{sortieId}")
    suspend fun sendMessage(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String,
        @Body message: CreateMessageDto
    ): Response<MessageResponse>

    /**
     * Upload un fichier média (image, vidéo, audio)
     * @param token Token JWT
     * @param file Fichier à uploader
     * @return Informations du fichier uploadé
     */
    @Multipart
    @POST("messages/upload")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    /**
     * Marque un message comme lu
     * @param messageId ID du message
     * @param token Token JWT
     */
    @POST("messages/{messageId}/read")
    suspend fun markAsRead(
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    /**
     * Supprime un message
     * @param messageId ID du message
     * @param token Token JWT
     */
    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    /**
     * Récupère un message spécifique par son ID
     * @param messageId ID du message
     * @param token Token JWT
     * @return Message
     */
    @GET("messages/{messageId}")
    suspend fun getMessageById(
        @Path("messageId") messageId: String,
        @Header("Authorization") token: String
    ): Response<MessageResponse>
}