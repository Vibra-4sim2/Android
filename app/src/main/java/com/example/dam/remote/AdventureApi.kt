// remote/AdventureApi.kt
package com.example.dam.remote

import com.example.dam.models.CreateRatingRequest
import com.example.dam.models.CreateRatingResponse
import com.example.dam.models.CreatorRatingResponse
import com.example.dam.models.EligibleSortieForRating
import com.example.dam.models.ParticipationRequest
import com.example.dam.models.ParticipationResponse
import com.example.dam.models.RatingItem
import com.example.dam.models.RecomputeRatingResponse
import com.example.dam.models.RecommendationsResponse
import com.example.dam.models.SimpleParticipationResponse
import com.example.dam.models.SortieRatingsResponse
import com.example.dam.models.SortieResponse
import com.example.dam.models.UpdateParticipationStatusRequest
import com.example.dam.models.UserParticipationResponse
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
        @Part("itineraire") itineraire: RequestBody,
        @Part("camping") camping: RequestBody?
    ): Response<Any>

    // ============== GET ENDPOINTS ==============

    @GET("sorties")
    suspend fun getAllSorties(): Response<List<SortieResponse>>

    @GET("sorties/{id}")
    suspend fun getSortieById(
        @Path("id") id: String
    ): Response<SortieResponse>

    // ============== PARTICIPATION ENDPOINTS ==============

    @POST("participations")
    suspend fun createParticipation(
        @Header("Authorization") token: String,
        @Body request: ParticipationRequest
    ): Response<SimpleParticipationResponse>

    @GET("participations")
    suspend fun getParticipations(
        @Query("sortieId") sortieId: String
    ): Response<List<ParticipationResponse>>

    @PATCH("participations/{id}/status")
    suspend fun updateParticipationStatus(
        @Path("id") id: String,
        @Header("Authorization") token: String,
        @Body request: UpdateParticipationStatusRequest
    ): Response<SimpleParticipationResponse>

    @DELETE("participations/{id}")
    suspend fun cancelParticipation(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("participations/user/{userId}")
    suspend fun getUserParticipations(
        @Path("userId") userId: String
    ): Response<List<UserParticipationResponse>>

    // ============== RATING ENDPOINTS ==============

    /**
     * ✅ FIXED: Get creator rating summary
     * GET /ratings/creator/{userId}
     * Returns: {"average": 3.1, "count": 9}
     */
    @GET("ratings/creator/{userId}")
    suspend fun getCreatorRating(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<CreatorRatingResponse>

    /**
     * ✅ FIXED: Recompute creator rating
     * POST /ratings/recompute/creator/{userId}
     * Returns: {"message": "Creator summary recomputed successfully"}
     */
    @POST("ratings/recompute/creator/{userId}")
    suspend fun recomputeCreatorRating(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<RecomputeRatingResponse>

    @GET("recommendations/user/{userId}")
    suspend fun getRecommendationsForUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<RecommendationsResponse>

    @GET("ratings/sortie/{sortieId}")
    suspend fun getSortieRatings(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<SortieRatingsResponse>

    @POST("ratings/sortie/{sortieId}")
    suspend fun createRating(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String,
        @Body request: CreateRatingRequest
    ): Response<CreateRatingResponse>

    @GET("ratings/sortie/{sortieId}/user")
    suspend fun getUserRatingForSortie(
        @Path("sortieId") sortieId: String,
        @Header("Authorization") token: String
    ): Response<RatingItem>

    @GET("ratings/eligible")
    suspend fun getEligibleSortiesForRating(
        @Header("Authorization") token: String
    ): Response<List<EligibleSortieForRating>>

    // ============== SAVED SORTIES ==============
    // Note: Les sorties sauvegardées sont gérées LOCALEMENT via LocalSavedSortiesManager
    // Aucun endpoint backend n'est nécessaire pour cette fonctionnalité
}