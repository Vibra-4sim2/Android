// repository/ParticipationRepository.kt
package com.example.dam.repository

import android.util.Log
import com.example.dam.models.ParticipationRequest
import com.example.dam.models.ParticipationResponse
import com.example.dam.models.SimpleParticipationResponse
import com.example.dam.models.UpdateParticipationStatusRequest
import com.example.dam.models.UserParticipationResponse
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParticipationRepository {

    private val api = RetrofitInstance.adventureApi
    private val TAG = "ParticipationRepo"

    /**
     * Get all participations for a specific sortie
     * Endpoint: GET /participations?sortieId={sortieId}
     * Returns: Full ParticipationResponse with populated user/sortie
     */
    suspend fun getParticipations(sortieId: String): MyResult<List<ParticipationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching participations for sortieId: $sortieId")
                val response = api.getParticipations(sortieId)

                if (response.isSuccessful && response.body() != null) {
                    val participations = response.body()!!
                    Log.d(TAG, "✅ Fetched ${participations.size} participations")
                    MyResult.Success(participations)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to fetch participations: ${response.code()} - $errorBody"
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getParticipations: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * ✅ FIXED: Get participations for a user
     * Endpoint: GET /participations/user/{userId}
     * Returns: UserParticipationResponse with userId as STRING and sortieId as OBJECT
     */
    suspend fun getUserParticipations(userId: String): MyResult<List<UserParticipationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching participations for userId: $userId")
                val response = api.getUserParticipations(userId)

                if (response.isSuccessful && response.body() != null) {
                    val participations = response.body()!!
                    Log.d(TAG, "✅ Fetched ${participations.size} user participations")
                    MyResult.Success(participations)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to fetch user participations: ${response.code()} - $errorBody"
                    Log.e(TAG, "❌ $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getUserParticipations: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Create a participation (Join a sortie)
     * Endpoint: POST /participations
     * Returns: SimpleParticipationResponse (with string IDs only)
     */
    suspend fun createParticipation(sortieId: String, token: String): MyResult<SimpleParticipationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📤 Creating participation for sortieId: $sortieId")
                val request = ParticipationRequest(sortieId = sortieId)
                val response = api.createParticipation("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val participation = response.body()!!
                    Log.d(TAG, "✅ Participation created: ${participation._id}")
                    MyResult.Success(participation)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "Sortie at full capacity or invalid sortie ID"
                        409 -> "You already participate in this sortie"
                        401 -> "Unauthorized - please login again"
                        else -> errorBody ?: "Failed to create participation"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in createParticipation: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Update participation status (Accept/Refuse) - Creator only
     * Endpoint: PATCH /participations/{id}/status
     * Returns: SimpleParticipationResponse (with string IDs only)
     */
    suspend fun updateParticipationStatus(
        participationId: String,
        status: String,
        token: String
    ): MyResult<SimpleParticipationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Updating participation status to $status")
                val request = UpdateParticipationStatusRequest(status = status)
                val response = api.updateParticipationStatus(
                    id = participationId,
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "✅ Status updated to $status")
                    MyResult.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        403 -> "Only the sortie creator can update participation status"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to update status: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in updateParticipationStatus: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }

    /**
     * Cancel own participation
     * Endpoint: DELETE /participations/{id}
     */
    suspend fun cancelParticipation(participationId: String, token: String): MyResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Cancelling participation")
                val response = api.cancelParticipation(participationId, "Bearer $token")

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Participation cancelled")
                    MyResult.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        403 -> "Only the participant can cancel their participation"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to cancel participation: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    MyResult.Failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in cancelParticipation: $errorMsg", e)
                MyResult.Failure(e)
            }
        }
    }
}