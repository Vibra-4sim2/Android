// viewmodel/MessagesViewModel.kt
package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.ChatGroupUI
import com.example.dam.models.toChatGroupUI
import com.example.dam.remote.RetrofitInstance
import com.example.dam.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'état des chats
 */
class MessagesViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val TAG = "MessagesViewModel"

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Liste des chats
    private val _chatGroups = MutableStateFlow<List<ChatGroupUI>>(emptyList())
    val chatGroups: StateFlow<List<ChatGroupUI>> = _chatGroups.asStateFlow()

    // Message d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Récupère tous les chats de l'utilisateur via les sorties
     * Solution temporaire en attendant l'endpoint /my-chats
     * @param context Context pour récupérer le token
     */
    fun loadUserChats(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Récupérer le token depuis SharedPreferences
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé. Veuillez vous reconnecter."
                    _isLoading.value = false
                    return@launch
                }

                // Récupérer l'ID utilisateur
                val userId = getUserId(context)
                if (userId.isNullOrEmpty()) {
                    _errorMessage.value = "ID utilisateur non trouvé."
                    _isLoading.value = false
                    return@launch
                }

                // SOLUTION TEMPORAIRE : Récupérer toutes les sorties de l'utilisateur
                // puis récupérer les chats associés
                loadChatsFromSorties(token, userId)

            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                _errorMessage.value = "Une erreur s'est produite"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge les chats en passant par les sorties
     */
    private suspend fun loadChatsFromSorties(token: String, userId: String) {
        try {
            Log.d(TAG, "🔍 Début chargement chats via sorties")
            Log.d(TAG, "Token: ${token.take(20)}...")
            Log.d(TAG, "UserId: $userId")

            // Récupérer toutes les sorties
            val sortiesResponse = RetrofitInstance.adventureApi.getAllSorties()

            if (!sortiesResponse.isSuccessful || sortiesResponse.body() == null) {
                _errorMessage.value = "Impossible de récupérer les sorties"
                Log.e(TAG, "❌ Erreur sorties: ${sortiesResponse.code()}")
                return
            }

            val sorties = sortiesResponse.body()!!
            Log.d(TAG, "✅ ${sorties.size} sorties récupérées")

            // Filtrer les sorties où l'utilisateur est créateur ou participant
            val mySorties = sorties.filter { sortie ->
                val isCreator = sortie.createurId.id == userId
                val isParticipant = sortie.participants.any {
                    it.userId == userId && (it.status == "ACCEPTEE" || it.status == "EN_ATTENTE")
                }
                Log.d(TAG, "Sortie ${sortie.titre}: creator=$isCreator, participant=$isParticipant")
                isCreator || isParticipant
            }

            Log.d(TAG, "📌 ${mySorties.size} sorties appartenant à l'utilisateur")

            // Récupérer les chats pour chaque sortie
            val chatsList = mutableListOf<com.example.dam.models.ChatGroupUI>()

            for (sortie in mySorties) {
                Log.d(TAG, "🔎 Recherche chat pour sortie: ${sortie.id}")
                val chatResult = chatRepository.getChatBySortie(sortie.id, "Bearer $token")

                chatResult.fold(
                    onSuccess = { chat ->
                        Log.d(TAG, "✅ Chat trouvé: ${chat.id}")
                        chatsList.add(chat.toChatGroupUI(userId))
                    },
                    onFailure = { error ->
                        Log.w(TAG, "⚠️ Chat non trouvé pour sortie ${sortie.id}: ${error.message}")
                    }
                )
            }

            _chatGroups.value = chatsList
            Log.d(TAG, "🎉 ${chatsList.size} chats chargés avec succès")

        } catch (e: Exception) {
            Log.e(TAG, "💥 Erreur critique: ${e.message}", e)
            _errorMessage.value = "Erreur lors du chargement des discussions"
        }
    }

    /**
     * Récupère le chat d'une sortie spécifique
     * @param sortieId ID de la sortie
     * @param context Context pour récupérer le token
     */
    fun loadChatBySortie(sortieId: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isLoading.value = false
                    return@launch
                }

                val userId = getUserId(context)
                if (userId.isNullOrEmpty()) {
                    _errorMessage.value = "ID utilisateur non trouvé"
                    _isLoading.value = false
                    return@launch
                }

                val result = chatRepository.getChatBySortie(sortieId, "Bearer $token")

                result.fold(
                    onSuccess = { chat ->
                        Log.d(TAG, "Chat loaded for sortie: $sortieId")
                        // Ajouter à la liste (ou remplacer si existe déjà)
                        val currentList = _chatGroups.value.toMutableList()
                        val chatUI = chat.toChatGroupUI(userId)
                        currentList.add(chatUI)
                        _chatGroups.value = currentList
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading chat: ${error.message}")
                        _errorMessage.value = error.message ?: "Erreur inconnue"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                _errorMessage.value = "Une erreur s'est produite"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Réinitialise le message d'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }

    // ========== HELPERS ==========

    /**
     * Récupère le token JWT depuis SharedPreferences
     * Utilise UserPreferences pour être cohérent avec le reste de l'app
     */
    private fun getToken(context: Context): String? {
        // Utiliser le nom correct des SharedPreferences
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        Log.d(TAG, "Token récupéré: ${if (token != null) "✅ Trouvé (${token.take(20)}...)" else "❌ Non trouvé"}")
        return token
    }

    /**
     * Récupère l'ID utilisateur depuis SharedPreferences
     */
    private fun getUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)

        Log.d(TAG, "UserId récupéré: ${if (userId != null) "✅ $userId" else "❌ Non trouvé"}")
        return userId
    }
}
