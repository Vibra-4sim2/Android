package com.example.dam.remote

import com.example.dam.models.PublicationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface qui définit les endpoints de l'API des publications
 *
 * ⚠️ ATTENTION : La route est "publication" SANS S !
 */
interface PublicationApiService {

    /**
     * POST /publication (SANS S !)
     *
     * ✅ BACKEND : http://localhost:3000/publication
     * ✅ ANDROID : http://10.0.2.2:3000/publication
     */
    @Multipart
    @POST("publication")  // ⚠️ SANS S !!!
    suspend fun createPublication(
        @Part("author") author: RequestBody,
        @Part("content") content: RequestBody,
        @Part file: MultipartBody.Part? = null,
        @Part("tags") tags: RequestBody? = null,
        @Part("mentions") mentions: RequestBody? = null,
        @Part("location") location: RequestBody? = null
    ): Response<PublicationResponse>

    /**
     * GET /publication (SANS S !)
     */
    @GET("publication")  // ⚠️ SANS S !!!
    suspend fun getAllPublications(): Response<List<PublicationResponse>>

    /**
     * GET /publication/{id} (SANS S !)
     */
    @GET("publication/{id}")  // ⚠️ SANS S !!!
    suspend fun getPublicationById(
        @Path("id") id: String
    ): Response<PublicationResponse>

    /**
     * POST /publication/{id}/like (SANS S !)
     */
    @POST("publication/{id}/like")  // ⚠️ SANS S !!!
    suspend fun likePublication(
        @Path("id") id: String,
        @Body userId: Map<String, String>
    ): Response<PublicationResponse>

    /**
     * GET /publication/author/{authorId} (SANS S !)
     */
    @GET("publication/author/{authorId}")  // ⚠️ SANS S !!!
    suspend fun getPublicationsByAuthor(
        @Path("authorId") authorId: String
    ): Response<List<PublicationResponse>>

    /**
     * DELETE /publication/{id} (SANS S !)
     */
    @DELETE("publication/{id}")  // ⚠️ SANS S !!!
    suspend fun deletePublication(
        @Path("id") id: String
    ): Response<PublicationResponse>
}