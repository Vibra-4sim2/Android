package com.example.dam.remote

// remote/GoogleRetrofitInstance.kt

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GoogleRetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private const val API_KEY = "AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o"
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val directionsApi: DirectionsApi by lazy { retrofit.create(DirectionsApi::class.java) }
    val geocodingApi: GeocodingApi by lazy { retrofit.create(GeocodingApi::class.java) }

    fun getApiKey() = API_KEY
}