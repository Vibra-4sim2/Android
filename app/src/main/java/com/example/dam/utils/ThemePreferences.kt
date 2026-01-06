package com.example.dam.utils

import android.content.Context
import android.util.Log

/**
 * ✅ Manages app theme preferences (Dark/Light mode)
 */
object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"
    private const val TAG = "ThemePreferences"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if dark mode is enabled (default is true)
     */
    fun isDarkMode(context: Context): Boolean {
        val isDark = getPrefs(context).getBoolean(KEY_IS_DARK_MODE, true) // Default: Dark mode
        Log.d(TAG, "🎨 Dark mode: $isDark")
        return isDark
    }

    /**
     * Set dark mode preference
     */
    fun setDarkMode(context: Context, isDark: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        Log.d(TAG, "✅ Dark mode set to: $isDark")
    }

    /**
     * Toggle between dark and light mode
     */
    fun toggleTheme(context: Context): Boolean {
        val newMode = !isDarkMode(context)
        setDarkMode(context, newMode)
        return newMode
    }
}

