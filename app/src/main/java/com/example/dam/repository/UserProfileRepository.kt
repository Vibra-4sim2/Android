package com.example.dam.repository

import android.util.Log
import com.example.dam.models.*
import com.example.dam.remote.AuthApiService
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Sealed class to represent success, failure, and loading states
sealed class MyResult<out T> {
    data class Success<T>(val data: T) : MyResult<T>()
    data class Failure(val error: Exception) : MyResult<Nothing>()
    object Loading : MyResult<Nothing>()
}

class UserProfileRepository {

    private val authApi = RetrofitInstance.authApi
    private val adventureApi = RetrofitInstance.adventureApi
    private val publicationApi = RetrofitInstance.publicationApi

    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    suspend fun getUserById(userId: String, token: String): MyResult<UserProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.getUserById(userId, formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else {
                MyResult.Failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error getting user: ${e.message}", e)
            MyResult.Failure(e)
        }
    }

    suspend fun getUserSorties(userId: String): MyResult<List<SortieResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = adventureApi.getAllSorties()
            if (response.isSuccessful && response.body() != null) {
                val allSorties = response.body()!!
                val userSorties = allSorties.filter { it.createurId.id == userId }
                MyResult.Success(userSorties)
            } else {
                MyResult.Failure(Exception("Failed to get sorties: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error getting sorties: ${e.message}", e)
            MyResult.Failure(e)
        }
    }

    suspend fun getUserPublications(userId: String): MyResult<List<PublicationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = publicationApi.getPublicationsByAuthor(userId)
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else {
                Log.e("UserProfileRepo", "Failed to get publications: ${response.code()}")
                MyResult.Failure(Exception("Failed to get publications: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error getting publications: ${e.message}", e)
            MyResult.Failure(e)
        }
    }

    suspend fun checkIsFollowing(userId: String, token: String): MyResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.checkIsFollowing(userId, formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!.isFollowing)
            } else {
                MyResult.Failure(Exception("Failed to check following status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error checking following: ${e.message}", e)
            MyResult.Failure(e)
        }
    }

    suspend fun getFollowStats(userId: String, token: String): MyResult<AuthApiService.FollowStatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.getFollowStats(userId, formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else {
                MyResult.Failure(Exception("Failed to get follow stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error getting stats: ${e.message}", e)
            MyResult.Failure(e)
        }
    }

    suspend fun followUser(userId: String, token: String): MyResult<AuthApiService.FollowResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.followUser(userId, formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserProfileRepo", "Error following user: $errorBody")
                MyResult.Failure(Exception("Failed to follow user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Exception following user", e)
            MyResult.Failure(e)
        }
    }

    suspend fun unfollowUser(userId: String, token: String): MyResult<AuthApiService.FollowResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.unfollowUser(userId, formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else {
                MyResult.Failure(Exception("Failed to unfollow user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Error unfollowing user: ${e.message}", e)
            MyResult.Failure(e)
        }
    }
}
