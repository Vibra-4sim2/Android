package com.example.dam.repository

import android.util.Log
import com.example.dam.models.*
import com.example.dam.remote.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Classe pour encapsuler les résultats des opérations
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Repository pour gérer les opérations d'authentification
 * Fait le pont entre l'API et le ViewModel
 */
class AuthRepository {

    private val TAG = "AuthRepository"

    // ========== AUTH METHODS ==========

    /**
     * Appelle l'API pour se connecter
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                email = email.trim(),
                password = password
            )

            Log.d(TAG, "Login request: $request")
            val response = RetrofitInstance.authApi.login(request)
            Log.d(TAG, "Login response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Login successful")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login error body: $errorBody")
                Log.e(TAG, "Login error code: ${response.code()}")

                val errorMessage = parseErrorMessage(errorBody) ?: "Invalid credentials"
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception: ${e.message}", e)
            e.printStackTrace()
            Result.Error(getNetworkErrorMessage(e))
        }
    }

    /**
     * Appelle l'API pour créer un nouveau compte
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        Gender: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                Gender = Gender.uppercase(),
                email = email.trim(),
                password = password
            )

            Log.d(TAG, "Register request: $request")
            Log.d(TAG, "Register gender value: ${request.Gender}")

            val response = RetrofitInstance.authApi.register(request)
            Log.d(TAG, "Register response code: ${response.code()}")

            if (response.isSuccessful) {
                Log.d(TAG, "Register response body: ${response.body()}")
            }

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Register successful")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Register error body: $errorBody")
                Log.e(TAG, "Register error code: ${response.code()}")

                val errorMessage = when (response.code()) {
                    400 -> {
                        val parsedError = parseErrorMessage(errorBody)
                        parsedError ?: "Invalid registration data. Please check your inputs."
                    }
                    409 -> "This email is already registered. Please use another email."
                    422 -> "Invalid data format. Please check your inputs."
                    500 -> "Server error. Please try again later."
                    503 -> "Service unavailable. Please try again later."
                    else -> parseErrorMessage(errorBody) ?: "Registration failed. Please try again."
                }

                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register exception: ${e.message}", e)
            e.printStackTrace()
            Result.Error(getNetworkErrorMessage(e))
        }
    }

    // ========== PASSWORD RESET METHODS ==========

    /**
     * Envoie un code de réinitialisation par email
     * POST /auth/forgot-password
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            val request = ForgotPasswordRequest(email = email.trim())

            Log.d(TAG, "ForgotPassword request: $request")
            val response = RetrofitInstance.authApi.forgotPassword(request)
            Log.d(TAG, "ForgotPassword response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "ForgotPassword successful: ${response.body()?.message}")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "ForgotPassword error: $errorBody")

                val errorMessage = when (response.code()) {
                    404 -> "Email not found. Please check your email address."
                    400 -> parseErrorMessage(errorBody) ?: "Invalid email address."
                    else -> parseErrorMessage(errorBody) ?: "Failed to send reset code."
                }
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ForgotPassword exception: ${e.message}", e)
            e.printStackTrace()
            Result.Error(getNetworkErrorMessage(e))
        }
    }

    /**
     * Vérifie le code de réinitialisation
     * POST /auth/verify-reset-code
     */
    suspend fun verifyResetCode(email: String, code: String): Result<VerifyResetCodeResponse> {
        return try {
            val request = VerifyResetCodeRequest(
                email = email.trim(),
                code = code.trim()
            )

            Log.d(TAG, "VerifyResetCode request: email=$email, code=$code")
            val response = RetrofitInstance.authApi.verifyResetCode(request)
            Log.d(TAG, "VerifyResetCode response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "VerifyResetCode successful: ${response.body()?.message}")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "VerifyResetCode error: $errorBody")

                val errorMessage = when (response.code()) {
                    400 -> "Invalid or expired code. Please try again."
                    404 -> "Code not found. Please request a new code."
                    else -> parseErrorMessage(errorBody) ?: "Code verification failed."
                }
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "VerifyResetCode exception: ${e.message}", e)
            e.printStackTrace()
            Result.Error(getNetworkErrorMessage(e))
        }
    }

    /**
     * Réinitialise le mot de passe avec le code vérifié
     * POST /auth/reset-password
     */
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<ResetPasswordResponse> {
        return try {
            val request = ResetPasswordRequest(
                email = email.trim(),
                code = code.trim(),
                newPassword = newPassword
            )

            // 🔍 ADD THIS DETAILED LOGGING
            Log.d(TAG, "========== API REQUEST DEBUG ==========")
            Log.d(TAG, "📧 Request Email: '${request.email}'")
            Log.d(TAG, "🔢 Request Code: '${request.code}'")
            Log.d(TAG, "🔢 Request Code length: ${request.code.length}")
            Log.d(TAG, "🔒 Request Password: '${request.newPassword.take(3)}...'")
            Log.d(TAG, "🌐 Endpoint: POST /auth/reset-password")
            Log.d(TAG, "======================================")

            val response = RetrofitInstance.authApi.resetPassword(request)
            Log.d(TAG, "ResetPassword response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "ResetPassword successful: ${response.body()?.message}")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "ResetPassword error: $errorBody")

                val errorMessage = when (response.code()) {
                    400 -> "Invalid code or password. Please try again."
                    404 -> "Reset request not found. Please start again."
                    422 -> "Password is too weak. Use at least 6 characters."
                    else -> parseErrorMessage(errorBody) ?: "Failed to reset password."
                }
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ResetPassword exception: ${e.message}", e)
            e.printStackTrace()
            Result.Error(getNetworkErrorMessage(e))
        }
    }
    // ========== USER PROFILE METHODS ==========

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String, token: String): Result<UserProfileResponse> {
        return try {
            val response = RetrofitInstance.authApi.getUserById(userId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMessage = "Failed to get user: ${response.code()} - ${response.message()}"
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message ?: "Unknown error"}")
        }
    }

    /**
     * Update user
     */
    suspend fun updateUser(
        userId: String,
        token: String,
        firstName: String? = null,
        lastName: String? = null,
        gender: String? = null,
        email: String? = null,
        password: String? = null
    ): Result<UpdateUserResponse> {
        return try {
            val updateRequest = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                email = email,
                password = password
            )

            val response = RetrofitInstance.authApi.updateUser(userId, updateRequest, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMessage = "Update failed: ${response.code()} - ${response.message()}"
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message ?: "Unknown error"}")
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Parse le message d'erreur du body JSON
     */
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) {
            Log.w(TAG, "Error body is null or blank")
            return null
        }

        return try {
            Log.d(TAG, "Parsing error body: $errorBody")
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

            when {
                errorResponse.message is String -> {
                    Log.d(TAG, "Error message (String): ${errorResponse.message}")
                    errorResponse.message
                }
                errorResponse.message is List<*> -> {
                    val messageList = errorResponse.message as List<*>
                    val errorMsg = messageList.joinToString(", ")
                    Log.d(TAG, "Error message (Array): $errorMsg")
                    errorMsg
                }
                !errorResponse.error.isNullOrBlank() -> {
                    Log.d(TAG, "Error message from 'error' field: ${errorResponse.error}")
                    errorResponse.error
                }
                !errorResponse.errors.isNullOrEmpty() -> {
                    val errorMsg = errorResponse.errors.joinToString(", ")
                    Log.d(TAG, "Error message from 'errors' array: $errorMsg")
                    errorMsg
                }
                else -> {
                    Log.w(TAG, "No error message found in error response")
                    null
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            errorBody.take(200)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while parsing: ${e.message}")
            null
        }
    }

    /**
     * Génère un message d'erreur compréhensible pour les erreurs réseau
     */
    private fun getNetworkErrorMessage(e: Exception): String {
        val errorMessage = when {
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "Cannot connect to server. Check your internet connection."

            e.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timeout. Please try again."

            e.message?.contains("Failed to connect", ignoreCase = true) == true ->
                "Cannot reach server. Make sure the server is running at http://192.168.1.169:3000"

            e.message?.contains("ECONNREFUSED", ignoreCase = true) == true ->
                "Server refused connection. Check if the server is running."

            e.message?.contains("SocketTimeoutException", ignoreCase = true) == true ->
                "Request timed out. Check your internet connection."

            e.message?.contains("UnknownHostException", ignoreCase = true) == true ->
                "Cannot find server. Check your network connection."

            else -> {
                Log.e(TAG, "Unknown network error: ${e.javaClass.simpleName} - ${e.message}")
                "Network error: ${e.message ?: "Unknown error occurred"}"
            }
        }

        Log.e(TAG, "Network error message: $errorMessage")
        return errorMessage
    }
}

/*
INSTRUCTIONS:
1. REMPLACEZ COMPLÈTEMENT votre fichier AuthRepository.kt par ce code
2. Synchronisez Gradle
3. Testez les 3 APIs de password reset
*/