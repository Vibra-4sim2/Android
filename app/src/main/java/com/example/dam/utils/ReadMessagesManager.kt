package com.example.dam.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ✅ Manages read/unread state for chat messages
 * Persists to SharedPreferences to survive app restarts
 */
object ReadMessagesManager {

    private const val PREFS_NAME = "read_messages_prefs"
    private const val KEY_READ_CHATS = "read_chats_set"

    // Set of chat IDs that have been read (last message was viewed)
    private val _readChatIds = MutableStateFlow<Set<String>>(emptySet())
    val readChatIds: StateFlow<Set<String>> = _readChatIds.asStateFlow()

    /**
     * Initialize from SharedPreferences
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedChats = prefs.getStringSet(KEY_READ_CHATS, emptySet()) ?: emptySet()
        _readChatIds.value = savedChats
    }

    /**
     * Mark a chat as read (user viewed the messages)
     */
    fun markChatAsRead(context: Context, sortieId: String) {
        val updated = _readChatIds.value.toMutableSet().apply {
            add(sortieId)
        }
        _readChatIds.value = updated

        // Persist to SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_READ_CHATS, updated).apply()

        android.util.Log.d("ReadMessagesManager", "✅ Marked chat $sortieId as read")
    }

    /**
     * Check if a chat has been read
     */
    fun isChatRead(sortieId: String): Boolean {
        return _readChatIds.value.contains(sortieId)
    }

    /**
     * Mark chat as unread (new message arrived)
     */
    fun markChatAsUnread(context: Context, sortieId: String) {
        val updated = _readChatIds.value.toMutableSet().apply {
            remove(sortieId)
        }
        _readChatIds.value = updated

        // Persist to SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_READ_CHATS, updated).apply()

        android.util.Log.d("ReadMessagesManager", "📬 Marked chat $sortieId as unread")
    }

    /**
     * Clear all read states (for logout or testing)
     */
    fun clearAll(context: Context) {
        _readChatIds.value = emptySet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

