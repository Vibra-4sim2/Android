// remote/AdventureApi.kt
package com.example.dam.remote

import com.example.dam.models.ParticipationRequest
import com.example.dam.models.ParticipationResponse
import com.example.dam.models.RecommendationsResponse
import com.example.dam.models.SimpleParticipationResponse
import com.example.dam.models.SortieResponse
import com.example.dam.models.UpdateParticipationStatusRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AdventureApi {
    @Multipart
    @POST("sorties")
    suspend fun createSortie(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part?,
        @Part("titre") titre: RequestBody,
        @Part("description") description: RequestBody,
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("option_camping") optionCamping: RequestBody,
        @Part("lieu") lieu: RequestBody,
        @Part("difficulte") difficulte: RequestBody,
        @Part("niveau") niveau: RequestBody,
        @Part("capacite") capacite: RequestBody,
        @Part("prix") prix: RequestBody,
//        @Part("campingId") campingId: RequestBody?,
        @Part("itineraire") itineraire: RequestBody,
        @Part("camping") camping: RequestBody?
    ): Response<Any>




    // ============== NEW GET ENDPOINTS ==============

    @GET("sorties")
    suspend fun getAllSorties(): Response<List<SortieResponse>>

    @GET("sorties/{id}")
    suspend fun getSortieById(
        @Path("id") id: String
    ): Response<SortieResponse>




    // ============== PARTICIPATION ENDPOINTS ==============
    /**
     * ✅ FIXED: Create participation - returns SimpleParticipationResponse
     * POST /participations
     */
    @POST("participations")
    suspend fun createParticipation(
        @Header("Authorization") token: String,
        @Body request: ParticipationRequest
    ): Response<SimpleParticipationResponse>

    /**
     * Get participations for a sortie - returns ParticipationResponse with populated data
     * GET /participations?sortieId={sortieId}
     */
    @GET("participations")
    suspend fun getParticipations(
        @Query("sortieId") sortieId: String
    ): Response<List<ParticipationResponse>>

    /**
     * ✅ FIXED: Update participation status - returns SimpleParticipationResponse
     * PATCH /participations/{id}/status
     */
    @PATCH("participations/{id}/status")
    suspend fun updateParticipationStatus(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: UpdateParticipationStatusRequest
    ): Response<SimpleParticipationResponse>

    /**
     * Cancel participation
     * DELETE /participations/{id}
     */
    @DELETE("participations/{id}")
    suspend fun cancelParticipation(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>




    @GET("recommendations/user/{userId}")
    suspend fun getRecommendationsForUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<RecommendationsResponse>
}


