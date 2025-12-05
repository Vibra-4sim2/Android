package com.example.dam.viewmodel

// viewmodel/RecommendationsViewModel.kt
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.SortieResponse
import com.example.dam.models.SmartMatch
import com.example.dam.repository.AdventureRepository
import com.example.dam.utils.Result
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les recommandations personnalisées
 * Charge les données depuis l'API et les filtre selon le type de recommandation
 */
class RecommendationsViewModel : ViewModel() {
    private val repo = AdventureRepository()

    // État: toutes les recommandations chargées depuis l'API
    var allRecommendations by mutableStateOf<List<SortieResponse>>(emptyList())
        private set

    // État: chargement en cours
    var isLoading by mutableStateOf(false)
        private set

    // État: message d'erreur (null si pas d'erreur)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // État: cluster de l'utilisateur (vient de l'API)
    var userCluster by mutableStateOf<Int?>(null)
        private set

    /**
     * Charge les recommandations depuis l'API
     * Utilise le userId et le token stockés dans UserPreferences
     */
    fun loadRecommendations(context: Context) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Récupérer userId et token depuis SharedPreferences
            val userId = UserPreferences.getUserId(context)
            val token = UserPreferences.getToken(context)

            // Vérifier que l'utilisateur est authentifié
            if (userId == null || token == null) {
                errorMessage = "User not authenticated"
                isLoading = false
                Log.e("RECOMMENDATIONS_VM", "❌ Missing userId or token")
                return@launch
            }

            Log.d("RECOMMENDATIONS_VM", "Loading recommendations for userId: $userId")

            // Appel à l'API via le repository
            when (val result = repo.getRecommendationsForUser(userId, token)) {
                is Result.Success -> {
                    allRecommendations = result.data.recommendations
                    userCluster = result.data.userCluster
                    Log.d("RECOMMENDATIONS_VM", "✅ Loaded ${allRecommendations.size} recommendations")
                    Log.d("RECOMMENDATIONS_VM", "User cluster: $userCluster")
                }
                is Result.Error -> {
                    errorMessage = result.message
                    Log.e("RECOMMENDATIONS_VM", "❌ Error: ${result.message}")
                }
                else -> {}
            }

            isLoading = false
        }
    }

    /**
     * PREFERENCE-BASED RECOMMENDATIONS
     * Retourne les 5 premières recommandations (basées sur le cluster de l'utilisateur)
     */
    fun getPreferenceBasedRecommendations(): List<SortieResponse> {
        return allRecommendations.take(5)
    }

    /**
     * WEATHER-BASED RECOMMENDATIONS
     * Filtre les sorties adaptées à la météo actuelle
     * Exclut les sorties de type CAMPING (simulation)
     */
    fun getWeatherBasedRecommendations(): List<SortieResponse> {
        return allRecommendations
            .filter { it.type != "CAMPING" }
            .take(5)
    }

    /**
     * SMART MATCHES
     * Génère des recommandations avec scores de match
     * Score décroissant : 95%, 90%, 85%, 80%, 75%
     */
    fun getSmartMatches(): List<SmartMatch> {
        return allRecommendations.take(5).mapIndexed { index, sortie ->
            SmartMatch(
                sortie = sortie,
                matchScore = 95 - (index * 5) // 95, 90, 85, 80, 75
            )
        }
    }

    /**
     * Rafraîchit les recommandations
     */
    fun refresh(context: Context) {
        loadRecommendations(context)
    }
}