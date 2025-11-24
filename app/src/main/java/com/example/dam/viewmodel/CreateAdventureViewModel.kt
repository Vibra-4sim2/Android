package com.example.dam.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.remote.ORSRequest
import com.example.dam.remote.OpenRouteServiceInstance
import com.example.dam.repository.AdventureRepository
import com.example.dam.utils.JwtHelper
import com.example.dam.utils.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CreateAdventureViewModel : ViewModel() {
    private val repo = AdventureRepository()

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var date by mutableStateOf("")
    var activityType by mutableStateOf("")
    var capacity by mutableStateOf("")
    var includeCamping by mutableStateOf(false)

    var campingName by mutableStateOf("")
    var campingLocation by mutableStateOf("")
    var campingPrice by mutableStateOf("")
    var campingStart by mutableStateOf("")
    var campingEnd by mutableStateOf("")

    var startLatLng by mutableStateOf<LatLng?>(null)
    var endLatLng by mutableStateOf<LatLng?>(null)
    var startAddress by mutableStateOf("")
    var endAddress by mutableStateOf("")
    var distance by mutableStateOf("")
    var footTime by mutableStateOf("")
    var bikeTime by mutableStateOf("")
    var calculating by mutableStateOf(false)
    var createResult by mutableStateOf<Result<String>?>(null)

    var polylinePoints by mutableStateOf<List<LatLng>>(emptyList())

    var editingStart by mutableStateOf(true)
    var isSearchingAddress by mutableStateOf(false)

    private var geocodeJob: Job? = null
    private var addressSearchJob: Job? = null

    fun setEditingPoint(isStart: Boolean) {
        editingStart = isStart
    }

    fun clearStartPoint() {
        startLatLng = null
        startAddress = ""
        polylinePoints = emptyList()
        distance = ""
        footTime = ""
        bikeTime = ""
    }

    fun clearEndPoint() {
        endLatLng = null
        endAddress = ""
        polylinePoints = emptyList()
        distance = ""
        footTime = ""
        bikeTime = ""
    }

    fun onMapClick(point: LatLng) {
        if (editingStart) {
            startLatLng = point
            startAddress = "Chargement..."

            geocodeJob?.cancel()
            geocodeJob = viewModelScope.launch {
                delay(300)
                startAddress = repo.reverseGeocode(point)
            }
        } else {
            endLatLng = point
            endAddress = "Chargement..."

            geocodeJob?.cancel()
            geocodeJob = viewModelScope.launch {
                delay(300)
                endAddress = repo.reverseGeocode(point)
            }
        }
    }

    fun searchAddress(address: String, isStart: Boolean) {
        if (address.isEmpty()) return

        isSearchingAddress = true
        addressSearchJob?.cancel()
        addressSearchJob = viewModelScope.launch {
            try {
                delay(500)
                val latLng = repo.geocodeAddress(address)
                if (latLng != null) {
                    if (isStart) {
                        startLatLng = latLng
                        startAddress = address
                    } else {
                        endLatLng = latLng
                        endAddress = address
                    }
                } else {
                    if (isStart) startAddress = "Adresse introuvable"
                    else endAddress = "Adresse introuvable"
                }
            } finally {
                isSearchingAddress = false
            }
        }
    }

    fun calculateRoute() {
        val s = startLatLng ?: return
        val e = endLatLng ?: return

        if (calculating) return

        calculating = true
        polylinePoints = emptyList()

        viewModelScope.launch {
            try {
                when (val walkResult = fetchCompleteRoute(s, e, "foot-walking")) {
                    is Result.Success -> {
                        val (dist, dur, points) = walkResult.data
                        distance = dist
                        footTime = dur
                        polylinePoints = points

                        launch {
                            when (val bikeResult = repo.calculateRoute(s, e, "cycling")) {
                                is Result.Success -> bikeTime = bikeResult.data.second
                                else -> bikeTime = "N/A"
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e("ROUTE", "Error: ${walkResult.message}")
                    }
                    else -> {}
                }
            } finally {
                calculating = false
            }
        }
    }

    private suspend fun fetchCompleteRoute(
        start: LatLng,
        end: LatLng,
        profile: String
    ): Result<Triple<String, String, List<LatLng>>> {
        return try {
            val coordinates = listOf(
                listOf(start.longitude, start.latitude),
                listOf(end.longitude, end.latitude)
            )
            val request = ORSRequest(coordinates)
            val response = OpenRouteServiceInstance.api.getDirections(profile, request)

            val feature = response.features.firstOrNull()
                ?: return Result.Error("Aucune route trouvée")

            val summary = feature.properties?.summary
                ?: return Result.Error("Pas de résumé disponible")

            val distanceKm = if (summary.distance < 10000)
                String.format("%.1f", summary.distance / 1000)
            else
                (summary.distance / 1000).toInt().toString()

            val durationMin = (summary.duration / 60).toInt()
            val durationText = if (durationMin < 60) "$durationMin min"
            else "${durationMin / 60}h ${durationMin % 60}min"

            val points = feature.geometry.coordinates.map { coord ->
                LatLng(coord[1], coord[0])
            }

            Result.Success(Triple("$distanceKm km", durationText, points))
        } catch (e: Exception) {
            Log.e("ROUTE", "Error: ${e.message}", e)
            Result.Error("Erreur: ${e.message}")
        }
    }

    fun createAdventure(token: String, photoFile: File?) {
        viewModelScope.launch {
            createResult = Result.Loading

            val userId = JwtHelper.getUserIdFromToken(token) ?: run {
                createResult = Result.Error("⚠️ Invalid token")
                return@launch
            }

            // Extract distance and duration
            val distanceMeters = distance.replace(" km", "").replace(",", ".").toDoubleOrNull()?.times(1000) ?: 0.0
            val durationSeconds = when {
                activityType.contains("VELO") -> bikeTime
                else -> footTime
            }.let { time ->
                val parts = time.split(" ")
                var seconds = 0.0
                parts.forEach { part ->
                    when {
                        part.contains("h") -> seconds += part.replace("h", "").toDoubleOrNull()?.times(3600) ?: 0.0
                        part.contains("min") -> seconds += part.replace("min", "").toDoubleOrNull()?.times(60) ?: 0.0
                    }
                }
                seconds
            }

            val itineraire = Itineraire(
                pointDepart = Point(startLatLng!!.latitude, startLatLng!!.longitude),
                pointArrivee = Point(endLatLng!!.latitude, endLatLng!!.longitude),
                distance = distanceMeters,
                duree_estimee = durationSeconds
            )

            val campingData = if (includeCamping && campingName.isNotEmpty()) {
                CampingData(
                    nom = campingName,
                    lieu = campingLocation,
                    prix = campingPrice.toDoubleOrNull() ?: 0.0,
                    dateDebut = campingStart.ifEmpty { date },
                    dateFin = campingEnd
                )
            } else null

            Log.d("CREATE_ADVENTURE", "Creating sortie:")
            Log.d("CREATE_ADVENTURE", "Title: $title")
            Log.d("CREATE_ADVENTURE", "Type: $activityType")
            Log.d("CREATE_ADVENTURE", "Date: $date")
            Log.d("CREATE_ADVENTURE", "Distance: $distanceMeters m")
            Log.d("CREATE_ADVENTURE", "Duration: $durationSeconds s")
            Log.d("CREATE_ADVENTURE", "Option Camping: $includeCamping")
            if (campingData != null) {
                Log.d("CREATE_ADVENTURE", "Camping Data: Présent")
            }

            // ✅ FIX: Use 'repo' instead of 'repository'
            val result = repo.createSortie(
                token = token,
                createurId = userId,
                photoFile = photoFile,
                titre = title,
                description = description,
                date = date,
                type = activityType,
                optionCamping = includeCamping,
                lieu = startAddress.ifEmpty { "Non spécifié" },
                difficulte = "MOYEN",
                niveau = "INTERMEDIAIRE",
                capacite = capacity.toIntOrNull() ?: 10,
                prix = 0.0,
                itineraire = itineraire,
                camping = campingData
            )

            createResult = result
        }
    }

    fun resetForm() {
        title = ""
        description = ""
        date = ""
        activityType = ""
        capacity = ""
        includeCamping = false
        campingName = ""
        campingLocation = ""
        campingPrice = ""
        campingStart = ""
        campingEnd = ""
        startLatLng = null
        endLatLng = null
        startAddress = ""
        endAddress = ""
        distance = ""
        footTime = ""
        bikeTime = ""
        polylinePoints = emptyList()
        createResult = null
        editingStart = true
    }
}