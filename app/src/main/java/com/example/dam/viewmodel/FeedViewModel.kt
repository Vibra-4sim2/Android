package com.example.dam.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.PublicationResponse
import com.example.dam.repository.PublicationRepository
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer le feed des publications
 */
class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "FeedViewModel"
    private val repository = PublicationRepository(application.applicationContext)
    private val currentUserId = UserPreferences.getUserId(application.applicationContext)

    // État du feed
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // Liste des publications
    private val _publications = MutableStateFlow<List<PublicationResponse>>(emptyList())
    val publications: StateFlow<List<PublicationResponse>> = _publications.asStateFlow()

    init {
        loadPublications()
    }

    /**
     * Charger toutes les publications
     */
    fun loadPublications() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            Log.d(TAG, "📥 Loading publications...")

            val result = repository.getAllPublications()

            result.fold(
                onSuccess = { publications ->
                    Log.d(TAG, "✅ Loaded ${publications.size} publications")
                    _publications.value = publications
                    _uiState.value = if (publications.isEmpty()) {
                        FeedUiState.Empty
                    } else {
                        FeedUiState.Success
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Failed to load publications", error)
                    _uiState.value = FeedUiState.Error(
                        error.message ?: "Failed to load publications"
                    )
                }
            )
        }
    }

    /**
     * Rafraîchir le feed
     */
    fun refreshFeed() {
        Log.d(TAG, "🔄 Refreshing feed...")
        loadPublications()
    }

    /**
     * Liker/Unliker une publication
     */
    fun toggleLike(publicationId: String) {
        viewModelScope.launch {
            Log.d(TAG, "❤️ Toggling like for publication: $publicationId")

            val result = repository.likePublication(publicationId)

            result.fold(
                onSuccess = { updatedPublication ->
                    Log.d(TAG, "✅ Like toggled successfully")

                    // Mettre à jour la publication dans la liste
                    _publications.value = _publications.value.map { pub ->
                        if (pub.id == publicationId) {
                            pub.copy(
                                likesCount = updatedPublication.likesCount,
                                likedBy = updatedPublication.likedBy
                            )
                        } else {
                            pub
                        }
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Failed to toggle like", error)
                }
            )
        }
    }

    /**
     * Vérifier si l'utilisateur actuel a liké une publication
     */
    fun isLikedByCurrentUser(publication: PublicationResponse): Boolean {
        return currentUserId?.let { userId ->
            publication.likedBy?.contains(userId) == true
        } ?: false
    }
}

/**
 * États possibles du feed
 */
sealed class FeedUiState {
    object Loading : FeedUiState()
    object Success : FeedUiState()
    object Empty : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}