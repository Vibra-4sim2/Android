package com.example.dam.repository

import android.util.Log
import com.example.dam.models.*
import com.example.dam.remote.GoogleRetrofitInstance
import com.example.dam.remote.ORSRequest
import com.example.dam.remote.OpenRouteServiceInstance
import com.example.dam.remote.RetrofitInstance
import com.example.dam.utils.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

class AdventureRepository {
    private val api = RetrofitInstance.adventureApi
    private val directions = GoogleRetrofitInstance.directionsApi
    private val geocode = GoogleRetrofitInstance.geocodingApi
    private val key = GoogleRetrofitInstance.getApiKey()
    private val orsApi = OpenRouteServiceInstance.api

    // ❌ SUPPRIMÉ : private val client = OkHttpClient()
    // ❌ SUPPRIMÉ : companion object { BASE_URL }
    // ✅ On utilise maintenant RetrofitInstance.adventureApi qui a déjà la bonne URL

    suspend fun reverseGeocode(latLng: LatLng): String {
        return try {
            val result = geocode.reverseGeocode("${latLng.latitude},${latLng.longitude}", key)
            result.results.firstOrNull()?.formattedAddress ?: "Adresse inconnue"
        } catch (e: Exception) {
            "Erreur adresse"
        }
    }

    suspend fun calculateRoute(
        start: LatLng,
        end: LatLng,
        mode: String = "walking"
    ): Result<Pair<String, String>> = try {
        val profile = when (mode) {
            "driving" -> "driving-car"
            "cycling" -> "cycling-regular"
            "walking" -> "foot-walking"
            else -> "foot-walking"
        }

        val coordinates = listOf(
            listOf(start.longitude, start.latitude),
            listOf(end.longitude, end.latitude)
        )

        val request = ORSRequest(coordinates)
        val response = orsApi.getDirections(profile, request)

        val summary = response.features.firstOrNull()?.properties?.summary
            ?: return Result.Error("Pas d'itinéraire trouvé")

        val distanceKm = if (summary.distance < 10000)
            String.format("%.1f", summary.distance / 1000)
        else
            (summary.distance / 1000).roundToInt().toString()

        val durationMin = (summary.duration / 60).toInt()
        val durationText = if (durationMin < 60) "$durationMin min"
        else "${durationMin / 60}h ${durationMin % 60}min"

        Result.Success("$distanceKm km" to durationText)
    } catch (e: Exception) {
        Result.Error("Erreur réseau: ${e.message}")
    }

    suspend fun geocodeAddress(address: String): LatLng? {
        return try {
            val result = geocode.geocodeAddress(address, key)
            val location = result.results.firstOrNull()?.geometry?.location
            location?.let { LatLng(it.lat, it.lng) }
        } catch (e: Exception) {
            Log.e("GEOCODE", "Error: ${e.message}")
            null
        }
    }

    /**
     * ✅ VERSION CORRIGÉE : Utilise Retrofit API au lieu de OkHttpClient manuel
     */
    suspend fun createSortie(
        token: String,
        createurId: String,
        photoFile: File?,
        titre: String,
        description: String,
        date: String,
        type: String,
        optionCamping: Boolean,
        lieu: String,
        difficulte: String,
        niveau: String,
        capacite: Int,
        prix: Double,
        itineraire: Itineraire,
        camping: CampingData?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // ✅ Extraire le type sans emoji
            val cleanType = type.split(" ")[0] // "VELO" ou "RANDONNEE"

            // ✅ Itinéraire JSON
            val itineraireJson = JSONObject().apply {
                put("pointDepart", JSONObject().apply {
                    put("latitude", itineraire.pointDepart.latitude)
                    put("longitude", itineraire.pointDepart.longitude)
                })
                put("pointArrivee", JSONObject().apply {
                    put("latitude", itineraire.pointArrivee.latitude)
                    put("longitude", itineraire.pointArrivee.longitude)
                })
                put("distance", itineraire.distance)
                put("duree_estimee", itineraire.duree_estimee)
            }.toString()

            // ✅ Camping JSON (seulement si activé ET données fournies)
            val campingJson = if (optionCamping && camping != null) {
                JSONObject().apply {
                    put("nom", camping.nom)
                    put("lieu", camping.lieu)
                    put("prix", camping.prix)
                    put("dateDebut", camping.dateDebut)
                    put("dateFin", camping.dateFin)
                }.toString()
            } else null

            // ✅ Photo (optionnelle)
            val photoPart = photoFile?.let {
                MultipartBody.Part.createFormData(
                    "photo",
                    it.name,
                    it.asRequestBody("image/jpeg".toMediaType())
                )
            }

            Log.d("CREATE_SORTIE", "========== REQUEST DEBUG ==========")
            Log.d("CREATE_SORTIE", "Token: Bearer ${token.take(30)}...")
            Log.d("CREATE_SORTIE", "Titre: $titre")
            Log.d("CREATE_SORTIE", "Type: $cleanType")
            Log.d("CREATE_SORTIE", "Option Camping: $optionCamping")
            Log.d("CREATE_SORTIE", "Itineraire: $itineraireJson")
            if (campingJson != null) {
                Log.d("CREATE_SORTIE", "Camping: $campingJson")
            }
            Log.d("CREATE_SORTIE", "===================================")

            // ✅ UTILISATION DE RETROFIT API (pas de OkHttpClient manuel !)
            val response = api.createSortie(
                token = "Bearer $token",
                photo = photoPart,
                titre = titre.toRequestBody("text/plain".toMediaType()),
                description = description.toRequestBody("text/plain".toMediaType()),
                date = date.toRequestBody("text/plain".toMediaType()),
                type = cleanType.toRequestBody("text/plain".toMediaType()),
                optionCamping = optionCamping.toString().toRequestBody("text/plain".toMediaType()),
                lieu = lieu.ifEmpty { "Non spécifié" }.toRequestBody("text/plain".toMediaType()),
                difficulte = difficulte.ifEmpty { "MOYEN" }.toRequestBody("text/plain".toMediaType()),
                niveau = niveau.ifEmpty { "INTERMEDIAIRE" }.toRequestBody("text/plain".toMediaType()),
                capacite = capacite.toString().toRequestBody("text/plain".toMediaType()),
                prix = prix.toString().toRequestBody("text/plain".toMediaType()),
                itineraire = itineraireJson.toRequestBody("application/json".toMediaType()),
                camping = campingJson?.toRequestBody("application/json".toMediaType())
            )

            Log.d("CREATE_SORTIE", "Response Code: ${response.code()}")
            Log.d("CREATE_SORTIE", "Response Body: ${response.body()}")

            if (response.isSuccessful) {
                Result.Success("✅ Sortie créée avec succès !")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                Log.e("CREATE_SORTIE", "Error: $errorBody")
                Result.Error("❌ Erreur ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("CREATE_SORTIE", "❌ Exception: ${e.message}", e)
            Result.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getAllSorties(): Result<List<SortieResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d("GET_SORTIES", "🔄 Fetching sorties from API...")
            val response = api.getAllSorties()

            if (response.isSuccessful && response.body() != null) {
                val sorties = response.body()!!
                Log.d("GET_SORTIES", "✅ Got ${sorties.size} sorties from API")

                // Log avatar info for each sortie
                sorties.forEachIndexed { index, sortie ->
                    Log.d("GET_SORTIES", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d("GET_SORTIES", "Sortie #${index + 1}: ${sortie.titre}")
                    Log.d("GET_SORTIES", "  Creator ID: ${sortie.createurId.id}")
                    Log.d("GET_SORTIES", "  Creator Email: ${sortie.createurId.email}")
                    Log.d("GET_SORTIES", "  Creator Name: ${sortie.createurId.firstName} ${sortie.createurId.lastName}")
                    Log.d("GET_SORTIES", "  ⚠️ Creator Avatar: ${sortie.createurId.avatar}")
                    Log.d("GET_SORTIES", "  Avatar is null? ${sortie.createurId.avatar == null}")
                    Log.d("GET_SORTIES", "  Avatar is empty? ${sortie.createurId.avatar?.isEmpty()}")
                    if (sortie.createurId.avatar != null) {
                        Log.d("GET_SORTIES", "  ✅ Avatar URL present: ${sortie.createurId.avatar}")
                    } else {
                        Log.e("GET_SORTIES", "  ❌ NO AVATAR - Backend didn't send avatar!")
                    }
                    Log.d("GET_SORTIES", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                }

                Result.Success(sorties)
            } else {
                Log.e("GET_SORTIES", "❌ API Error: ${response.code()} - ${response.message()}")
                Result.Error("Erreur ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("GET_SORTIES", "❌ Exception: ${e.message}", e)
            Result.Error("Erreur réseau: ${e.message}")
        }
    }

    suspend fun getSortieById(id: String): Result<SortieResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSortieById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Sortie non trouvée")
            }
        } catch (e: Exception) {
            Log.e("GET_SORTIE_DETAIL", "Exception: ${e.message}", e)
            Result.Error("Erreur réseau: ${e.message}")
        }
    }




    suspend fun getRecommendationsForUser(
        userId: String,
        token: String
    ): Result<RecommendationsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("RECOMMENDATIONS", "Fetching recommendations for user: $userId")

            val response = api.getRecommendationsForUser(
                userId = userId,
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                val recommendations = response.body()!!
                Log.d("RECOMMENDATIONS", "✅ Loaded ${recommendations.recommendations.size} recommendations")
                Log.d("RECOMMENDATIONS", "User cluster: ${recommendations.userCluster}")
                Result.Success(recommendations)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("RECOMMENDATIONS", "❌ Error ${response.code()}: $errorBody")
                Result.Error("Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("RECOMMENDATIONS", "❌ Exception: ${e.message}", e)
            Result.Error("Network error: ${e.message}")
        }
    }
}