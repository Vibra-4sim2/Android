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

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val codeSent: Boolean = false,
    val codeVerified: Boolean = false,
    val passwordReset: Boolean = false,
    val email: String = "",
    val verificationCode: String = ""
)

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "ForgotPasswordViewModel"

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    /**
     * Step 1: Send reset code to email
     */
    fun sendResetCode(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email is required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Invalid email format")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, email = email, error = null)
            Log.d(TAG, "Sending reset code to: $email")

            when (val result = repository.forgotPassword(email)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Reset code sent successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        codeSent = true,
                        email = email
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Failed to send reset code: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Step 2: Verify the reset code
     */
    fun verifyResetCode(code: String) {
        val email = _uiState.value.email

        if (code.isBlank() || code.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Invalid code format")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "Verifying reset code: $code for email: $email")

            when (val result = repository.verifyResetCode(email, code)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Code verified successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        codeVerified = true,
                        verificationCode = code,
                        error = null
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Code verification failed: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Step 3: Reset password - FIXED VERSION
     */
    fun resetPassword(newPassword: String) {  // ✅ Only takes newPassword parameter
        val email = _uiState.value.email
        val code = _uiState.value.verificationCode  // ✅ Get code from state

        // 🔍 Detailed logging
        Log.d(TAG, "========== RESET PASSWORD DEBUG ==========")
        Log.d(TAG, "📧 Email: '$email'")
        Log.d(TAG, "🔢 Code: '$code'")
        Log.d(TAG, "🔒 Password length: ${newPassword.length}")
        Log.d(TAG, "=========================================")

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email is missing. Please restart the process.")
            return
        }

        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Verification code is missing")
            return
        }

        if (newPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "Calling repository.resetPassword with email: $email, code: $code")

            when (val result = repository.resetPassword(email, code, newPassword)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Password reset successful!")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        passwordReset = true,
                        error = null
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "❌ Password reset failed: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    /**
     * Resend the verification code
     */
    fun resendCode() {
        val email = _uiState.value.email
        Log.d(TAG, "Resending code to: $email")
        sendResetCode(email)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset all state
     */
    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }
}