package com.example.dam.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ✅ SIMPLIFIED Chat state manager
 * Tracks which chats are currently being viewed (session-only)
 * This makes badges disappear instantly when you open a chat
 */
object ChatStateManager {

    private var context: Context? = null

    // Simple set of sortieIds currently being viewed (session-only, not persisted)
    private val _recentlyOpenedChats = MutableStateFlow<Set<String>>(emptySet())
    val recentlyOpenedChats: StateFlow<Set<String>> = _recentlyOpenedChats.asStateFlow()

    /**
     * Initialize with context
     */
    fun initialize(appContext: Context) {
        if (context == null) {
            context = appContext.applicationContext
            android.util.Log.d("ChatStateManager", "✅ Initialized")
        }
    }

    /**
     * Mark a chat as currently being viewed
     */
    fun markChatAsOpened(sortieId: String) {
        android.util.Log.d("ChatStateManager", "✅ Marking chat as opened: $sortieId")

        // Add to currently viewing set
        _recentlyOpenedChats.value = _recentlyOpenedChats.value + sortieId

        android.util.Log.d("ChatStateManager", "📋 Currently viewing chats: ${_recentlyOpenedChats.value}")
    }

    /**
     * Clear a chat from the viewing set
     */
    fun clearOptimisticState(sortieId: String) {
        android.util.Log.d("ChatStateManager", "🧹 Clearing chat from viewing: $sortieId")

        // Remove from currently viewing set
        _recentlyOpenedChats.value = _recentlyOpenedChats.value - sortieId

        android.util.Log.d("ChatStateManager", "📋 Currently viewing chats: ${_recentlyOpenedChats.value}")
    }

    /**
     * Clear all viewing states
     */
    fun clearAllOptimisticStates() {
        android.util.Log.d("ChatStateManager", "🧹 Clearing all viewing states")
        _recentlyOpenedChats.value = emptySet()
    }
}

