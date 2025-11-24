package com.example.dam.viewmodel

import android.content.Context
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val accessToken: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel pour gérer la logique du LoginScreen
 * Gère l'état et appelle le repository
 */
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private var _accessToken: String = ""

    /**
     * Fonction pour se connecter
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                error = "Please fill all fields"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        accessToken = result.data.accessToken,
                        isSuccess = true
                    )
                }

                is Result.Error -> {
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
     * ✅ Authentification avec Google
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
     * ✅ AJOUTÉ: Fonction de logout avec nettoyage du chat
     */
    fun logout(context: Context, chatViewModel: com.example.dam.viewmodel.ChatViewModel) {
        Log.d(TAG, "========== LOGOUT ==========")
        Log.d(TAG, "🔴 Starting logout process")

        // ✅ 1. Nettoyer le chat et déconnecter le socket
        chatViewModel.disconnect()
        Log.d(TAG, "✅ Chat disconnected")

        // ✅ 2. Supprimer les tokens
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        val tokenBefore = sharedPref.getString("auth_token", null)
        Log.d(TAG, "🔑 Token avant: ${tokenBefore?.take(20)}")

        sharedPref.edit().apply {
            remove("auth_token")
            remove("user_id")
            apply()
        }

        val tokenAfter = sharedPref.getString("auth_token", null)
        Log.d(TAG, "🔑 Token après: $tokenAfter")

        // ✅ 3. Réinitialiser l'état du ViewModel
        _uiState.value = LoginUiState()
        _accessToken = ""

        Log.d(TAG, "✅ Logout complete")
        Log.d(TAG, "============================")
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

    fun getAccessToken(): String {
        return _uiState.value.accessToken ?: ""
    }
}