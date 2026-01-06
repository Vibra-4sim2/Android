package com.example.dam.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.repository.MyResult
import com.example.dam.repository.UserProfileRepository
import com.example.dam.repository.PublicationRepository
import com.example.dam.repository.ParticipationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.dam.repository.RatingRepository

class UserProfileViewModel(private val context: Context) : ViewModel() {

    private val repository = UserProfileRepository()
    private val ratingRepository = RatingRepository()

    private val publicationRepository = PublicationRepository(context)
    private val participationRepository = ParticipationRepository()

    // États existants
    private val _user = MutableStateFlow<UserProfileResponse?>(null)
    val user: StateFlow<UserProfileResponse?> = _user.asStateFlow()

    private val _userParticipations = MutableStateFlow<List<UserParticipationResponse>>(emptyList())
    val userParticipations: StateFlow<List<UserParticipationResponse>> = _userParticipations.asStateFlow()

    private val _userSorties = MutableStateFlow<List<SortieResponse>>(emptyList())
    val userSorties: StateFlow<List<SortieResponse>> = _userSorties.asStateFlow()

    private val _userPublications = MutableStateFlow<List<PublicationResponse>>(emptyList())
    val userPublications: StateFlow<List<PublicationResponse>> = _userPublications.asStateFlow()

    // ✅ NEW: Rating state
    private val _userRating = MutableStateFlow<CreatorRatingResponse?>(null)
    val userRating: StateFlow<CreatorRatingResponse?> = _userRating.asStateFlow()

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
            try {
                val result = repository.getFollowing(currentUserId, token)
                if (result is MyResult.Success<*>) {
                    val followingData = result.data as? FollowingResponse
                    if (followingData != null) {
                        val ids = followingData.following.map { it.id }.toSet()
                        _followingIds.value = ids
                        Log.d("UserProfileVM", "Loaded ${ids.size} following users")
                    }
                } else if (result is MyResult.Failure) {
                    Log.e("UserProfileVM", "Failed to load following: ${result.error.message}")
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Exception loading following: ${e.message}")
            }
        }
    }

    fun loadUserProfile(userId: String, token: String) {
        Log.d("UserProfileVM", "🔍 loadUserProfile called for userId: $userId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _currentUserId.value = com.example.dam.utils.UserPreferences.getUserId(context)

                // Charger le profil
                try {
                    val userResult = repository.getUserById(userId, token)
                    if (userResult is MyResult.Success<*>) {
                        _user.value = userResult.data as? UserProfileResponse

                        // Debug logging
                        _user.value?.let { user ->
                            Log.d("UserProfileVM", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            Log.d("UserProfileVM", "✅ User Profile Loaded:")
                            Log.d("UserProfileVM", "User ID: ${user.id}")
                            Log.d("UserProfileVM", "Name: ${user.firstName} ${user.lastName}")
                            Log.d("UserProfileVM", "Email: ${user.email}")
                            Log.d("UserProfileVM", "Avatar URL: ${user.avatar}")
                            Log.d("UserProfileVM", "Avatar is null? ${user.avatar == null}")
                            Log.d("UserProfileVM", "Avatar is empty? ${user.avatar?.isEmpty()}")
                            Log.d("UserProfileVM", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        }
                    } else if (userResult is MyResult.Failure) {
                        _errorMessage.value = userResult.error.message
                        Log.e("UserProfileVM", "❌ Failed to load user: ${userResult.error.message}")
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "Exception loading profile: ${e.message}")
                }

                // Charger les sorties créées
                try {
                    val sortiesResult = repository.getUserSorties(userId)
                    if (sortiesResult is MyResult.Success<*>) {
                        val sorties = sortiesResult.data as? List<*>
                        _userSorties.value = sorties?.filterIsInstance<SortieResponse>() ?: emptyList()
                    } else if (sortiesResult is MyResult.Failure) {
                        Log.e("UserProfileVM", "Error loading sorties")
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "Exception loading sorties: ${e.message}")
                }

                // ✅ Charger les participations (sorties auxquelles l'utilisateur participe)
                try {
                    val participationsResult = participationRepository.getUserParticipations(userId)
                    if (participationsResult is MyResult.Success<*>) {
                        val participations = participationsResult.data as? List<*>
                        _userParticipations.value = participations?.filterIsInstance<UserParticipationResponse>() ?: emptyList()
                        Log.d("UserProfileVM", "Loaded ${_userParticipations.value.size} participations")
                    } else if (participationsResult is MyResult.Failure) {
                        Log.e("UserProfileVM", "Error loading participations: ${participationsResult.error.message}")
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "Exception loading participations: ${e.message}")
                }

                // Charger les publications
                try {
                    Log.d("UserProfileVM", "📝 Loading publications for userId: $userId")
                    val publicationsResult = repository.getUserPublications(userId)
                    when (publicationsResult) {
                        is com.example.dam.utils.Result.Success -> {
                            _userPublications.value = publicationsResult.data
                            Log.d("UserProfileVM", "✅ Publications loaded: ${_userPublications.value.size} items")
                            _userPublications.value.forEachIndexed { index, pub ->
                                Log.d("UserProfileVM", "  Publication #${index + 1}: ${pub.content.take(50)}...")
                            }
                        }
                        is com.example.dam.utils.Result.Failure -> {
                            Log.e("UserProfileVM", "❌ Error loading publications: ${publicationsResult.message.message}")
                        }
                        is com.example.dam.utils.Result.Error -> {
                            Log.e("UserProfileVM", "❌ Error loading publications: ${publicationsResult.message}")
                        }
                        is com.example.dam.utils.Result.Loading -> {
                            Log.d("UserProfileVM", "⏳ Loading publications...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "❌ Exception loading publications: ${e.message}")
                }

                // ✅ NEW: Charger le rating du créateur
                try {
                    // ✅ Use the combined method that recomputes then fetches
                    when (val result = ratingRepository.getCreatorRatingWithRecompute(userId, token)) {
                        is MyResult.Success -> {
                            _userRating.value = result.data
                            Log.d(TAG, "✅ User rating loaded: ${result.data.average}")
                        }
                        is MyResult.Failure -> {
                            Log.e(TAG, "❌ Failed to load rating: ${result.error.message}")
                            // Set default empty rating
                            _userRating.value = CreatorRatingResponse(average = 0.0, count = 0)
                        }
                        is MyResult.Loading -> {
                            // Loading state
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception loading rating", e)
                    _userRating.value = CreatorRatingResponse(average = 0.0, count = 0)
                }

                // Vérifier si on suit déjà
                try {
                    val followingResult = repository.checkIsFollowing(userId, token)
                    if (followingResult is MyResult.Success<*>) {
                        _isFollowing.value = followingResult.data as? Boolean ?: false
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "Exception checking following: ${e.message}")
                }

                // Stats follow
                try {
                    Log.d("UserProfileVM", "🔍 Loading follow stats for userId: $userId")
                    val statsResult = repository.getFollowStats(userId, token)
                    if (statsResult is MyResult.Success<*>) {
                        // Cast to the correct type from AuthApiService
                        val stats = statsResult.data as? com.example.dam.remote.AuthApiService.FollowStatsResponse
                        if (stats != null) {
                            _followersCount.value = stats.followers
                            _followingCount.value = stats.following
                            Log.d("UserProfileVM", "✅ Stats loaded: followers=${stats.followers}, following=${stats.following}")
                        } else {
                            Log.e("UserProfileVM", "❌ Stats cast failed - data type: ${statsResult.data?.javaClass?.name}")
                        }
                    } else if (statsResult is MyResult.Failure) {
                        Log.e("UserProfileVM", "❌ Failed to load stats: ${statsResult.error.message}")
                    }
                } catch (e: Exception) {
                    Log.e("UserProfileVM", "❌ Exception loading stats: ${e.message}", e)
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
            try {
                val result = repository.followUser(userId, token)
                if (result is MyResult.Success<*>) {
                    _isFollowing.value = true
                    _followersCount.value += 1
                    // Mise à jour locale si c'est l'utilisateur courant qui suit quelqu'un
                    if (_currentUserId.value == com.example.dam.utils.UserPreferences.getUserId(context)) {
                        _followingIds.value = _followingIds.value + userId
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Exception following user: ${e.message}")
            }
        }
    }

    fun unfollowUser(userId: String, token: String) {
        viewModelScope.launch {
            try {
                val result = repository.unfollowUser(userId, token)
                if (result is MyResult.Success<*>) {
                    _isFollowing.value = false
                    _followersCount.value = maxOf(0, _followersCount.value - 1)
                    if (_currentUserId.value == com.example.dam.utils.UserPreferences.getUserId(context)) {
                        _followingIds.value = _followingIds.value - userId
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Exception unfollowing user: ${e.message}")
            }
        }
    }

    fun toggleLike(publicationId: String) {
        viewModelScope.launch {
            try {
                val result = publicationRepository.likePublication(publicationId)
                if (result.isSuccess) {
                    val likeResponse = result.getOrNull()
                    if (likeResponse != null) {
                        // Update only the like-related fields in the existing publication
                        _userPublications.value = _userPublications.value.map { pub ->
                            if (pub.id == publicationId) {
                                pub.copy(
                                    likesCount = likeResponse.likesCount,
                                    likedBy = likeResponse.likedBy
                                )
                            } else {
                                pub
                            }
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

    /**
     * ✅ Clear all user profile data (called on logout to prevent session leakage)
     */
    fun clearUserData() {
        _user.value = null
        _userParticipations.value = emptyList()
        _userSorties.value = emptyList()
        _userPublications.value = emptyList()
        _userRating.value = null
        _isLoading.value = false
        _isFollowing.value = false
        _followersCount.value = 0
        _followingCount.value = 0
        _followingIds.value = emptySet()
        _errorMessage.value = null
        _currentUserId.value = null
    }
}