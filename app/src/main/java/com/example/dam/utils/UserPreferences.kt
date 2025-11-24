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
        Log.d(TAG, "✅ Saved userId: $userId")
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
        Log.d(TAG, "✅ Saved token: ${token.take(30)}...")

        // Décoder automatiquement le token pour extraire le userId
        val userId = JwtHelper.getUserIdFromToken(token)
        userId?.let { saveUserId(context, it) }

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

    // ✅ Clear pour le logout (preserve onboarding flags)
    fun clear(context: Context) {
        Log.d(TAG, "🚪 Clearing user session...")

        // Save onboarding status before clearing
        val wasOnboardingComplete = getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)

        getPrefs(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            // ❌ DO NOT REMOVE KEY_ONBOARDING_COMPLETE
            // ❌ DO NOT REMOVE KEY_FIRST_LAUNCH
            .apply()

        // Restore onboarding status (user already completed it once)
        if (wasOnboardingComplete) {
            setOnboardingComplete(context, true)
        }

        Log.d(TAG, "✅ Session cleared (onboarding status preserved: $wasOnboardingComplete)")
    }
}