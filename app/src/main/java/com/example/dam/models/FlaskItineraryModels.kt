package com.example.dam.models

import com.google.gson.annotations.SerializedName

data class FlaskItineraryRequest(
    @SerializedName("start") val start: FlaskLocationPoint,
    @SerializedName("end") val end: FlaskLocationPoint,
    @SerializedName("waypoints") val waypoints: List<FlaskLocationPoint>? = null,
    @SerializedName("context") val context: String? = null,
    @SerializedName("activity_type") val activityType: String? = null
)

data class FlaskLocationPoint(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("display_name") val displayName: String? = null
)

data class FlaskItineraryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("itinerary") val itinerary: GeneratedItinerary,
    @SerializedName("personalization") val personalization: ItineraryPersonalization,
    @SerializedName("ai_recommendations") val aiRecommendations: AiRecommendations,
    @SerializedName("metadata") val metadata: ItineraryMetadata
)

data class GeneratedItinerary(
    @SerializedName("geometry") val geometry: ItineraryGeometry,
    @SerializedName("summary") val summary: ItinerarySummary,
    @SerializedName("bbox") val bbox: List<Double>?,
    @SerializedName("segments") val segments: List<RouteSegment>?,
    @SerializedName("waypoints") val waypoints: List<WaypointInfo>?
)

data class ItineraryGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>,
    @SerializedName("type") val type: String
)

data class ItinerarySummary(
    @SerializedName("distance") val distance: Double, // en mètres
    @SerializedName("duration") val duration: Double, // en secondes
    @SerializedName("ascent") val ascent: Double? = null,
    @SerializedName("descent") val descent: Double? = null
)

data class RouteSegment(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("steps") val steps: List<RouteStep>?
)

data class RouteStep(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("instruction") val instruction: String,
    @SerializedName("name") val name: String?,
    @SerializedName("way_points") val wayPoints: List<Int>?
)

data class WaypointInfo(
    @SerializedName("location") val location: List<Double>,
    @SerializedName("name") val name: String?
)

data class ItineraryPersonalization(
    @SerializedName("profile_used") val profileUsed: String,
    @SerializedName("difficulty_assessment") val difficultyAssessment: String,
    @SerializedName("difficulty_score") val difficultyScore: Double,
    @SerializedName("elevation_preference") val elevationPreference: String?,
    @SerializedName("scenic_route") val scenicRoute: Boolean,
    @SerializedName("route_preference") val routePreference: String?
)

data class AiRecommendations(
    @SerializedName("suggested_stops") val suggestedStops: List<String>?,
    @SerializedName("safety_tips") val safetyTips: List<String>?,
    @SerializedName("equipment_suggestions") val equipmentSuggestions: List<String>?,
    @SerializedName("best_time_of_day") val bestTimeOfDay: String?,
    @SerializedName("weather_considerations") val weatherConsiderations: String?,
    @SerializedName("personalized_tips") val personalizedTips: List<String>?,
    @SerializedName("alternative_suggestion") val alternativeSuggestion: String?
)

data class ItineraryMetadata(
    @SerializedName("generated_by") val generatedBy: String?,
    @SerializedName("user_level") val userLevel: String?,
    @SerializedName("activity_type") val activityType: String?
)