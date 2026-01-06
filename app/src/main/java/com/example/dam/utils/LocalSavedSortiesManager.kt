package com.example.dam.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dam.models.SortieResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gestionnaire local pour les sorties sauvegardées
 * Utilise SharedPreferences pour la persistance locale
 */
class LocalSavedSortiesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "saved_sorties_local",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val TAG = "LocalSavedSorties"

    companion object {
        private const val KEY_SAVED_SORTIES = "saved_sorties_list"
        private const val KEY_SAVED_SORTIE_IDS = "saved_sortie_ids"
    }

    /**
     * Sauvegarder une sortie localement
     */
    fun saveSortie(sortie: SortieResponse): Boolean {
        return try {
            val currentSorties = getAllSavedSorties().toMutableList()

            // Vérifier si déjà sauvegardée
            if (currentSorties.any { it.id == sortie.id }) {
                Log.d(TAG, "⚠️ Sortie ${sortie.id} déjà sauvegardée")
                return false
            }

            // Ajouter la nouvelle sortie
            currentSorties.add(sortie)

            // Sauvegarder dans SharedPreferences
            val json = gson.toJson(currentSorties)
            prefs.edit().putString(KEY_SAVED_SORTIES, json).apply()

            // Mettre à jour la liste des IDs
            updateSavedIds()

            Log.d(TAG, "✅ Sortie ${sortie.titre} sauvegardée localement")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la sauvegarde: ${e.message}", e)
            false
        }
    }

    /**
     * Supprimer une sortie sauvegardée
     */
    fun removeSortie(sortieId: String): Boolean {
        return try {
            val currentSorties = getAllSavedSorties().toMutableList()
            val removed = currentSorties.removeAll { it.id == sortieId }

            if (removed) {
                val json = gson.toJson(currentSorties)
                prefs.edit().putString(KEY_SAVED_SORTIES, json).apply()
                updateSavedIds()
                Log.d(TAG, "✅ Sortie $sortieId supprimée localement")
            }

            removed
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la suppression: ${e.message}", e)
            false
        }
    }

    /**
     * Récupérer toutes les sorties sauvegardées
     */
    fun getAllSavedSorties(): List<SortieResponse> {
        return try {
            val json = prefs.getString(KEY_SAVED_SORTIES, null) ?: return emptyList()
            val type = object : TypeToken<List<SortieResponse>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la lecture: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Vérifier si une sortie est sauvegardée
     */
    fun isSortieSaved(sortieId: String): Boolean {
        val ids = getSavedSortieIds()
        return sortieId in ids
    }

    /**
     * Récupérer uniquement les IDs des sorties sauvegardées (pour performance)
     */
    fun getSavedSortieIds(): Set<String> {
        return try {
            val json = prefs.getString(KEY_SAVED_SORTIE_IDS, null) ?: return emptySet()
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type) ?: emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la lecture des IDs: ${e.message}", e)
            emptySet()
        }
    }

    /**
     * Mettre à jour la liste des IDs (optimisation)
     */
    private fun updateSavedIds() {
        val ids = getAllSavedSorties().map { it.id }.toSet()
        val json = gson.toJson(ids)
        prefs.edit().putString(KEY_SAVED_SORTIE_IDS, json).apply()
    }

    /**
     * Rechercher des sorties sauvegardées
     */
    fun searchSavedSorties(query: String): List<SortieResponse> {
        return getAllSavedSorties().filter { sortie ->
            sortie.titre.contains(query, ignoreCase = true) ||
            sortie.description.contains(query, ignoreCase = true) ||
            sortie.type.contains(query, ignoreCase = true)
        }
    }

    /**
     * Compter les sorties sauvegardées
     */
    fun getSavedCount(): Int {
        return getSavedSortieIds().size
    }

    /**
     * Effacer toutes les sorties sauvegardées
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        Log.d(TAG, "🗑️ Toutes les sorties sauvegardées ont été effacées")
    }
}

