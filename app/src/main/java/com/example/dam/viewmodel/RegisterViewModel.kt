package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.repository.AuthRepository
import com.example.dam.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * État de l'interface de registration
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel pour gérer la logique du RegisterScreen
 */
class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "RegisterViewModel"

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Valider les champs avant l'inscription
     */
    private fun validateFields(
        firstName: String,
        lastName: String,
        Gender: String,
        birthDate: String,  // ✅ NEW PARAMETER
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            firstName.isBlank() -> "First name is required"
            lastName.isBlank() -> "Last name is required"
            Gender.isBlank() -> "Gender is required"
//            !Gender.lowercase().matches(Regex("^(male|female|other)$")) ->
//                "Gender must be 'male', 'female', or 'other'"
            birthDate.isBlank() -> "Birth date is required"  // ✅ NEW VALIDATION

            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Invalid email format"
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    /**
     * Fonction pour créer un compte
     * ✅ UPDATED: Added birthDate parameter
     */
    fun register(
        firstName: String,
        lastName: String,
        Gender: String,
        birthDate: String,  // ✅ NEW PARAMETER
        email: String,
        password: String,
        confirmPassword: String
    ) {
        Log.d(TAG, "========== REGISTRATION ATTEMPT ==========")
        Log.d(TAG, "firstName: '$firstName'")
        Log.d(TAG, "lastName: '$lastName'")
        Log.d(TAG, "Gender: '$Gender'")
        Log.d(TAG, "birthDate: '$birthDate'")  // ✅ NEW LOG
        Log.d(TAG, "email: '$email'")
        Log.d(TAG, "==========================================")

        // Valider les champs
        val validationError = validateFields(
            firstName, lastName, Gender, birthDate, email, password, confirmPassword  // ✅ UPDATED
        )

        if (validationError != null) {
            Log.e(TAG, "❌ Validation error: $validationError")
            _uiState.value = RegisterUiState(error = validationError)
            return
        }

        // Lancer l'appel API
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            Log.d(TAG, "📤 Starting registration API call...")

            when (val result = repository.register(
                firstName = firstName,
                lastName = lastName,
                Gender = Gender,
                birthDate = birthDate,  // ✅ NEW PARAMETER
                email = email,
                password = password
            )) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Registration successful! User ID: ${result.data.id}")
                    _uiState.value = RegisterUiState(
                        isLoading = false,
                        userId = result.data.id,
                        isSuccess = true
                    )
                }

                is Result.Error -> {
                    Log.e(TAG, "❌ Registration failed: ${result.message}")
                    _uiState.value = RegisterUiState(
                        isLoading = false,
                        error = result.message
                    )
                }

                is Result.Loading -> {
                    Log.d(TAG, "⏳ Still loading...")
                    _uiState.value = RegisterUiState(isLoading = true)
                }
            }
        }
    }

    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        Log.d(TAG, "Clearing error")
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Réinitialiser l'état
     */
    fun resetState() {
        Log.d(TAG, "Resetting state")
        _uiState.value = RegisterUiState()
    }
}

/*
INSTRUCTIONS D'UTILISATION :
1. Créez un fichier RegisterViewModel.kt dans com.example.dam.viewmodel
2. Collez ce code
3. Synchronisez Gradle

COMMENT L'UTILISER DANS REGISTERSCREEN :
val viewModel: RegisterViewModel = viewModel()
val uiState by viewModel.uiState.collectAsState()

// Appeler l'inscription
viewModel.register(firstName, lastName, gender, email, password, confirmPassword)

// Observer les changements
LaunchedEffect(uiState.isSuccess) {
    if (uiState.isSuccess) {
        navController.navigate("activities_selection")
    }
}

// Afficher les erreurs
if (uiState.error != null) {
    AlertDialog(...)
}

// Afficher le loading
if (uiState.isLoading) {
    CircularProgressIndicator()
}
*/