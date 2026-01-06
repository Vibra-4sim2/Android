package com.example.dam.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.SortieResponse
import com.example.dam.remote.ORSRequest
import com.example.dam.remote.OpenRouteServiceInstance
import com.example.dam.repository.AdventureRepository
import com.example.dam.utils.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SortieDetailViewModel : ViewModel() {
    private val repo = AdventureRepository()

    var sortie by mutableStateOf<SortieResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Load sortie details by ID
     */
    fun loadSortieDetail(sortieId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val result = repo.getSortieById(sortieId)) {
                is Result.Success -> {
                    sortie = result.data
                    Log.d("SORTIE_DETAIL", "Loaded sortie: ${result.data.titre}")
                }
                is Result.Error -> {
                    errorMessage = result.message
                    Log.e("SORTIE_DETAIL", "Error: ${result.message}")
                }
                else -> {}
            }

            isLoading = false
        }
    }

    /**
     * Fetches route between two coordinates using OpenRouteService
     */
    suspend fun loadRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ORS_ROUTE", "Requesting route...")

                val request = ORSRequest(
                    coordinates = listOf(
                        listOf(startLng, startLat), // ORS expects [lng, lat]
                        listOf(endLng, endLat)
                    )
                )

                val response = OpenRouteServiceInstance.api.getDirections(
                    profile = "driving-car",
                    body = request
                )

                val coords = response.features[0].geometry.coordinates

                Log.d("ORS_ROUTE", "Route received: ${coords.size} points")

                coords.map { point ->
                    LatLng(point[1], point[0]) // Convert to Google Map LatLng
                }

            } catch (e: Exception) {
                Log.e("ORS_ROUTE", "Error fetching route", e)
                emptyList()
            }
        }
    }
}
