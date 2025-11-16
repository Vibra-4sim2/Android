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

class SortieDetailViewModel : ViewModel() {
    private val repo = AdventureRepository()

    var sortie by mutableStateOf<SortieResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

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
}