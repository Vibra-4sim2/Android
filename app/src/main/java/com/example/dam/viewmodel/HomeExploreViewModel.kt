package com.example.dam.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.SortieResponse
import com.example.dam.repository.AdventureRepository
import com.example.dam.utils.Result
import kotlinx.coroutines.launch

class HomeExploreViewModel : ViewModel() {
    private val repo = AdventureRepository()

    var sorties by mutableStateOf<List<SortieResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedFilter by mutableStateOf("explore")
    var searchQuery by mutableStateOf("")

    init {
        loadSorties()
    }

    fun loadSorties() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val result = repo.getAllSorties()) {
                is Result.Success -> {
                    sorties = result.data
                    Log.d("HOME_EXPLORE", "Loaded ${sorties.size} sorties")
                }
                is Result.Error -> {
                    errorMessage = result.message
                    Log.e("HOME_EXPLORE", "Error: ${result.message}")
                }
                else -> {}
            }

            isLoading = false
        }
    }

    fun getFilteredSorties(): List<SortieResponse> {
        var filtered = sorties

        // Filter by type
        filtered = when (selectedFilter) {
            "cycling" -> filtered.filter { it.type == "VELO" }
            "hiking" -> filtered.filter { it.type == "RANDONNEE" }
            "camping" -> filtered.filter { it.optionCamping == true }  // ✅ MODIFIÉ : filtre par option camping
            else -> filtered
        }

        // Filter by search query
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.titre.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.itineraire?.pointArrivee?.address?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        return filtered
    }

    fun setFilter(filter: String) {
        selectedFilter = filter
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun refresh() {
        loadSorties()
    }
}