package com.example.dam.viewmodel

// Fichier: app/src/main/java/com/example/dam/viewmodel/LoginViewModel.kt


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.repository.AuthRepository
import com.example.dam.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log


/**
 * État de l'interface de login
 */
data class LoginUiState(
    val isLoading: Boolean = false,        // Est-ce qu'on charge ?
    val error: String? = null,             // Message d'erreur s'il y en a
    val accessToken: String? = null,       // Token JWT reçu
    val isSuccess: Boolean = false         // Login réussi ?




)

/**
 * ViewModel pour gérer la logique du LoginScreen
 * Gère l'état et appelle le repository
 */
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {


    private val TAG = "LoginViewModel"

    // État privé modifiable
    private val _uiState = MutableStateFlow(LoginUiState())

    // État public en lecture seule pour l'UI
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private var _accessToken: String = ""

    /**
     * Fonction pour se connecter
     *
     * @param email Email saisi par l'utilisateur
     * @param password Mot de passe saisi
     */
    fun login(email: String, password: String) {
        // Vérifications basiques
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                error = "Please fill all fields"
            )
            return
        }

        // Lancer l'appel API dans une coroutine
        viewModelScope.launch {
            // 1. Afficher le loading
            _uiState.value = LoginUiState(isLoading = true)

            // 2. Appeler le repository
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    // ✅ Succès
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        accessToken = result.data.accessToken,
                        isSuccess = true
                    )
                }

                is Result.Error -> {
                    // ❌ Erreur
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        error = result.message
                    )
                }

                is Result.Loading -> {
                    // En chargement (normalement pas utilisé ici)
                    _uiState.value = LoginUiState(isLoading = true)
                }
            }
        }
    }







    /**
     * ✅ NOUVEAU: Authentification avec Google
     */
    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            Log.d(TAG, "========== GOOGLE SIGN-IN VIEWMODEL ==========")
            Log.d(TAG, "🔵 Starting Google Sign-In with token")
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = repository.googleSignIn(idToken)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Google Sign-In successful in ViewModel")
                    _accessToken = result.data.accessToken
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        accessToken = result.data.accessToken,
                        isSuccess = true
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Google Sign-In failed: ${result.message}")
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState(isLoading = true)
                }
            }
        }
    }





    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Réinitialiser l'état
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }


    // Add this to your LoginViewModel class
    fun getAccessToken(): String {
        return _uiState.value.accessToken ?: ""
    }
}

/*
COMMENT CRÉER CE FICHIER :
1. Clic droit sur java/com/example/dam
2. New → Package
3. Nommez : viewmodel
4. Clic droit sur viewmodel
5. New → Kotlin Class/File
6. Nommez : LoginViewModel
7. Collez ce code
*/