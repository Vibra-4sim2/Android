// models/Models.kt
package com.example.dam.models

import com.google.gson.annotations.SerializedName

// ---------- ITINÉRAIRE ----------
data class Point(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("address") val address: String? = null
)

data class Itineraire(
    @SerializedName("pointDepart") val pointDepart: Point,
    @SerializedName("pointArrivee") val pointArrivee: Point,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duree_estimee") val duree_estimee: Double
)

// ---------- CAMPING ----------
data class CampingData(
    @SerializedName("nom") val nom: String,
    @SerializedName("lieu") val lieu: String,
    @SerializedName("prix") val prix: Double,
    @SerializedName("dateDebut") val dateDebut: String,
    @SerializedName("dateFin") val dateFin: String
)

// ---------- DIRECTIONS ----------
data class DirectionsResponse(val routes: List<Route>)
data class Route(val legs: List<Leg>)
data class Leg(val distance: Distance, val duration: Duration)
data class Distance(val text: String, val value: Int)
data class Duration(val text: String, val value: Int)

// ---------- GEOCODING ----------
data class GeocodingResponse(val results: List<GeocodeResult>, val status: String)

data class GeocodeResult(
    @SerializedName("formatted_address") val formattedAddress: String,
    val geometry: Geometry?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

// ============== SORTIE RESPONSE MODELS ==============

data class SortieResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("option_camping") val optionCamping: Boolean,
    @SerializedName("createurId") val createurId: CreateurInfo,
    @SerializedName("camping") val camping: CampingInfo?,
    @SerializedName("capacite") val capacite: Int,
    @SerializedName("participants") val participants: List<ParticipantInfo>,
    @SerializedName("itineraire") val itineraire: ItineraireInfo?,
    @SerializedName("photo") val photo: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class CreateurInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

data class CampingInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("nom") val nom: String,
    @SerializedName("lieu") val lieu: String,
    @SerializedName("prix") val prix: Double,
    @SerializedName("dateDebut") val dateDebut: String?,
    @SerializedName("dateFin") val dateFin: String?
)

data class ItineraireInfo(
    @SerializedName("pointDepart") val pointDepart: Point,
    @SerializedName("pointArrivee") val pointArrivee: Point,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duree_estimee") val dureeEstimee: Double
)

data class ParticipantInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("sortieId") val sortieId: String?,
    @SerializedName("status") val status: String?
)

// ============== PARTICIPATION MODELS ==============

data class ParticipationRequest(
    val sortieId: String
)

data class UpdateParticipationStatusRequest(
    val status: String
)

data class SimpleParticipationResponse(
    @SerializedName("_id") val _id: String,
    val userId: String,
    val sortieId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

data class ParticipationResponse(
    @SerializedName("_id") val _id: String,
    val userId: UserInfo,
    val sortieId: SortieInfo,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

data class UserInfo(
    @SerializedName("_id") val _id: String,
    val email: String,
    val name: String? = null
)

data class SortieInfo(
    @SerializedName("_id") val _id: String,
    val titre: String,
    val description: String,
    val difficulte: String,
    val date: String,
    val type: String,
    @SerializedName("option_camping") val optionCamping: Boolean,
    val createurId: String,
    val photo: String? = null,
    val camping: String? = null,
    val capacite: Int,
    val itineraire: Itineraire? = null,
    val participants: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

data class UserParticipationResponse(
    @SerializedName("_id") val _id: String,
    val userId: String,
    val sortieId: UserParticipationSortieInfo?,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

data class UserParticipationSortieInfo(
    @SerializedName("_id") val _id: String,
    val titre: String,
    val type: String,
    val date: String? = null,
    val photo: String? = null,
    val description: String? = null
)

data class CancelParticipationResponse(
    val message: String
)

// ============== RATING MODELS ==============

/**
 * Creator rating summary
 * GET /ratings/creator/{userId}
 */
data class CreatorRatingResponse(
    val average: Double,
    val count: Int
)

/**
 * Response when recomputing creator rating
 * POST /ratings/recompute/creator/{userId}
 */
data class RecomputeRatingResponse(
    @SerializedName("message")
    val message: String
)

data class RatingItem(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("stars") val stars: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class SortieRatingsResponse(
    @SerializedName("ratings") val ratings: List<RatingItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class CreateRatingRequest(
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("stars") val stars: Int,
    @SerializedName("comment") val comment: String? = null
)

data class CreateRatingResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("sortieId") val sortieId: String,
    @SerializedName("stars") val stars: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class SortieRatingData(
    val average: Double,
    val count: Int
)


/**
 * Eligible sortie for rating
 * GET /ratings/eligible
 */
data class EligibleSortieForRating(
    @SerializedName("sortieId")
    val sortieId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("camping")
    val camping: Boolean,

    @SerializedName("eligibleDate")
    val eligibleDate: String
)