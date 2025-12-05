package com.example.dam.repository

import android.util.Log
import com.example.dam.models.ParticipationRequest
import com.example.dam.models.ParticipationResponse
import com.example.dam.models.SimpleParticipationResponse
import com.example.dam.models.UpdateParticipationStatusRequest
import com.example.dam.remote.RetrofitInstance
import com.example.dam.utils.Result
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
    suspend fun getParticipations(sortieId: String): Result<List<ParticipationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "📥 Fetching participations for sortieId: $sortieId")
                Log.d(TAG, "========================================")

                val response = api.getParticipations(sortieId)

                Log.d(TAG, "Response Code: ${response.code()}")
                Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")
                Log.d(TAG, "Response body is null: ${response.body() == null}")

                if (response.isSuccessful && response.body() != null) {
                    val participations = response.body()!!
                    Log.d(TAG, "✅ Fetched ${participations.size} participations")

                    participations.forEachIndexed { index, p ->
                        Log.d(TAG, "[$index] ID: ${p._id}, Status: ${p.status}, User: ${p.userId.email}")
                    }

                    Result.Success(participations)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to fetch participations: ${response.code()} - $errorBody"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in getParticipations: $errorMsg", e)
                e.printStackTrace()
                Result.Error(errorMsg)
            }
        }
    }

    /**
     * Create a participation (Join a sortie)
     * Endpoint: POST /participations
     * Returns: SimpleParticipationResponse (with string IDs only)
     */
    suspend fun createParticipation(sortieId: String, token: String): Result<SimpleParticipationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "📤 Creating participation for sortieId: $sortieId")
                Log.d(TAG, "Token: ${token.take(20)}...")
                Log.d(TAG, "========================================")

                val request = ParticipationRequest(sortieId = sortieId)
                val response = api.createParticipation("Bearer $token", request)

                Log.d(TAG, "Response Code: ${response.code()}")
                Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val participation = response.body()!!
                    Log.d(TAG, "✅ Participation created: ${participation._id}")
                    Log.d(TAG, "Status: ${participation.status}")
                    Result.Success(participation)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "Sortie at full capacity or invalid sortie ID"
                        409 -> "You already participates in this sortie"
                        401 -> "Unauthorized - please login again"
                        else -> errorBody ?: "Failed to create participation"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in createParticipation: $errorMsg", e)
                e.printStackTrace()
                Result.Error(errorMsg)
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
        status: String, // "ACCEPTEE" or "REFUSEE"
        token: String
    ): Result<SimpleParticipationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🔄 Updating participation status")
                Log.d(TAG, "Participation ID: $participationId")
                Log.d(TAG, "New Status: $status")
                Log.d(TAG, "Token: ${token.take(20)}...")
                Log.d(TAG, "========================================")

                val request = UpdateParticipationStatusRequest(status = status)
                val response = api.updateParticipationStatus(
                    id = participationId,
                    token = "Bearer $token",
                    request = request
                )

                Log.d(TAG, "Response Code: ${response.code()}")
                Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "✅ Status updated to $status")
                    Result.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        403 -> "Only the sortie creator can update participation status"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to update status: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in updateParticipationStatus: $errorMsg", e)
                e.printStackTrace()
                Result.Error(errorMsg)
            }
        }
    }

    /**
     * Cancel own participation
     * Endpoint: DELETE /participations/{id}
     */
    suspend fun cancelParticipation(participationId: String, token: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🗑️ Cancelling participation")
                Log.d(TAG, "Participation ID: $participationId")
                Log.d(TAG, "Token: ${token.take(20)}...")
                Log.d(TAG, "========================================")

                val response = api.cancelParticipation(participationId, "Bearer $token")

                Log.d(TAG, "Response Code: ${response.code()}")
                Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Participation cancelled")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        403 -> "Only the participant can cancel their participation"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to cancel participation: ${response.code()} - $errorBody"
                    }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, "❌ EXCEPTION in cancelParticipation: $errorMsg", e)
                e.printStackTrace()
                Result.Error(errorMsg)
            }
        }
    }
}