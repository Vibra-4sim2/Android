package com.example.dam.repository

import android.util.Log
import com.example.dam.models.*
import com.example.dam.remote.GoogleRetrofitInstance
import com.example.dam.remote.ORSRequest
import com.example.dam.remote.OpenRouteServiceInstance
import com.example.dam.remote.RetrofitInstance
import com.example.dam.utils.Result
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

class AdventureRepository {
    private val api = RetrofitInstance.adventureApi
    private val directions = GoogleRetrofitInstance.directionsApi
    private val geocode = GoogleRetrofitInstance.geocodingApi
    private val key = GoogleRetrofitInstance.getApiKey()
    private val gson = Gson()
    private val orsApi = OpenRouteServiceInstance.api
    private val client = OkHttpClient()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:3000"  // ✅ Sans le slash final
    }

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
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("titre", titre)
                .addFormDataPart("description", description)
                .addFormDataPart("date", date)

                // ✅ FIX: Extract only the type without emoji
                .addFormDataPart("type", type.split(" ")[0]) // "VELO" or "RANDONNEE"

                .addFormDataPart("option_camping", optionCamping.toString())

                // ✅ FIX: Provide default values if empty
                .addFormDataPart("lieu", lieu.ifEmpty { "Non spécifié" })
                .addFormDataPart("difficulte", difficulte.ifEmpty { "MOYEN" })
                .addFormDataPart("niveau", niveau.ifEmpty { "INTERMEDIAIRE" })
                .addFormDataPart("capacite", capacite.toString())
                .addFormDataPart("prix", prix.toString())

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

            requestBody.addFormDataPart("itineraire", itineraireJson)

            // ✅ FIX: Only send camping if both conditions are true
            if (optionCamping && camping != null) {
                val campingJson = JSONObject().apply {
                    put("nom", camping.nom)
                    put("lieu", camping.lieu)
                    put("prix", camping.prix)
                    put("dateDebut", camping.dateDebut)
                    put("dateFin", camping.dateFin)
                }.toString()

                Log.d("CREATE_SORTIE", "Camping JSON: $campingJson")
                requestBody.addFormDataPart("camping", campingJson)
            }

            // ✅ Photo (optional)
            if (photoFile != null && photoFile.exists()) {
                requestBody.addFormDataPart(
                    "photo",
                    photoFile.name,
                    photoFile.asRequestBody("image/jpeg".toMediaType())
                )
                Log.d("CREATE_SORTIE", "Photo added: ${photoFile.name}")
            }

            val request = Request.Builder()
                .url("$BASE_URL/sorties")
                .post(requestBody.build())
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "*/*")
                .build()

            Log.d("CREATE_SORTIE", "========== REQUEST DEBUG ==========")
            Log.d("CREATE_SORTIE", "URL: $BASE_URL/sorties")
            Log.d("CREATE_SORTIE", "Token: Bearer ${token.take(30)}...")
            Log.d("CREATE_SORTIE", "Titre: $titre")
            Log.d("CREATE_SORTIE", "Type: ${type.split(" ")[0]}")
            Log.d("CREATE_SORTIE", "Option Camping: $optionCamping")
            Log.d("CREATE_SORTIE", "Itineraire: $itineraireJson")
            Log.d("CREATE_SORTIE", "===================================")

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            Log.d("CREATE_SORTIE", "Response Code: ${response.code}")
            Log.d("CREATE_SORTIE", "Response Body: $body")

            if (response.isSuccessful) {
                Result.Success("✅ Sortie créée avec succès !")
            } else {
                Result.Error("❌ Erreur ${response.code}: $body")
            }
        } catch (e: Exception) {
            Log.e("CREATE_SORTIE", "❌ Exception: ${e.message}", e)
            Result.Error("Erreur réseau: ${e.message}")
        }
    }

// Add these functions at the bottom of AdventureRepository class

    suspend fun getAllSorties(): Result<List<SortieResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllSorties()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Erreur ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("GET_SORTIES", "Exception: ${e.message}", e)
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
}