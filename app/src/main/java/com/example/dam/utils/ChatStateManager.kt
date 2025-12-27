package com.example.dam.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton to manage chat state across ViewModels
 * Used to immediately clear badges when entering a chat (optimistic UI update)
 *
 * ✅ NOW PERSISTED: State survives app restarts using SharedPreferences
 */
object ChatStateManager {

    private const val PREFS_NAME = "chat_state_prefs"
    private const val KEY_RECENTLY_OPENED = "recently_opened_chats"

    private var context: Context? = null

    // Set of sortieIds that have been recently opened (optimistically marked as read)
    private val _recentlyOpenedChats = MutableStateFlow<Set<String>>(emptySet())
    val recentlyOpenedChats: StateFlow<Set<String>> = _recentlyOpenedChats.asStateFlow()

    /**
     * Initialize the manager with application context
     * Call this from Application.onCreate() or first usage
     */
    fun initialize(appContext: Context) {
        if (context == null) {
            context = appContext.applicationContext
            loadPersistedState()
            android.util.Log.d("ChatStateManager", "✅ Initialized with persisted state: ${_recentlyOpenedChats.value}")
        }
    }

    /**
     * Load persisted optimistic state from SharedPreferences
     */
    private fun loadPersistedState() {
        try {
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val persistedSet = prefs.getStringSet(KEY_RECENTLY_OPENED, emptySet()) ?: emptySet()
                _recentlyOpenedChats.value = persistedSet
                android.util.Log.d("ChatStateManager", "📂 Loaded ${persistedSet.size} persisted optimistic states")
                android.util.Log.d("ChatStateManager", "   Loaded IDs: $persistedSet")
            } ?: run {
                android.util.Log.w("ChatStateManager", "⚠️ Context is null, cannot load state")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatStateManager", "❌ Error loading persisted state: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Save optimistic state to SharedPreferences
     */
    private fun savePersistedState() {
        try {
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val success = prefs.edit().putStringSet(KEY_RECENTLY_OPENED, _recentlyOpenedChats.value).commit()
                android.util.Log.d("ChatStateManager", "💾 Saved ${_recentlyOpenedChats.value.size} optimistic states to disk")
                android.util.Log.d("ChatStateManager", "   Save success: $success")
                android.util.Log.d("ChatStateManager", "   Saved IDs: ${_recentlyOpenedChats.value}")
            } ?: run {
                android.util.Log.w("ChatStateManager", "⚠️ Context is null, cannot save state")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatStateManager", "❌ Error saving persisted state: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Mark a chat as recently opened (optimistic badge clear)
     * @param sortieId The sortie ID of the chat
     */
    fun markChatAsOpened(sortieId: String) {
        android.util.Log.d("ChatStateManager", "========================================")
        android.util.Log.d("ChatStateManager", "✅ MARKING CHAT AS OPENED")
        android.util.Log.d("ChatStateManager", "   sortieId: $sortieId")
        android.util.Log.d("ChatStateManager", "   Current optimistic states BEFORE: ${_recentlyOpenedChats.value}")

        _recentlyOpenedChats.value = _recentlyOpenedChats.value + sortieId
        savePersistedState()  // ✅ Persist to disk

        android.util.Log.d("ChatStateManager", "   Current optimistic states AFTER: ${_recentlyOpenedChats.value}")
        android.util.Log.d("ChatStateManager", "   Total optimistic states: ${_recentlyOpenedChats.value.size}")
        android.util.Log.d("ChatStateManager", "========================================")
    }

    /**
     * Clear a chat from the recently opened set
     * Called after backend confirms messages are read
     * @param sortieId The sortie ID of the chat
     */
    fun clearOptimisticState(sortieId: String) {
        val wasPresentBefore = _recentlyOpenedChats.value.contains(sortieId)
        _recentlyOpenedChats.value = _recentlyOpenedChats.value - sortieId
        savePersistedState()  // ✅ Persist to disk
        android.util.Log.d("ChatStateManager", "🧹 Optimistic state cleared for: $sortieId")
        android.util.Log.d("ChatStateManager", "   Was present before: $wasPresentBefore")
        android.util.Log.d("ChatStateManager", "   Current optimistic set: ${_recentlyOpenedChats.value}")
    }

    /**
     * Check if a chat was recently opened (for optimistic badge hiding)
     * @param sortieId The sortie ID to check
     * @return true if the chat was recently opened
     */
    fun isChatRecentlyOpened(sortieId: String): Boolean {
        return _recentlyOpenedChats.value.contains(sortieId)
    }

    /**
     * Clear all optimistic states (use when refreshing from backend)
     */
    fun clearAllOptimisticStates() {
        _recentlyOpenedChats.value = emptySet()
        savePersistedState()  // ✅ Persist to disk
        android.util.Log.d("ChatStateManager", "🧹 All optimistic states cleared")
    }
}

