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
    private const val BASE_URL = "http://10.0.2.2:3000/"

//    private const val BASE_URL = "http://192.168.121.98:3000/"


    // Pour appareil physique, décommentez et modifiez :
    // private const val BASE_URL = "http://192.168.1.X:3000/"

    /**
     * Intercepteur pour logger les requêtes HTTP (utile pour debug)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log complet (requête + réponse)
    }

    /**
     * Configuration du client HTTP
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)           // Ajoute le logging
        .connectTimeout(30, TimeUnit.SECONDS)         // Timeout connexion
        .readTimeout(30, TimeUnit.SECONDS)            // Timeout lecture
        .writeTimeout(30, TimeUnit.SECONDS)           // Timeout écriture
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

    val adventureApi: AdventureApi by lazy { retrofit.create(AdventureApi::class.java) }

    /**
     * Instance de l'API des publications
     */
    val publicationApi: PublicationApiService by lazy {
        retrofit.create(PublicationApiService::class.java)
    }
}


/*
COMMENT CRÉER CE FICHIER :
1. Clic droit sur java/com/example/dam/data/remote
2. New → Kotlin Class/File
3. Choisissez "Object"
4. Nommez : RetrofitInstance
5. Collez ce code

IMPORTANT - TROUVER VOTRE IP :
- Ouvrez CMD (Windows) ou Terminal (Mac)
- Tapez : ipconfig (Windows) ou ifconfig (Mac)
- Cherchez "IPv4 Address" ou "inet"
- Exemple : 192.168.1.5
- Remplacez dans BASE_URL si vous testez sur appareil physique
*/