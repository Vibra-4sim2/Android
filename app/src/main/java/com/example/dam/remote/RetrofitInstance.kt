package com.example.dam.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton pour configurer et fournir l'instance Retrofit
 */
object RetrofitInstance {

    // ========== CONFIGUREZ VOTRE URL ICI ==========
    // Pour émulateur Android Studio : 10.0.2.2
    // Pour appareil physique : l'IP de votre PC (ex: 192.168.1.5)
    // Pour trouver votre IP : cmd → ipconfig (Windows) ou ifconfig (Mac/Linux)
//    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val BASE_URL = "https://dam-4sim2.onrender.com/"
    private const val FLASK_URL = "https://dam-4sim2.onrender.com/"


    /**
     * Intercepteur pour logger les requêtes HTTP (utile pour debug)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log complet (requête + réponse)
    }


    val pollApi: PollApiService by lazy {
        retrofit.create(PollApiService::class.java)
    }

    /**
     * Configuration du client HTTP
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)           // Ajoute le logging
        // APRÈS (assez long pour attendre le réveil de Render)
        .connectTimeout(90, TimeUnit.SECONDS)  // ✅
        .readTimeout(90, TimeUnit.SECONDS)     // ✅
        .writeTimeout(90, TimeUnit.SECONDS)    // ✅         // Timeout écriture
        .build()

    /**
     * Instance Retrofit configurée
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // Conversion JSON
        .build()

    /**
     * Instance de l'API d'authentification
     * Utilisez : RetrofitInstance.authApi.login(...)
     */
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }


    val participationapi: AdventureApi by lazy {
        retrofit.create(AdventureApi::class.java)
    }

    val adventureApi: AdventureApi by lazy { retrofit.create(AdventureApi::class.java) }

    /**
     * Instance de l'API des publications
     */
    val publicationApi: PublicationApiService by lazy {
        retrofit.create(PublicationApiService::class.java)
    }


    /**
     * ✅ NOUVELLE INSTANCE - API des chats
     */
    val chatApi: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }

    val messageApi: MessageApiService by lazy {
        retrofit.create(MessageApiService::class.java)
    }

    /**
     * ✅ API des notifications (polling system)
     */
    val notificationApi: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }
}