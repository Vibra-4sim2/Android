// remote/OpenRouteServiceApi.kt
package com.example.dam.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Requête
data class ORSRequest(
    val coordinates: List<List<Double>>,
    val format: String = "geojson"
)

// Réponse complète ORS
data class ORSResponse(
    val features: List<Feature>
) {
    data class Feature(
        val geometry: Geometry,
        val properties: Properties?
    )

    data class Geometry(
        val coordinates: List<List<Double>>,  // ← LA CLÉ ICI
        val type: String
    )

    data class Properties(
        val summary: Summary?
    )

    data class Summary(
        val distance: Double,
        val duration: Double
    )
}

interface OpenRouteServiceApi {
    @POST("directions/{profile}/geojson")
    suspend fun getDirections(
        @Path("profile") profile: String,
        @Body body: ORSRequest
    ): ORSResponse
}