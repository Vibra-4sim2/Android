package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.SortieResponse
import com.example.dam.utils.LocalSavedSortiesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les sorties sauvegardées LOCALEMENT (sans backend)
 * Utilise SharedPreferences pour la persistance
 */
class SavedSortiesViewModel : ViewModel() {
    private val TAG = "SavedSortiesViewModel"
    private var localManager: LocalSavedSortiesManager? = null

    private val _savedSorties = MutableStateFlow<List<SortieResponse>>(emptyList())
    val savedSorties: StateFlow<List<SortieResponse>> = _savedSorties.asStateFlow()

    private val _filteredSorties = MutableStateFlow<List<SortieResponse>>(emptyList())
    val filteredSorties: StateFlow<List<SortieResponse>> = _filteredSorties.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _savedSortieIds = MutableStateFlow<Set<String>>(emptySet())
    val savedSortieIds: StateFlow<Set<String>> = _savedSortieIds.asStateFlow()

    /**
     * Initialiser le manager local
     */
    fun initialize(context: Context) {
        if (localManager == null) {
            localManager = LocalSavedSortiesManager(context)
            Log.d(TAG, "✅ LocalSavedSortiesManager initialisé")
        }
    }

    /**
     * Charger toutes les sorties sauvegardées localement
     */
    fun loadSavedSorties(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                initialize(context)

                val sorties = localManager?.getAllSavedSorties() ?: emptyList()
                val ids = localManager?.getSavedSortieIds() ?: emptySet()

                _savedSorties.value = sorties
                _filteredSorties.value = sorties
                _savedSortieIds.value = ids

                Log.d(TAG, "✅ Chargé ${sorties.size} sorties sauvegardées localement")
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de chargement: ${e.message}"
                Log.e(TAG, "❌ Erreur: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Rafraîchir les sorties sauvegardées
     */
    fun refresh(context: Context) {
        loadSavedSorties(context)
    }

    /**
     * Rechercher des sorties par titre
     */
    fun searchSorties(query: String) {
        _searchQuery.value = query

        if (query.isEmpty()) {
            _filteredSorties.value = _savedSorties.value
        } else {
            _filteredSorties.value = _savedSorties.value.filter { sortie ->
                sortie.titre.contains(query, ignoreCase = true) ||
                sortie.description.contains(query, ignoreCase = true) ||
                sortie.type.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Sauvegarder une sortie localement
     */
    fun saveSortie(context: Context, sortie: SortieResponse) {
        viewModelScope.launch {
            try {
                initialize(context)

                val success = localManager?.saveSortie(sortie) ?: false

                if (success) {
                    // Recharger la liste
                    loadSavedSorties(context)
                    Log.d(TAG, "✅ Sortie ${sortie.titre} sauvegardée")
                } else {
                    _errorMessage.value = "Cette sortie est déjà sauvegardée"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de sauvegarde: ${e.message}"
                Log.e(TAG, "❌ Erreur: ${e.message}", e)
            }
        }
    }

    /**
     * Supprimer une sortie sauvegardée
     */
    fun removeSavedSortie(context: Context, sortieId: String) {
        viewModelScope.launch {
            try {
                initialize(context)

                val removed = localManager?.removeSortie(sortieId) ?: false

                if (removed) {
                    // Mettre à jour la liste localement
                    _savedSorties.value = _savedSorties.value.filter { it.id != sortieId }
                    _filteredSorties.value = _filteredSorties.value.filter { it.id != sortieId }
                    _savedSortieIds.value = _savedSortieIds.value.minus(sortieId)

                    Log.d(TAG, "✅ Sortie $sortieId supprimée")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de suppression: ${e.message}"
                Log.e(TAG, "❌ Erreur: ${e.message}", e)
            }
        }
    }

    /**
     * Vérifier si une sortie est sauvegardée
     */
    fun isSortieSaved(sortieId: String): Boolean {
        return sortieId in _savedSortieIds.value
    }

    /**
     * Obtenir les IDs des sorties sauvegardées
     */
    fun getSavedSortieIds(context: Context): Set<String> {
        initialize(context)
        return localManager?.getSavedSortieIds() ?: emptySet()
    }

    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

