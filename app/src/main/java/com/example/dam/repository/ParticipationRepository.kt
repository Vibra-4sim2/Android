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

    /**
     * Get all participations for a specific sortie
     * Endpoint: GET /participations?sortieId={sortieId}
     * Returns: Full ParticipationResponse with populated user/sortie
     */
    suspend fun getParticipations(sortieId: String): Result<List<ParticipationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getParticipations(sortieId)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("ParticipationRepo", "✅ Fetched ${response.body()!!.size} participations")
                    Result.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch participations: ${response.code()}"
                    Log.e("ParticipationRepo", errorMsg)
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ParticipationRepo", errorMsg, e)
                Result.Error(errorMsg)
            }
        }
    }

    /**
     * ✅ FIXED: Create a participation (Join a sortie)
     * Endpoint: POST /participations
     * Returns: SimpleParticipationResponse (with string IDs only)
     */
    suspend fun createParticipation(sortieId: String, token: String): Result<SimpleParticipationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ParticipationRequest(sortieId = sortieId)
                val response = api.createParticipation("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("ParticipationRepo", "✅ Participation created: ${response.body()!!._id}")
                    Result.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = when (response.code()) {
                        400 -> "Sortie at full capacity or invalid sortie ID"
                        409 -> "You already participates in this sortie"
                        401 -> "Unauthorized - please login again"
                        else -> errorBody ?: "Failed to create participation"
                    }
                    Log.e("ParticipationRepo", "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ParticipationRepo", errorMsg, e)
                Result.Error(errorMsg)
            }
        }
    }

    /**
     * ✅ FIXED: Update participation status (Accept/Refuse) - Creator only
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
                val request = UpdateParticipationStatusRequest(status = status)
                val response = api.updateParticipationStatus(
                    id = participationId,
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    Log.d("ParticipationRepo", "✅ Status updated to $status")
                    Result.Success(response.body()!!)
                } else {
                    val errorMsg = when (response.code()) {
                        403 -> "Only the sortie creator can update participation status"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to update status: ${response.code()}"
                    }
                    Log.e("ParticipationRepo", "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ParticipationRepo", errorMsg, e)
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
                val response = api.cancelParticipation(participationId, "Bearer $token")

                if (response.isSuccessful) {
                    Log.d("ParticipationRepo", "✅ Participation cancelled")
                    Result.Success(Unit)
                } else {
                    val errorMsg = when (response.code()) {
                        403 -> "Only the participant can cancel their participation"
                        404 -> "Participation not found"
                        401 -> "Unauthorized - please login again"
                        else -> "Failed to cancel participation: ${response.code()}"
                    }
                    Log.e("ParticipationRepo", "❌ Error ${response.code()}: $errorMsg")
                    Result.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ParticipationRepo", errorMsg, e)
                Result.Error(errorMsg)
            }
        }
    }
}