package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.repository.MyResult
import com.example.dam.repository.UserProfileRepository
import com.example.dam.repository.PublicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

class UserProfileViewModel(private val context: Context) : ViewModel() {

    private val repository = UserProfileRepository()
    private val publicationRepository = PublicationRepository(context)

    // États existants
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

    private val _currentUserId = MutableStateFlow<String?>(null)

    // AJOUTÉ : Pour le filtre "Following" dans HomeExplore
    private val _followingIds = MutableStateFlow<Set<String>>(emptySet())
    val followingIds: StateFlow<Set<String>> = _followingIds.asStateFlow()

    // Charger les utilisateurs que l'utilisateur courant suit
    fun loadFollowing(currentUserId: String, token: String) {
        viewModelScope.launch {
            when (val result = repository.getFollowing(currentUserId, token)) {
                is MyResult.Success -> {
                    val ids = result.data.following.map { it.id }.toSet()
                    _followingIds.value = ids
                    Log.d("UserProfileVM", "Loaded ${ids.size} following users")
                }
                is MyResult.Failure -> {
                    Log.e("UserProfileVM", "Failed to load following: ${result.error.message}")
                }
                else -> {}
            }
        }
    }

    fun loadUserProfile(userId: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _currentUserId.value = com.example.dam.utils.UserPreferences.getUserId(context)

                // Charger le profil
                when (val userResult = repository.getUserById(userId, token)) {
                    is MyResult.Success -> _user.value = userResult.data
                    is MyResult.Failure -> _errorMessage.value = userResult.error.message
                    else -> {}
                }

                // Charger les sorties
                when (val sortiesResult = repository.getUserSorties(userId)) {
                    is MyResult.Success -> _userSorties.value = sortiesResult.data
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error loading sorties")
                    else -> {}
                }

                // Charger les publications
                when (val publicationsResult = repository.getUserPublications(userId)) {
                    is MyResult.Success -> _userPublications.value = publicationsResult.data
                    is MyResult.Failure -> Log.e("UserProfileVM", "Error loading publications")
                    else -> {}
                }

                // Vérifier si on suit déjà
                when (val followingResult = repository.checkIsFollowing(userId, token)) {
                    is MyResult.Success -> _isFollowing.value = followingResult.data
                    else -> {}
                }

                // Stats follow
                when (val statsResult = repository.getFollowStats(userId, token)) {
                    is MyResult.Success -> {
                        _followersCount.value = statsResult.data.followers
                        _followingCount.value = statsResult.data.following
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
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
                    // Mise à jour locale si c'est l'utilisateur courant qui suit quelqu'un
                    if (_currentUserId.value == com.example.dam.utils.UserPreferences.getUserId(context)) {
                        _followingIds.value = _followingIds.value + userId
                    }
                }
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
                    if (_currentUserId.value == com.example.dam.utils.UserPreferences.getUserId(context)) {
                        _followingIds.value = _followingIds.value - userId
                    }
                }
                else -> {}
            }
        }
    }

    fun toggleLike(publicationId: String) {
        viewModelScope.launch {
            try {
                val result = publicationRepository.likePublication(publicationId)
                if (result.isSuccess) {
                    val updatedPublication = result.getOrNull()
                    if (updatedPublication != null) {
                        val list = _userPublications.value.toMutableList()
                        val index = list.indexOfFirst { it.id == publicationId }
                        if (index != -1) {
                            list[index] = updatedPublication
                            _userPublications.value = list
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Like error", e)
            }
        }
    }

    fun isLikedByCurrentUser(publication: PublicationResponse): Boolean {
        val userId = _currentUserId.value ?: return false
        return publication.likedBy?.contains(userId) ?: false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}