package com.example.dam.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    /**
     * Intercepteur pour logger les requêtes HTTP
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Configuration du client HTTP
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instance Retrofit configurée
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Instance de l'API d'authentification
     */
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    /**
     * Instance de l'API des publications
     */
    val publicationApi: PublicationApiService by lazy {
        retrofit.create(PublicationApiService::class.java)
    }
}