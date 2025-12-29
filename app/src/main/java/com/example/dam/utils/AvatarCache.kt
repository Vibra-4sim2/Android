package com.example.dam.utils

import android.util.Log
import com.example.dam.models.UserProfileResponse
import com.example.dam.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cache for user avatars to avoid repeated API calls
 */
object AvatarCache {
    private val cache = mutableMapOf<String, String?>()

    /**
     * Get avatar URL for a user ID
     * First checks cache, then fetches from API if needed
     */
    suspend fun getAvatarForUser(userId: String, token: String): String? = withContext(Dispatchers.IO) {
        // Check cache first
        if (cache.containsKey(userId)) {
            Log.d("AvatarCache", "✅ Cache hit for user $userId")
            return@withContext cache[userId]
        }

        // Fetch from API
        try {
            Log.d("AvatarCache", "🔄 Fetching avatar for user $userId from API...")
            val response = RetrofitInstance.authApi.getUserById(userId, formatToken(token))

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                val avatar = user.avatar

                // Cache it
                cache[userId] = avatar

                Log.d("AvatarCache", "✅ Fetched and cached avatar for user $userId: $avatar")
                return@withContext avatar
            } else {
                Log.e("AvatarCache", "❌ Failed to fetch user $userId: ${response.code()}")
                cache[userId] = null
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("AvatarCache", "❌ Exception fetching user $userId: ${e.message}")
            cache[userId] = null
            return@withContext null
        }
    }

    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    /**
     * Clear the cache
     */
    fun clear() {
        cache.clear()
        Log.d("AvatarCache", "🗑️ Cache cleared")
    }

    /**
     * Pre-populate cache with known avatars
     */
    fun populate(userId: String, avatarUrl: String?) {
        cache[userId] = avatarUrl
        Log.d("AvatarCache", "📥 Pre-cached avatar for user $userId")
    }
}

