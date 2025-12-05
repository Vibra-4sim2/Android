package com.example.dam.models

// models/RecommendationsModels.kt
import com.google.gson.annotations.SerializedName



/**
 * Response principale de l'endpoint /recommendations/user/{userId}
 */
data class RecommendationsResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("userCluster") val userCluster: Int,
    @SerializedName("recommendations") val recommendations: List<SortieResponse>
)

/**
 * Wrapper pour les Smart Matches avec score
 */
data class SmartMatch(
    val sortie: SortieResponse,
    val matchScore: Int
)