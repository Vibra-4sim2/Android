package com.example.dam.remote

// remote/FlaskAiApi.kt

import com.example.dam.models.FlaskItineraryRequest
import com.example.dam.models.FlaskItineraryResponse
import com.example.dam.models.FlaskRecommendationsResponse
import com.example.dam.models.MatchmakingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ✅ NEW: Interface pour l'API Flask (IA/Recommendations)
 * Base URL: https://flask-ai-api-1ynk.onrender.com/api
 */
interface FlaskAiApi {

    /**
     * GET /recommendations
     * Obtient les recommandations de sorties pour l'utilisateur connecté
     * L'ID utilisateur est récupéré automatiquement du JWT
     *
     * @param token JWT token (format: "Bearer eyJ...")
     * @return Liste de sorties recommandées avec cluster info
     */
    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") token: String
    ): Response<FlaskRecommendationsResponse>

    /**
     * GET /matching
     * Trouve les utilisateurs les plus similaires à l'utilisateur connecté
     * Utilise K-Nearest Neighbors (KNN) pour calculer la similarité
     *
     * @param token JWT token (format: "Bearer eyJ...")
     * @param minSimilarity Score minimum 0-1 (défaut: 0.05)
     * @param limit Nombre max de matches (défaut: 10, max: 50)
     * @return Liste d'utilisateurs similaires avec scores
     */
    @GET("matching")
    suspend fun getMatchmaking(
        @Header("Authorization") token: String,
        @Query("minSimilarity") minSimilarity: Double? = null,
        @Query("limit") limit: Int? = null
    ): Response<MatchmakingResponse>



    @POST("itinerary/generate")
    suspend fun generateItinerary(
        @Header("Authorization") token: String,
        @Body request: FlaskItineraryRequest
    ): Response<FlaskItineraryResponse>
}