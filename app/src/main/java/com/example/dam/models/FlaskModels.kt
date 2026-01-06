package com.example.dam.models

import com.google.gson.annotations.SerializedName

// ============== FLASK RECOMMENDATIONS MODELS ==============

/**
 * ✅ Response from Flask GET /recommendations
 */
data class FlaskRecommendationsResponse(
    @SerializedName("userId") val userId: String?,
    @SerializedName("userCluster") val userCluster: Int?,
    @SerializedName("recommendations") val recommendations: List<FlaskSortieResponse>?,
    @SerializedName("debug") val debug: Map<String, Any>?
)

/**
 * ✅ Sortie format from Flask
 */
data class FlaskSortieResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String,
    @SerializedName("difficulte") val difficulte: String?,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("option_camping") val optionCamping: Boolean,
    @SerializedName("photo") val photo: String?,
    @SerializedName("capacite") val capacite: Int,
    @SerializedName("createurId") val createurId: String,
    @SerializedName("itineraire") val itineraire: FlaskItineraire?,
    @SerializedName("participants") val participants: List<String>?
)

data class FlaskItineraire(
    @SerializedName("pointDepart") val pointDepart: FlaskPoint,
    @SerializedName("pointArrivee") val pointArrivee: FlaskPoint,
    @SerializedName("description") val description: String?,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duree_estimee") val dureeEstimee: Double,
    @SerializedName("geometry") val geometry: List<List<Double>>?,
    @SerializedName("instructions") val instructions: List<String>?
)

data class FlaskPoint(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("address") val address: String?
)

// ============== MATCHMAKING MODELS ==============

/**
 * ✅ Response from Flask GET /matching
 */
data class MatchmakingResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("totalMatches") val totalMatches: Int,
    @SerializedName("matches") val matches: List<UserMatch>,
    @SerializedName("algorithm") val algorithm: String,
    @SerializedName("parameters") val parameters: Map<String, Any>
)

/**
 * ✅ Individual user match with similarity score
 */
data class UserMatch(
    @SerializedName("userId") val userId: String,
    @SerializedName("similarity") val similarity: Double,
    @SerializedName("similarityPercent") val similarityPercent: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("user") val user: MatchedUserInfo,
    @SerializedName("matchedPreferences") val matchedPreferences: Map<String, Any>
)

/**
 * ✅ User info in match result
 */
data class MatchedUserInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("Gender") val gender: String?
)


/**
 * ✅ Smart match for ML-based sortie recommendations
 */
data class SmartMatch(
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("matchScore") val matchScore: Double,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("sortie") val sortie: SortieResponse? = null
)

// ============== PERSONALIZED SORTIE ANALYSIS MODELS ==============

/**
 * ✅ Response from Flask GET /sortie/analyze/{sortie_id}/personalized
 */
data class PersonalizedSortieAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("sortie") val sortie: Map<String, Any>?,
    @SerializedName("analysis") val analysis: SortieAnalysis,
    @SerializedName("personalized_tips") val personalizedTips: List<String>,
    @SerializedName("safety_warnings") val safetyWarnings: List<String>,
    @SerializedName("preparation_checklist") val preparationChecklist: List<String>,
    @SerializedName("nutrition_tips") val nutritionTips: List<String>,
    @SerializedName("equipment") val equipment: List<EquipmentItem>,
    @SerializedName("equipment_summary") val equipmentSummary: EquipmentSummary,
    @SerializedName("metadata") val metadata: Map<String, Any>?
)

data class SortieAnalysis(
    @SerializedName("difficulty_score") val difficultyScore: Double,
    @SerializedName("difficulty_label") val difficultyLabel: String,
    @SerializedName("estimated_duration") val estimatedDuration: String,
    @SerializedName("physical_demand") val physicalDemand: String,
    @SerializedName("technical_demand") val technicalDemand: String,
    @SerializedName("weather_sensitivity") val weatherSensitivity: String,
    @SerializedName("best_season") val bestSeason: String
)

data class EquipmentItem(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("image") val image: String?,
    @SerializedName("description") val description: String,
    @SerializedName("buy_link") val buyLink: String?,
    @SerializedName("price_range") val priceRange: String?,
    @SerializedName("necessity") val necessity: String,
    @SerializedName("necessity_reason") val necessityReason: String
)

data class EquipmentSummary(
    @SerializedName("essential_count") val essentialCount: Int,
    @SerializedName("recommended_count") val recommendedCount: Int,
    @SerializedName("optional_count") val optionalCount: Int,
    @SerializedName("total_estimated_cost") val totalEstimatedCost: String
)
