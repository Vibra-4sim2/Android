package com.example.dam.repository

import android.util.Log
import com.example.dam.models.FlaskItineraryRequest
import com.example.dam.models.FlaskItineraryResponse
import com.example.dam.models.FlaskRecommendationsResponse
import com.example.dam.models.MatchmakingResponse
import com.example.dam.models.PersonalizedSortieAnalysisResponse
import com.example.dam.remote.RetrofitFlask
import com.example.dam.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ✅ Repository for Flask AI API (Recommendations, Matchmaking & Itinerary)
 * Uses Result from com.example.dam.utils
 */
class FlaskAiRepository {

    private val api = RetrofitFlask.aiApi
    private val TAG = "FlaskAiRepository"

    /**
     * Get AI-powered recommendations from Flask
     *
     * @param token JWT token (will be formatted as "Bearer token")
     * @return Result with FlaskRecommendationsResponse
     */
    suspend fun getAiRecommendations(
        token: String
    ): Result<FlaskRecommendationsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

                Log.d(TAG, "📊 Fetching AI recommendations from Flask")
                Log.d(TAG, "🔑 Using token: ${bearerToken.take(30)}...")

                val response = api.getRecommendations(bearerToken)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val recommendationsCount = data.recommendations?.size ?: 0

                    Log.d(TAG, "✅ Got $recommendationsCount AI recommendations")
                    Log.d(TAG, "✅ User cluster: ${data.userCluster}")

                    Result.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        401 -> "Token JWT invalide ou manquant"
                        404 -> "Utilisateur ou préférences non trouvés"
                        503 -> "Service de recommandations non disponible"
                        else -> "Erreur ${response.code()}: $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau Flask: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION: $errorMsg", e)
                Result.Failure(e)
            }
        }
    }

    /**
     * Get user matchmaking results from Flask KNN algorithm
     *
     * @param token JWT token (will be formatted as "Bearer token")
     * @param minSimilarity Minimum similarity score (0-1), default: 0.05
     * @param limit Maximum number of matches, default: 10, max: 50
     * @return Result with MatchmakingResponse
     */
    suspend fun getMatchmaking(
        token: String,
        minSimilarity: Double? = null,
        limit: Int? = null
    ): Result<MatchmakingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

                Log.d(TAG, "🎯 Fetching matchmaking from Flask")
                Log.d(TAG, "🔑 Using token: ${bearerToken.take(30)}...")
                Log.d(TAG, "📊 Params: minSimilarity=$minSimilarity, limit=$limit")

                val response = api.getMatchmaking(bearerToken, minSimilarity, limit)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    Log.d(TAG, "✅ Got ${data.totalMatches} matches")
                    Log.d(TAG, "✅ Algorithm: ${data.algorithm}")

                    Result.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        401 -> "Token JWT invalide ou manquant"
                        404 -> "Utilisateur ou préférences non trouvés"
                        503 -> "Service de matching non disponible"
                        else -> "Erreur ${response.code()}: $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau Flask: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION: $errorMsg", e)
                Result.Failure(e)
            }
        }
    }

    /**
     * Generate personalized itinerary with AI recommendations
     *
     * @param token JWT token (will be formatted as "Bearer token")
     * @param request Itinerary request with start, end, waypoints, context
     * @return Result with FlaskItineraryResponse
     */
    suspend fun generateItinerary(
        token: String,
        request: FlaskItineraryRequest
    ): Result<FlaskItineraryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

                Log.d(TAG, "🗺️ Generating AI itinerary from Flask")
                Log.d(TAG, "🔑 Using token: ${bearerToken.take(30)}...")
                Log.d(TAG, "📍 Start: ${request.start.lat}, ${request.start.lon}")
                Log.d(TAG, "📍 End: ${request.end.lat}, ${request.end.lon}")
                Log.d(TAG, "🎯 Activity: ${request.activityType ?: "auto-detect"}")
                Log.d(TAG, "💬 Context: ${request.context ?: "none"}")

                val response = api.generateItinerary(bearerToken, request)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    Log.d(TAG, "✅ Itinerary generated successfully")
                    Log.d(TAG, "✅ Distance: ${data.itinerary.summary.distance / 1000} km")
                    Log.d(TAG, "✅ Duration: ${data.itinerary.summary.duration / 60} min")
                    Log.d(TAG, "✅ Difficulty: ${data.personalization.difficultyAssessment}")
                    Log.d(TAG, "✅ AI Tips: ${data.aiRecommendations.personalizedTips?.size ?: 0}")

                    Result.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        401 -> "Token JWT invalide ou manquant"
                        404 -> "Préférences utilisateur non trouvées"
                        503 -> "Service d'itinéraire non disponible"
                        else -> "Erreur ${response.code()}: $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau Flask: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION: $errorMsg", e)
                Result.Failure(e)
            }
        }
    }

    /**
     * Get personalized sortie analysis adapted to user profile
     *
     * @param token JWT token (will be formatted as "Bearer token")
     * @param sortieId ID of the sortie to analyze
     * @return Result with PersonalizedSortieAnalysisResponse
     */
    suspend fun getPersonalizedSortieAnalysis(
        token: String,
        sortieId: String
    ): Result<PersonalizedSortieAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

                Log.d(TAG, "🔍 Analyzing sortie $sortieId for user profile")
                Log.d(TAG, "🔑 Using token: ${bearerToken.take(30)}...")

                val response = api.getPersonalizedSortieAnalysis(bearerToken, sortieId)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    Log.d(TAG, "✅ Sortie analysis retrieved successfully")
                    Log.d(TAG, "✅ Difficulty: ${data.analysis.difficultyLabel}")
                    Log.d(TAG, "✅ Equipment items: ${data.equipment.size}")
                    Log.d(TAG, "✅ Tips: ${data.personalizedTips.size}")
                    Log.d(TAG, "✅ Safety warnings: ${data.safetyWarnings.size}")

                    Result.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        401 -> "Token JWT invalide ou manquant"
                        404 -> "Sortie non trouvée"
                        503 -> "Service d'analyse non disponible"
                        else -> "Erreur ${response.code()}: $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur réseau Flask: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION: $errorMsg", e)
                Result.Failure(e)
            }
        }
    }
}