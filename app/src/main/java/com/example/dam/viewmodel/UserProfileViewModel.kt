package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.PublicationResponse
import com.example.dam.models.SortieResponse
import com.example.dam.models.UserProfileResponse
import com.example.dam.repository.MyResult
import com.example.dam.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()

    private val _user = MutableStateFlow<UserProfileResponse?>(null)
    val user: StateFlow<UserProfileResponse?> = _user.asStateFlow()

    private val _userSorties = MutableStateFlow<List<SortieResponse>>(emptyList())
    val userSorties: StateFlow<List<SortieResponse>> = _userSorties.asStateFlow()

    private val _userPublications = MutableStateFlow<List<PublicationResponse>>(emptyList())
    val userPublications: StateFlow<List<PublicationResponse>> = _userPublications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadUserProfile(userId: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Load user profile
                when (val userResult = repository.getUserById(userId, token)) {
                    is MyResult.Success -> _user.value = userResult.data
                    is MyResult.Failure -> {
                        _errorMessage.value = userResult.error.message
                        Log.e("UserProfileVM", "Error loading user: ${userResult.error.message}")
                    }
                    else -> {}
                }

                // Load user's sorties
                when (val sortiesResult = repository.getUserSorties(userId)) {
                    is MyResult.Success -> _userSorties.value = sortiesResult.data
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error loading sorties: ${sortiesResult.error.message}")
                    else -> {}
                }

                // Load user's publications
                when (val publicationsResult = repository.getUserPublications(userId)) {
                    is MyResult.Success -> _userPublications.value = publicationsResult.data
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error loading publications: ${publicationsResult.error.message}")
                    else -> {}
                }

                // Check if current user is following this user
                when (val followingResult = repository.checkIsFollowing(userId, token)) {
                    is MyResult.Success -> _isFollowing.value = followingResult.data
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error checking follow status: ${followingResult.error.message}")
                    else -> {}
                }

                // Get follower/following counts ✅ pass token here
                when (val statsResult = repository.getFollowStats(userId, token)) {
                    is MyResult.Success -> {
                        _followersCount.value = statsResult.data.followers
                        _followingCount.value = statsResult.data.following
                    }
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error loading stats: ${statsResult.error.message}")
                    else -> {}
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("UserProfileVM", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun followUser(userId: String, token: String) {
        viewModelScope.launch {
            when (val result = repository.followUser(userId, token)) {
                is MyResult.Success -> {
                    _isFollowing.value = true
                    _followersCount.value += 1
                }
                is MyResult.Failure -> _errorMessage.value = result.error.message
                else -> {}
            }
        }
    }

    fun unfollowUser(userId: String, token: String) {
        viewModelScope.launch {
            when (val result = repository.unfollowUser(userId, token)) {
                is MyResult.Success -> {
                    _isFollowing.value = false
                    _followersCount.value = maxOf(0, _followersCount.value - 1)
                }
                is MyResult.Failure -> _errorMessage.value = result.error.message
                else -> {}
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
