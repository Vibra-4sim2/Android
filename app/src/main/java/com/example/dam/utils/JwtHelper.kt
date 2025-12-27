package com.example.dam.utils
import android.util.Log
import com.auth0.android.jwt.JWT

object JwtHelper {
    private const val TAG = "JwtHelper"

    fun getUserIdFromToken(token: String?): String? {
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "❌ Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(token)

            // Try to get userId from different possible claims
            var userId: String? = jwt.subject // "sub" claim (standard)

            if (userId.isNullOrEmpty()) {
                // Try alternative claim names
                userId = jwt.getClaim("userId").asString()
                Log.d(TAG, "⚠️ userId not in 'sub', trying 'userId' claim: $userId")
            }

            if (userId.isNullOrEmpty()) {
                userId = jwt.getClaim("id").asString()
                Log.d(TAG, "⚠️ userId not in 'userId', trying 'id' claim: $userId")
            }

            if (userId.isNullOrEmpty()) {
                Log.e(TAG, "❌ No userId found in token claims")
                // Log all available claims for debugging
                Log.e(TAG, "Available claims: ${jwt.claims.keys}")
            } else {
                Log.d(TAG, "✅ Decoded userId from token: $userId")
            }

            userId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decoding JWT: ${e.message}", e)
            Log.e(TAG, "Token (first 50 chars): ${token.take(50)}")
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            // Add 5 minutes grace period (300 seconds) to account for clock skew
            val isExpired = jwt.isExpired(300)

            // Log expiration details for debugging
            val expiresAt = jwt.expiresAt
            if (expiresAt != null) {
                val now = System.currentTimeMillis()
                val timeLeft = (expiresAt.time - now) / 1000 / 60 // minutes
                Log.d(TAG, "🕐 Token expires at: $expiresAt")
                Log.d(TAG, "🕐 Time left: $timeLeft minutes")
                Log.d(TAG, "🔐 Token expired (with grace): $isExpired")
            }

            isExpired
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking token expiration: ${e.message}", e)
            // On error, consider token invalid
            true
        }
    }
}