package com.example.dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.CreatorRatingResponse
import com.example.dam.models.EligibleSortieForRating
import com.example.dam.models.SortieRatingData
import com.example.dam.repository.MyResult
import com.example.dam.repository.RatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RatingViewModel : ViewModel() {

    private val repository = RatingRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ✅ NEW: StateFlow for creator rating
    private val _creatorRating = MutableStateFlow<CreatorRatingResponse?>(null)
    val creatorRating: StateFlow<CreatorRatingResponse?> = _creatorRating.asStateFlow()

    // ✅ NEW: StateFlow for eligible sorties
    private val _eligibleSorties = MutableStateFlow<List<EligibleSortieForRating>>(emptyList())
    val eligibleSorties: StateFlow<List<EligibleSortieForRating>> = _eligibleSorties.asStateFlow()

    /**
     * ✅ NEW: Load creator rating with automatic recompute
     * Use this method to display user's rating on profile/details screens
     */
    fun loadCreatorRating(userId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("RatingVM", "📊 Loading creator rating for user: $userId")

                // Use the method that recomputes AND fetches in one call
                when (val result = repository.getCreatorRatingWithRecompute(userId, token)) {
                    is MyResult.Success -> {
                        _creatorRating.value = result.data
                        Log.d("RatingVM", "✅ Creator rating loaded: ${result.data.average} stars (${result.data.count} reviews)")
                    }
                    is MyResult.Failure -> {
                        val errorMsg = result.error.message ?: "Failed to load rating"
                        _errorMessage.value = errorMsg

                        // If error is "User not found or no ratings yet", set count to 0
                        if (errorMsg.contains("not found", ignoreCase = true) ||
                            errorMsg.contains("no ratings", ignoreCase = true)) {
                            _creatorRating.value = CreatorRatingResponse(0.0, 0)
                        }

                        Log.e("RatingVM", "❌ Error loading creator rating: $errorMsg")
                    }
                    is MyResult.Loading -> {
                        // Loading state already set
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("RatingVM", "❌ Exception loading creator rating", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Submit a rating for a sortie
     */
    fun submitRating(
        sortieId: String,
        stars: Int,
        comment: String,
        token: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                Log.d("RatingVM", "📤 Submitting rating: $stars stars for sortie $sortieId")

                val commentToSend = if (comment.isBlank()) null else comment

                when (val result = repository.createRating(sortieId, stars, commentToSend, token)) {
                    is MyResult.Success -> {
                        _successMessage.value = "Rating submitted successfully!"
                        Log.d("RatingVM", "✅ Rating submitted: ${result.data.id}")
                        onSuccess()
                    }
                    is MyResult.Failure -> {
                        val errorMsg = result.error.message ?: "Unknown error"
                        val message = when {
                            errorMsg.contains("participant", ignoreCase = true) ->
                                "You must participate in this sortie to rate it"
                            errorMsg.contains("self-rating", ignoreCase = true) ->
                                "You cannot rate your own sortie"
                            errorMsg.contains("404") ->
                                "Sortie not found"
                            errorMsg.contains("401") ->
                                "Please login again"
                            errorMsg.contains("403") ->
                                "Only accepted participants can rate"
                            else -> errorMsg
                        }
                        _errorMessage.value = message
                        Log.e("RatingVM", "❌ Error: $errorMsg")
                    }
                    is MyResult.Loading -> {
                        // Loading state already set
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("RatingVM", "❌ Exception submitting rating", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Get sortie rating summary
     */
    fun getSortieRating(
        sortieId: String,
        token: String,
        onResult: (SortieRatingData?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = repository.getSortieRatings(sortieId, token)) {
                    is MyResult.Success -> {
                        val ratings = result.data.ratings
                        if (ratings.isNotEmpty()) {
                            val average = ratings.map { it.stars }.average()
                            onResult(SortieRatingData(average, result.data.total))
                        } else {
                            onResult(null)
                        }
                    }
                    is MyResult.Failure -> {
                        Log.e("RatingVM", "Failed to fetch ratings: ${result.error.message}")
                        onResult(null)
                    }
                    is MyResult.Loading -> {
                        // Loading
                    }
                }
            } catch (e: Exception) {
                Log.e("RatingVM", "Exception getting sortie rating", e)
                onResult(null)
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Clear creator rating (useful when navigating away)
     */
    fun clearCreatorRating() {
        _creatorRating.value = null
    }

    /**
     * ✅ NEW: Load eligible sorties for rating
     * Call this when the app starts or user logs in
     */
    fun loadEligibleSorties(token: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d("RatingVM", "📊 Loading eligible sorties for rating")

                when (val result = repository.getEligibleSortiesForRating(token)) {
                    is MyResult.Success -> {
                        _eligibleSorties.value = result.data
                        Log.d("RatingVM", "✅ Loaded ${result.data.size} eligible sorties")
                    }
                    is MyResult.Failure -> {
                        _errorMessage.value = "Failed to load eligible sorties"
                        Log.e("RatingVM", "❌ Error: ${result.error.message}")
                    }
                    is MyResult.Loading -> {
                        // Loading
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("RatingVM", "❌ Exception loading eligible sorties", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove a sortie from eligible list after rating
     */
    fun removeEligibleSortie(sortieId: String) {
        _eligibleSorties.value = _eligibleSorties.value.filter { it.sortieId != sortieId }
    }
}