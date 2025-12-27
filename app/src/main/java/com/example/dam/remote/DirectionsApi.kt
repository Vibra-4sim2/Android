// remote/DirectionsApi.kt
package com.example.dam.remote

import com.example.dam.models.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") key: String
    ): DirectionsResponse
}