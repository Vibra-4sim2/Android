package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.repository.FlaskAiRepository
import com.example.dam.utils.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlaskAiViewModel : ViewModel() {

    private val repository = FlaskAiRepository()
    private val TAG = "FlaskAiViewModel"

    // ============== RECOMMENDATIONS STATE ==============
    private val _recommendations = MutableStateFlow<List<FlaskSortieResponse>>(emptyList())
    val recommendations: StateFlow<List<FlaskSortieResponse>> = _recommendations.asStateFlow()

    private val _userCluster = MutableStateFlow<Int?>(null)
    val userCluster: StateFlow<Int?> = _userCluster.asStateFlow()

    private val _recommendationsLoading = MutableStateFlow(false)
    val recommendationsLoading: StateFlow<Boolean> = _recommendationsLoading.asStateFlow()

    private val _recommendationsError = MutableStateFlow<String?>(null)
    val recommendationsError: StateFlow<String?> = _recommendationsError.asStateFlow()

    // ============== MATCHMAKING STATE ==============
    private val _matches = MutableStateFlow<List<UserMatch>>(emptyList())
    val matches: StateFlow<List<UserMatch>> = _matches.asStateFlow()

    private val _totalMatches = MutableStateFlow(0)
    val totalMatches: StateFlow<Int> = _totalMatches.asStateFlow()

    private val _matchmakingAlgorithm = MutableStateFlow<String?>(null)
    val matchmakingAlgorithm: StateFlow<String?> = _matchmakingAlgorithm.asStateFlow()

    private val _matchmakingLoading = MutableStateFlow(false)
    val matchmakingLoading: StateFlow<Boolean> = _matchmakingLoading.asStateFlow()

    private val _matchmakingError = MutableStateFlow<String?>(null)
    val matchmakingError: StateFlow<String?> = _matchmakingError.asStateFlow()

    // ============== ITINERARY STATE ==============
    private val _itinerary = MutableStateFlow<FlaskItineraryResponse?>(null)
    val itinerary: StateFlow<FlaskItineraryResponse?> = _itinerary.asStateFlow()

    private val _itineraryRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val itineraryRoute: StateFlow<List<LatLng>> = _itineraryRoute.asStateFlow()

    private val _itineraryLoading = MutableStateFlow(false)
    val itineraryLoading: StateFlow<Boolean> = _itineraryLoading.asStateFlow()

    private val _itineraryError = MutableStateFlow<String?>(null)
    val itineraryError: StateFlow<String?> = _itineraryError.asStateFlow()

    // ============== RECOMMENDATIONS FUNCTIONS ==============
    fun loadAiRecommendations(token: String) {
        viewModelScope.launch {
            _recommendationsLoading.value = true
            _recommendationsError.value = null
            try {
                when (val result = repository.getAiRecommendations(token)) {
                    is Result.Success -> {
                        val data = result.data
                        _recommendations.value = data.recommendations ?: emptyList()
                        _userCluster.value = data.userCluster
                    }
                    is Result.Error -> _recommendationsError.value = result.message
                    is Result.Failure -> _recommendationsError.value = result.message.message ?: "Erreur inconnue"
                    else -> Unit
                }
            } catch (e: Exception) {
                _recommendationsError.value = "Exception: ${e.message}"
                Log.e(TAG, "Exception loading recommendations", e)
            } finally {
                _recommendationsLoading.value = false
            }
        }
    }

    fun clearRecommendations() {
        _recommendations.value = emptyList()
        _userCluster.value = null
        _recommendationsError.value = null
    }

    // ============== MATCHMAKING FUNCTIONS ==============
    fun loadMatchmaking(token: String, minSimilarity: Double = 0.05, limit: Int = 10) {
        viewModelScope.launch {
            _matchmakingLoading.value = true
            _matchmakingError.value = null
            try {
                when (val result = repository.getMatchmaking(token, minSimilarity, limit)) {
                    is Result.Success -> {
                        val data = result.data
                        _matches.value = data.matches
                        _totalMatches.value = data.totalMatches
                        _matchmakingAlgorithm.value = data.algorithm
                    }
                    is Result.Error -> _matchmakingError.value = result.message
                    is Result.Failure -> _matchmakingError.value = result.message.message ?: "Erreur inconnue"
                    else -> Unit
                }
            } catch (e: Exception) {
                _matchmakingError.value = "Exception: ${e.message}"
                Log.e(TAG, "Exception loading matchmaking", e)
            } finally {
                _matchmakingLoading.value = false
            }
        }
    }

    fun clearMatchmaking() {
        _matches.value = emptyList()
        _totalMatches.value = 0
        _matchmakingAlgorithm.value = null
        _matchmakingError.value = null
    }

    // ============== ITINERARY FUNCTIONS ==============
    fun generateItinerary(
        token: String,
        startLat: Double,
        startLon: Double,
        startName: String? = null,
        endLat: Double,
        endLon: Double,
        endName: String? = null,
        waypoints: List<FlaskLocationPoint>? = null,
        context: String? = null,
        activityType: String? = null
    ) {
        viewModelScope.launch {
            _itineraryLoading.value = true
            _itineraryError.value = null

            try {
                Log.d(TAG, "Generating AI itinerary...")

                val request = FlaskItineraryRequest(
                    start = FlaskLocationPoint(startLat, startLon, startName),
                    end = FlaskLocationPoint(endLat, endLon, endName),
                    waypoints = waypoints,
                    context = context,
                    activityType = activityType
                )

                when (val result = repository.generateItinerary(token, request)) {
                    is Result.Success -> {
                        val data = result.data
                        _itinerary.value = data

                        // Extraire les coordonnées de la géométrie
                        val coordinates = data.itinerary.geometry.coordinates

                        if (coordinates.isEmpty()) {
                            Log.e(TAG, "Coordonnées vides")
                            _itineraryError.value = "Tracé vide"
                        } else {
                            try {
                                // Convertir les coordonnées [lon, lat, elevation] en LatLng
                                val routePoints = coordinates.mapNotNull { coord ->
                                    if (coord.size >= 2) {
                                        // coord[0] = longitude, coord[1] = latitude
                                        LatLng(coord[1], coord[0])
                                    } else {
                                        null
                                    }
                                }

                                if (routePoints.size > 5) {
                                    _itineraryRoute.value = routePoints
                                    Log.d(TAG, "✅ ROUTE IA CHARGÉE ! ${routePoints.size} points")
                                    Log.d(TAG, "📏 Distance: ${data.itinerary.summary.distance / 1000} km")
                                    Log.d(TAG, "⏱️ Durée: ${data.itinerary.summary.duration / 60} min")
                                } else {
                                    Log.w(TAG, "⚠️ Trop peu de points: ${routePoints.size}")
                                    _itineraryError.value = "Tracé trop court"
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Erreur conversion coordonnées: ${e.message}", e)
                                _itineraryError.value = "Erreur de tracé"
                            }
                        }
                    }
                    is Result.Error -> {
                        _itineraryError.value = result.message
                        Log.e(TAG, "Error: ${result.message}")
                    }
                    is Result.Failure -> {
                        _itineraryError.value = "Erreur réseau"
                        Log.e(TAG, "Failure: ${result.message.message}")
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _itineraryError.value = "Exception: ${e.message}"
                Log.e(TAG, "Exception generating itinerary", e)
            } finally {
                _itineraryLoading.value = false
            }
        }
    }

    fun clearItinerary() {
        _itinerary.value = null
        _itineraryRoute.value = emptyList()
        _itineraryError.value = null
    }

    fun clearErrors() {
        _recommendationsError.value = null
        _matchmakingError.value = null
        _itineraryError.value = null
    }

    fun getFormattedDistance(): String {
        val distance = _itinerary.value?.itinerary?.summary?.distance ?: return "N/A"
        return if (distance < 1000) "${distance.toInt()} m"
        else String.format("%.1f km", distance / 1000)
    }

    fun getFormattedDuration(): String {
        val duration = _itinerary.value?.itinerary?.summary?.duration ?: return "N/A"
        val minutes = (duration / 60).toInt()
        return if (minutes < 60) "$minutes min"
        else "${minutes / 60}h ${minutes % 60}min"
    }
}