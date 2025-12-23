package com.example.dam.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ✅ Singleton Retrofit pour l'API Flask (IA/Recommendations)
 * Base URL: https://flask-ai-api-1ynk.onrender.com/api
 */
object RetrofitFlask {

    private const val FLASK_URL = "https://flask-ai-api-1ynk.onrender.com/api/"

    /**
     * Intercepteur pour logger les requêtes HTTP (utile pour debug)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Configuration du client HTTP
     * Timeouts longs pour attendre le réveil de Render (cold start)
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    /**
     * Instance Retrofit configurée pour Flask
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(FLASK_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * ✅ FIXED: Expose FlaskAiApi (not AdventureApi)
     * Usage: RetrofitFlask.aiApi.getRecommendations(token)
     */
    val aiApi: FlaskAiApi by lazy {
        retrofit.create(FlaskAiApi::class.java)
    }
}