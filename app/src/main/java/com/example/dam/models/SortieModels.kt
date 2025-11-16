// models/Models.kt
package com.example.dam.models

import com.google.gson.annotations.SerializedName

// ---------- ITINÉRAIRE ----------
data class Point(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("display_name") val displayName: String? = null,  // ✅ ADDED
    @SerializedName("address") val address: String? = null  // ✅ ADDED
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

// ============== ✅ NEW: SORTIE RESPONSE MODELS (ADD BELOW) ==============

data class SortieResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String, // "RANDONNEE", "VELO", "CAMPING"
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
    @SerializedName("email") val email: String
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
    @SerializedName("distance") val distance: Double, // en mètres
    @SerializedName("duree_estimee") val dureeEstimee: Double // en secondes
)

data class ParticipantInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("sortieId") val sortieId: String?,
    @SerializedName("status") val status: String?
)