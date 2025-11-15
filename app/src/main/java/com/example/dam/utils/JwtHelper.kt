package com.example.dam.utils
import android.util.Log
import com.auth0.android.jwt.JWT

object JwtHelper {
    private const val TAG = "JwtHelper"

    fun getUserIdFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            val userId = jwt.subject // "sub" claim contient le userId
            Log.d(TAG, "✅ Decoded userId from token: $userId")
            userId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decoding JWT: ${e.message}", e)
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            jwt.isExpired(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking token expiration: ${e.message}")
            true
        }
    }
}