package com.example.dam.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.UserProfileResponse
import com.example.dam.repository.AuthRepository
import com.example.dam.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _currentUser = MutableStateFlow<UserProfileResponse?>(null)
    val currentUser: StateFlow<UserProfileResponse?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Load user profile
    fun loadUserProfile(userId: String, token: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.getUserById(userId, token)
            _isLoading.value = false

            when (result) {
                is Result.Success -> {
                    _currentUser.value = result.data
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                Result.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    // Update user profile
    fun updateUserProfile(
        userId: String,
        token: String,
        firstName: String? = null,
        lastName: String? = null,
        gender: String? = null,
        email: String? = null,
        password: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.updateUser(
                userId = userId,
                token = token,
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                email = email,
                password = password
            )

            _isLoading.value = false

            when (result) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    onSuccess()
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    onError(result.message)
                }
                Result.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    // Upload user avatar
    fun uploadAvatar(
        userId: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.uploadAvatar(
                userId = userId,
                imageUri = imageUri,
                context = context
            )

            _isLoading.value = false

            when (result) {
                is Result.Success -> {
                    // Update current user with new avatar
                    _currentUser.value = result.data
                    onSuccess()
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                    onError(result.message)
                }
                Result.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * ✅ Clear all user data (called on logout to prevent session leakage)
     */
    fun clearUserData() {
        _currentUser.value = null
        _isLoading.value = false
        _errorMessage.value = null
    }
}