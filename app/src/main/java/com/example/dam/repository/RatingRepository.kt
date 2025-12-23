package com.example.dam.repository

import android.util.Log
import com.example.dam.models.CreateRatingRequest
import com.example.dam.models.CreateRatingResponse
import com.example.dam.models.CreatorRatingResponse
import com.example.dam.models.EligibleSortieForRating
import com.example.dam.models.RatingItem
import com.example.dam.models.SortieRatingsResponse
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RatingRepository {

    private val api = RetrofitInstance.adventureApi
    private val TAG = "RatingRepository"

    /**
     * ✅ NEW: Get eligible sorties for rating
     */
    suspend fun getEligibleSortiesForRating(
        token: String
    ): MyResult<List<EligibleSortieForRating>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching eligible sorties for rating")
                val response = api.getEligibleSortiesForRating("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val eligibleSorties = response.body()!!
                    Log.d(TAG, "✅ Found ${eligibleSorties.size} eligible sorties")
                    MyResult.Success(eligibleSorties)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        401 -> "Unauthorized - please login again"
                        404 -> "No eligible sorties found"
                        else -> "Failed to fetch eligible sorties: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getEligibleSortiesForRating: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * ✅ FIXED: Recompute and fetch creator rating in one call
     * This is the RECOMMENDED method to use
     */
    suspend fun getCreatorRatingWithRecompute(
        userId: String,
        token: String
    ): MyResult<CreatorRatingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Recompute ratings (trigger recalculation)
                Log.d(TAG, "🔄 Step 1: Recomputing ratings for user $userId")
                val recomputeResponse = api.recomputeCreatorRating(userId, "Bearer $token")

                if (recomputeResponse.isSuccessful && recomputeResponse.body() != null) {
                    Log.d(TAG, "✅ Recompute successful: ${recomputeResponse.body()?.message}")
                } else {
                    Log.w(TAG, "⚠️ Recompute failed (${recomputeResponse.code()}), continuing anyway...")
                }

                // Step 2: Fetch the updated rating data
                Log.d(TAG, "📥 Step 2: Fetching updated rating data")
                val fetchResponse = api.getCreatorRating(userId, "Bearer $token")

                if (fetchResponse.isSuccessful && fetchResponse.body() != null) {
                    val rating = fetchResponse.body()!!
                    Log.d(TAG, "✅ Creator rating loaded: ${rating.average} (${rating.count} reviews)")
                    MyResult.Success(rating)
                } else {
                    val errorBody = fetchResponse.errorBody()?.string()
                    val errorMsg = when (fetchResponse.code()) {
                        404 -> "User not found or no ratings yet"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to get creator rating: ${fetchResponse.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getCreatorRatingWithRecompute: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * ✅ Get creator rating (use this if you DON'T need to recompute first)
     */
    suspend fun getCreatorRating(
        userId: String,
        token: String
    ): MyResult<CreatorRatingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching creator rating for userId: $userId")
                val response = api.getCreatorRating(userId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val rating = response.body()!!
                    Log.d(TAG, "✅ Creator rating: ${rating.average} (${rating.count} reviews)")
                    MyResult.Success(rating)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        404 -> "User not found or no ratings yet"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to fetch creator rating: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getCreatorRating: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Get all ratings for a sortie
     */
    suspend fun getSortieRatings(
        sortieId: String,
        token: String,
        page: Int = 1,
        limit: Int = 100
    ): MyResult<SortieRatingsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching ratings for sortieId: $sortieId")
                val response = api.getSortieRatings(sortieId, "Bearer $token", page, limit)

                if (response.isSuccessful && response.body() != null) {
                    val ratings = response.body()!!
                    Log.d(TAG, "✅ Fetched ${ratings.total} ratings")
                    MyResult.Success(ratings)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to fetch ratings: ${response.code()} - $errorBody"
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getSortieRatings: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Create or update a rating
     */
    suspend fun createRating(
        sortieId: String,
        stars: Int,
        comment: String?,
        token: String
    ): MyResult<CreateRatingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📤 Creating rating: $stars stars for sortie $sortieId")
                val request = CreateRatingRequest(sortieId, stars, comment)
                val response = api.createRating(sortieId, "Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val rating = response.body()!!
                    Log.d(TAG, "✅ Rating created: ${rating.id}")
                    MyResult.Success(rating)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid rating data"
                        403 -> "You must be a participant to rate this sortie"
                        404 -> "Sortie not found"
                        401 -> "Unauthorized - please login again"
                        else -> errorBody ?: "Failed to create rating"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in createRating: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Get user's own rating for a sortie
     */
    suspend fun getUserRatingForSortie(
        sortieId: String,
        token: String
    ): MyResult<RatingItem?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching user rating for sortie: $sortieId")
                val response = api.getUserRatingForSortie(sortieId, "Bearer $token")

                if (response.isSuccessful) {
                    val rating = response.body()
                    Log.d(TAG, "✅ User rating fetched: ${rating?.stars ?: "none"}")
                    MyResult.Success(rating)
                } else if (response.code() == 404) {
                    Log.d(TAG, "ℹ️ User hasn't rated this sortie yet")
                    MyResult.Success(null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to fetch user rating: ${response.code()} - $errorBody"
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getUserRatingForSortie: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }
}