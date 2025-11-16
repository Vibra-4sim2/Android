// remote/GeocodingApi.kt
package com.example.dam.remote

import com.example.dam.models.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latlng: String,
        @Query("key") key: String
    ): GeocodingResponse




    // ✅ NOUVEAU: Geocoding normal (adresse → coordonnées)
    @GET("geocode/json")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") key: String
    ): GeocodingResponse
}