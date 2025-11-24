package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.ParticipationResponse
import com.example.dam.repository.ParticipationRepository
import com.example.dam.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ParticipationViewModel : ViewModel() {

    private val repository = ParticipationRepository()

    private val _participations = MutableStateFlow<List<ParticipationResponse>>(emptyList())
    val participations: StateFlow<List<ParticipationResponse>> = _participations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _hasJoined = MutableStateFlow(false)
    val hasJoined: StateFlow<Boolean> = _hasJoined.asStateFlow()

    // Load participations for a sortie
    fun loadParticipations(sortieId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getParticipations(sortieId)) {
                is Result.Success -> {
                    _participations.value = result.data
                    Log.d("ParticipationVM", "✅ Loaded ${result.data.size} participations")
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    Log.e("ParticipationVM", "❌ Error: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Join sortie (create participation)
    fun joinSortie(sortieId: String, token: String, onSuccess: () -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.createParticipation(sortieId, token)) {
                is Result.Success -> {
                    _hasJoined.value = true
                    _successMessage.value = "Demande envoyée avec succès!"
                    loadParticipations(sortieId) // Reload list
                    onSuccess()
                    Log.d("ParticipationVM", "✅ Joined sortie")
                }
                is Result.Error -> {
                    val message = when {
                        result.message.contains("already participates") ->
                            "Vous avez déjà rejoint cette sortie"
                        result.message.contains("full capacity") ->
                            "Cette sortie est complète"
                        else -> result.message
                    }
                    _errorMessage.value = message
                    Log.e("ParticipationVM", "❌ Error joining: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Accept participation request
    fun acceptParticipation(participationId: String, sortieId: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.updateParticipationStatus(participationId, "ACCEPTEE", token)) {
                is Result.Success -> {
                    _successMessage.value = "Demande acceptée!"
                    loadParticipations(sortieId) // Reload list
                    Log.d("ParticipationVM", "✅ Accepted participation")
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    Log.e("ParticipationVM", "❌ Error accepting: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Refuse participation request
    fun refuseParticipation(participationId: String, sortieId: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.updateParticipationStatus(participationId, "REFUSEE", token)) {
                is Result.Success -> {
                    _successMessage.value = "Demande refusée"
                    loadParticipations(sortieId) // Reload list
                    Log.d("ParticipationVM", "✅ Refused participation")
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    Log.e("ParticipationVM", "❌ Error refusing: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Cancel own participation
    fun cancelParticipation(participationId: String, sortieId: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.cancelParticipation(participationId, token)) {
                is Result.Success -> {
                    _hasJoined.value = false
                    _successMessage.value = "Participation annulée"
                    loadParticipations(sortieId) // Reload list
                    Log.d("ParticipationVM", "✅ Cancelled participation")
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    Log.e("ParticipationVM", "❌ Error cancelling: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Check if current user has joined
    fun checkIfJoined(sortieId: String, currentUserId: String) {
        viewModelScope.launch {
            val hasJoined = _participations.value.any {
                it.userId._id == currentUserId && it.status != "REFUSEE"
            }
            _hasJoined.value = hasJoined
        }
    }

    // Get pending requests count
    fun getPendingCount(): Int {
        return _participations.value.count { it.status == "EN_ATTENTE" }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}