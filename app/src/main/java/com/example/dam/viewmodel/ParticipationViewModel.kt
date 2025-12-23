package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.ParticipationResponse
import com.example.dam.repository.ParticipationRepository
import com.example.dam.repository.MyResult
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

    /**
     * Load participations for a sortie
     */
    fun loadParticipations(sortieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Clear previous errors

            try {
                Log.d("ParticipationVM", "Loading participations for sortie: $sortieId")

                when (val result = repository.getParticipations(sortieId)) {
                    is MyResult.Success -> {
                        _participations.value = result.data
                        Log.d("ParticipationVM", "✅ Loaded ${result.data.size} participations")
                    }
                    is MyResult.Failure -> {
                        val errorMsg = result.error.message ?: "Unknown error"
                        _errorMessage.value = errorMsg
                        Log.e("ParticipationVM", "❌ Error loading participations: $errorMsg")
                    }
                    is MyResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Erreur: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("ParticipationVM", "❌ Exception loading participations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Join sortie (create participation)
     */
    fun joinSortie(sortieId: String, token: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("ParticipationVM", "Attempting to join sortie: $sortieId")

                when (val result = repository.createParticipation(sortieId, token)) {
                    is MyResult.Success -> {
                        _hasJoined.value = true
                        _successMessage.value = "Demande envoyée avec succès!"
                        loadParticipations(sortieId)
                        onSuccess()
                        Log.d("ParticipationVM", "✅ Successfully joined sortie")
                    }
                    is MyResult.Failure -> {
                        val errorMsg = result.error.message ?: "Unknown error"
                        val message = when {
                            errorMsg.contains("already participates", ignoreCase = true) ->
                                "Vous avez déjà rejoint cette sortie"
                            errorMsg.contains("full capacity", ignoreCase = true) ->
                                "Cette sortie est complète"
                            errorMsg.contains("401") ->
                                "Non autorisé. Veuillez vous reconnecter"
                            else -> errorMsg
                        }
                        _errorMessage.value = message
                        Log.e("ParticipationVM", "❌ Error joining: $errorMsg")
                    }
                    is MyResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur réseau: ${e.message}"
                Log.e("ParticipationVM", "❌ Exception joining sortie", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Accept participation request
     */
    fun acceptParticipation(participationId: String, sortieId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("ParticipationVM", "Accepting participation: $participationId")

                when (val result = repository.updateParticipationStatus(participationId, "ACCEPTEE", token)) {
                    is MyResult.Success -> {
                        _successMessage.value = "Demande acceptée!"
                        loadParticipations(sortieId)
                        Log.d("ParticipationVM", "✅ Accepted participation")
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = result.error.message
                        Log.e("ParticipationVM", "❌ Error accepting: ${result.error.message}")
                    }
                    is MyResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                Log.e("ParticipationVM", "❌ Exception accepting participation", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refuse participation request
     */
    fun refuseParticipation(participationId: String, sortieId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("ParticipationVM", "Refusing participation: $participationId")

                when (val result = repository.updateParticipationStatus(participationId, "REFUSEE", token)) {
                    is MyResult.Success -> {
                        _successMessage.value = "Demande refusée"
                        loadParticipations(sortieId)
                        Log.d("ParticipationVM", "✅ Refused participation")
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = result.error.message
                        Log.e("ParticipationVM", "❌ Error refusing: ${result.error.message}")
                    }
                    is MyResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                Log.e("ParticipationVM", "❌ Exception refusing participation", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancel own participation
     */
    fun cancelParticipation(participationId: String, sortieId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("ParticipationVM", "Cancelling participation: $participationId")

                when (val result = repository.cancelParticipation(participationId, token)) {
                    is MyResult.Success -> {
                        _hasJoined.value = false
                        _successMessage.value = "Participation annulée"
                        loadParticipations(sortieId)
                        Log.d("ParticipationVM", "✅ Cancelled participation")
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = result.error.message
                        Log.e("ParticipationVM", "❌ Error cancelling: ${result.error.message}")
                    }
                    is MyResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                Log.e("ParticipationVM", "❌ Exception cancelling participation", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check if current user has joined
     */
    fun checkIfJoined(sortieId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val hasJoined = _participations.value.any {
                    it.userId._id == currentUserId && it.status != "REFUSEE"
                }
                _hasJoined.value = hasJoined
                Log.d("ParticipationVM", "User has joined: $hasJoined")
            } catch (e: Exception) {
                Log.e("ParticipationVM", "Error checking if joined", e)
            }
        }
    }

    /**
     * Get pending requests count
     */
    fun getPendingCount(): Int {
        return try {
            _participations.value.count { it.status == "EN_ATTENTE" }
        } catch (e: Exception) {
            Log.e("ParticipationVM", "Error getting pending count", e)
            0
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Reset state (useful when leaving screen)
     */
    fun resetState() {
        _participations.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = null
        _successMessage.value = null
        _hasJoined.value = false
    }
}