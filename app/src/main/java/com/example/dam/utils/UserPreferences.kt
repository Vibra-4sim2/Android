package com.example.dam.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object UserPreferences {
    private const val TAG = "UserPreferences"
    private const val PREF_NAME = "cycle_app_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: String) {
        getPrefs(context).edit().putString(KEY_USER_ID, userId).apply()
        Log.d(TAG, "✅ Saved userId to cycle_app_prefs: $userId")

        // ✅ ALSO save to auth_prefs for backwards compatibility
        try {
            val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            authPrefs.edit().putString("user_id", userId).apply()
            Log.d(TAG, "✅ Also saved userId to auth_prefs")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving userId to auth_prefs: ${e.message}")
        }
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun saveToken(context: Context, token: String) {
        // Save to main preferences (cycle_app_prefs)
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
        Log.d(TAG, "✅ Saved token to cycle_app_prefs: ${token.take(30)}...")

        // Décoder automatiquement le token pour extraire le userId
        val userId = JwtHelper.getUserIdFromToken(token)
        userId?.let {
            saveUserId(context, it)

            // ✅ ALSO save to auth_prefs for backwards compatibility
            try {
                val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                authPrefs.edit()
                    .putString("access_token", token)
                    .putString("user_id", it)
                    .apply()
                Log.d(TAG, "✅ Also saved to auth_prefs for compatibility")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving to auth_prefs: ${e.message}")
            }
        }

        // Marquer que ce n'est plus la première utilisation
        setFirstLaunchComplete(context)
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
        Log.d(TAG, "✅ Onboarding complete: $complete")
    }

    fun isOnboardingComplete(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    // ✅ Gestion du premier lancement
    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        Log.d(TAG, "✅ First launch marked as complete")
    }

    // ✅ Clear ALL user data on logout - COMPLETE session reset
    fun clear(context: Context) {
        Log.d(TAG, "🚪 Clearing user session...")

        // 1️⃣ Clear main preferences (cycle_app_prefs)
        getPrefs(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ONBOARDING_COMPLETE)  // ✅ Clear onboarding for new user
            // Keep KEY_FIRST_LAUNCH to skip app onboarding screens
            .apply()

        // 2️⃣ CRITICAL FIX: Also clear auth_prefs (duplicate storage location)
        try {
            val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            authPrefs.edit()
                .remove("access_token")
                .remove("user_id")
                .apply()
            Log.d(TAG, "✅ Cleared auth_prefs")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing auth_prefs: ${e.message}")
        }

        // 3️⃣ Clear login preferences (remember me data) - optional but recommended
        try {
            val loginPrefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            loginPrefs.edit()
                .remove("saved_email")
                .remove("saved_password")
                .putBoolean("remember_me", false)
                .apply()
            Log.d(TAG, "✅ Cleared login_prefs (remember me)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing login_prefs: ${e.message}")
        }

        // 4️⃣ Clear chat state (optimistic badge states)
        try {
            ChatStateManager.clearAllOptimisticStates()
            Log.d(TAG, "✅ Cleared chat state")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing chat state: ${e.message}")
        }

        Log.d(TAG, "✅ Session cleared completely - ready for new user")
    }

    /**
     * Clear ALL session-related data including auth_prefs
     * This ensures complete session isolation between different users
     * Use this for logout to prevent data leakage
     */
    fun clearAllSessionData(context: Context) {
        Log.d(TAG, "🚪 Clearing ALL user session data (complete isolation)...")

        // Clear UserPreferences (cycle_app_prefs)
        getPrefs(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ONBOARDING_COMPLETE)
            // Keep KEY_FIRST_LAUNCH to skip app onboarding screens
            .apply()

        // Clear auth_prefs (used by ProfileScreen, LoginScreen, etc.)
        val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        authPrefs.edit()
            .remove("access_token")
            .remove("user_id")
            .apply()
        Log.d(TAG, "✅ Cleared auth_prefs (access_token, user_id)")

        // Clear login_prefs remember me data (optional - preserves remember me)
        // Uncomment if you want to also clear saved credentials on logout
        // val loginPrefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        // loginPrefs.edit().clear().apply()

        Log.d(TAG, "✅ Session cleared completely - ready for new user")
    }

    // ✅ Clear ALL data including first launch (for complete app reset)
    fun clearAll(context: Context) {
        Log.d(TAG, "🔥 Clearing ALL app data...")
        getPrefs(context).edit().clear().apply()
        Log.d(TAG, "✅ All data cleared")
    }
}