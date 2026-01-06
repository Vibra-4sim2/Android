package com.example.dam.remote

// remote/OpenRouteServiceInstance.kt

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenRouteServiceInstance {
    private const val API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjVkNzZmZDM5ZjI5MjRkMTQ4MzUzYWU1MDVmMjlkYmNlIiwiaCI6Im11cm11cjY0In0="

    private const val BASE_URL = "https://api.openrouteservice.org/v2/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", API_KEY)
                .header("Accept", "application/json, application/geo+json")
                .header("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenRouteServiceApi = retrofit.create(OpenRouteServiceApi::class.java)
}