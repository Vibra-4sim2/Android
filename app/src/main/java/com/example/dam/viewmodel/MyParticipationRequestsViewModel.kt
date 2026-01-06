package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.ParticipationResponse
import com.example.dam.models.SortieResponse
import com.example.dam.repository.ParticipationRepository
import com.example.dam.repository.UserProfileRepository
import com.example.dam.repository.MyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer toutes les demandes de participation
 * aux sorties créées par l'utilisateur courant
 */
class MyParticipationRequestsViewModel : ViewModel() {

    private val participationRepository = ParticipationRepository()
    private val userProfileRepository = UserProfileRepository()

    // Liste des demandes avec les infos de la sortie
    data class ParticipationWithSortie(
        val participation: ParticipationResponse,
        val sortie: SortieResponse
    )

    private val _allRequests = MutableStateFlow<List<ParticipationWithSortie>>(emptyList())
    val allRequests: StateFlow<List<ParticipationWithSortie>> = _allRequests.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Charger toutes les demandes de participation pour les sorties de l'utilisateur
     * ✅ Optimisé avec appels parallèles pour un chargement plus rapide
     */
    fun loadAllRequestsForUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("MyParticipationVM", "📥 Loading requests for user: $userId")

                // 1. Récupérer les sorties créées par l'utilisateur
                when (val sortiesResult = userProfileRepository.getUserSorties(userId)) {
                    is MyResult.Success -> {
                        val mySorties = sortiesResult.data
                        Log.d("MyParticipationVM", "✅ Found ${mySorties.size} sorties")

                        // ✅ Charger les participations en parallèle pour chaque sortie
                        val allRequestsWithSorties = coroutineScope {
                            val deferredResults = mySorties.map { sortie ->
                                async {
                                    when (val participationsResult = participationRepository.getParticipations(sortie.id)) {
                                        is MyResult.Success -> {
                                            participationsResult.data.map { participation ->
                                                ParticipationWithSortie(participation, sortie)
                                            }
                                        }
                                        is MyResult.Failure -> {
                                            Log.e("MyParticipationVM", "❌ Error loading participations for ${sortie.id}")
                                            emptyList()
                                        }
                                        else -> emptyList()
                                    }
                                }
                            }
                            // ✅ Attendre tous les résultats en parallèle
                            deferredResults.awaitAll().flatten()
                        }

                        // Compter les demandes en attente
                        val pendingTotal = allRequestsWithSorties.count { it.participation.status == "EN_ATTENTE" }

                        // Trier par date de création (plus récent en premier)
                        _allRequests.value = allRequestsWithSorties.sortedByDescending {
                            it.participation.createdAt
                        }
                        _pendingCount.value = pendingTotal

                        Log.d("MyParticipationVM", "✅ Total: ${allRequestsWithSorties.size} requests, $pendingTotal pending")
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = sortiesResult.error.message
                        Log.e("MyParticipationVM", "❌ Error loading sorties: ${sortiesResult.error.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                Log.e("MyParticipationVM", "❌ Exception", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Accepter une demande
     */
    fun acceptParticipation(participationId: String, sortieId: String, token: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = participationRepository.updateParticipationStatus(participationId, "ACCEPTEE", token)) {
                    is MyResult.Success -> {
                        _successMessage.value = "Demande acceptée!"
                        loadAllRequestsForUser(userId)
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = result.error.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refuser une demande
     */
    fun refuseParticipation(participationId: String, sortieId: String, token: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = participationRepository.updateParticipationStatus(participationId, "REFUSEE", token)) {
                    is MyResult.Success -> {
                        _successMessage.value = "Demande refusée"
                        loadAllRequestsForUser(userId)
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = result.error.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}

