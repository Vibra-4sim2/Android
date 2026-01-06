// viewmodel/ConversationsViewModel.kt
package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.repository.ConversationMediaRepository
import com.example.dam.utils.ConversationSocketManager
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ✅ VIEW MODEL POUR LES CONVERSATIONS PRIVÉES (1-1)
 * Complètement séparé du MessagesViewModel pour les chats de groupe
 */
class ConversationsViewModel : ViewModel() {

    companion object {
        private const val TAG = "ConversationsVM"
        // ✅ CORRECTION: Utiliser la MÊME URL que SocketService
        private const val SERVER_URL = "https://dam-4sim2.onrender.com"
    }

    private val socketManager = ConversationSocketManager.getInstance()
    private val mediaRepository = ConversationMediaRepository()

    // ==================== STATE FLOWS FROM SOCKET MANAGER ====================
    val isConnected: StateFlow<Boolean> = socketManager.isConnected
    val conversations: StateFlow<List<Conversation>> = socketManager.conversations
    val currentConversation: StateFlow<Conversation?> = socketManager.currentConversation
    val messages: StateFlow<List<DirectMessage>> = socketManager.messages
    val typingUsers: StateFlow<Set<String>> = socketManager.typingUsers
    val errorMessage: StateFlow<String?> = socketManager.errorMessage

    // ==================== LOCAL STATE ====================
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _conversationsUI = MutableStateFlow<List<ConversationUI>>(emptyList())
    val conversationsUI: StateFlow<List<ConversationUI>> = _conversationsUI.asStateFlow()

    private val _messagesUI = MutableStateFlow<List<DirectMessageUI>>(emptyList())
    val messagesUI: StateFlow<List<DirectMessageUI>> = _messagesUI.asStateFlow()

    private var currentUserId: String = ""

    init {
        // Observer conversations changes and convert to UI model
        viewModelScope.launch {
            conversations.collect { conversationsList ->
                if (currentUserId.isNotEmpty()) {
                    _conversationsUI.value = conversationsList.map {
                        ConversationUI.fromConversation(it, currentUserId)
                    }.sortedByDescending { it.lastMessageTimestamp }
                }
            }
        }

        // Observer messages changes and convert to UI model
        viewModelScope.launch {
            messages.collect { messagesList ->
                if (currentUserId.isNotEmpty()) {
                    _messagesUI.value = messagesList.map {
                        DirectMessageUI.fromDirectMessage(it, currentUserId)
                    }.sortedBy { it.timestamp }
                }
            }
        }
    }

    // ==================== INITIALIZATION ====================
    fun initialize(context: Context) {
        val token = UserPreferences.getToken(context) ?: ""
        val userId = UserPreferences.getUserId(context) ?: ""

        if (token.isEmpty() || userId.isEmpty()) {
            Log.e(TAG, "❌ Cannot initialize: token or userId is empty")
            return
        }

        currentUserId = userId
        socketManager.initialize(SERVER_URL, token, userId)
        // ✅ RÉACTIVÉ: Connecter automatiquement
        socketManager.connect()

        Log.d(TAG, "✅ Initialized with userId: $userId")
        Log.d(TAG, "📡 Connecting to: $SERVER_URL/conversations")
    }

    // ==================== CONVERSATION MANAGEMENT ====================
    fun loadConversations() {
        socketManager.getMyConversations()
    }

    fun startConversationWithUser(recipientId: String) {
        Log.d(TAG, "🚀 Starting conversation with user: $recipientId")
        socketManager.initiateConversation(recipientId)
    }

    fun openConversation(conversationId: String) {
        Log.d(TAG, "📂 Opening conversation: $conversationId")
        socketManager.joinConversation(conversationId)
        socketManager.getMessages(conversationId, limit = 50)
        socketManager.markAsRead(conversationId)
    }

    fun closeConversation(conversationId: String) {
        Log.d(TAG, "📁 Closing conversation: $conversationId")
        socketManager.leaveConversation(conversationId)
        socketManager.clearMessages()
    }

    fun deleteConversation(conversationId: String) {
        socketManager.deleteConversation(conversationId)
    }

    fun muteConversation(conversationId: String, muted: Boolean) {
        socketManager.muteConversation(conversationId, muted)
    }

    // ==================== MESSAGE MANAGEMENT ====================
    fun sendMessage(conversationId: String, content: String) {
        if (content.isBlank()) {
            Log.w(TAG, "⚠️ Cannot send empty message")
            return
        }
        socketManager.sendTextMessage(conversationId, content.trim())
    }

    fun sendImageMessage(conversationId: String, imageUrl: String, caption: String? = null) {
        socketManager.sendMessage(
            conversationId = conversationId,
            type = DirectMessageType.IMAGE,
            content = caption,
            mediaUrl = imageUrl
        )
    }

    fun sendAudioMessage(conversationId: String, audioUrl: String, duration: Int) {
        socketManager.sendMessage(
            conversationId = conversationId,
            type = DirectMessageType.AUDIO,
            mediaUrl = audioUrl,
            mediaDuration = duration
        )
    }

    // ==================== MEDIA UPLOAD AND SEND ====================

    /**
     * Upload et envoi d'une image
     */
    fun sendImageWithUpload(conversationId: String, imageFile: File, context: Context, caption: String? = null) {
        viewModelScope.launch {
            _isSending.value = true
            _uploadProgress.value = 0f

            try {
                Log.d(TAG, "📤 Uploading image: ${imageFile.name}")

                val result = withContext(Dispatchers.IO) {
                    mediaRepository.uploadImage(context, imageFile)
                }

                if (result.success && result.url != null) {
                    Log.d(TAG, "✅ Image uploaded: ${result.url}")
                    sendImageMessage(conversationId, result.url, caption)
                } else {
                    Log.e(TAG, "❌ Image upload failed: ${result.errorMessage}")
                    socketManager.setError(result.errorMessage ?: "Erreur d'upload")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Image upload error: ${e.message}")
                socketManager.setError(e.message ?: "Erreur d'upload")
            } finally {
                _isSending.value = false
                _uploadProgress.value = 0f
            }
        }
    }

    /**
     * Upload et envoi d'un audio
     */
    fun sendAudioWithUpload(conversationId: String, audioFile: File, duration: Int, context: Context) {
        viewModelScope.launch {
            _isSending.value = true
            _uploadProgress.value = 0f

            try {
                Log.d(TAG, "📤 Uploading audio: ${audioFile.name} (${duration}s)")

                val result = withContext(Dispatchers.IO) {
                    mediaRepository.uploadAudio(context, audioFile)
                }

                if (result.success && result.url != null) {
                    Log.d(TAG, "✅ Audio uploaded: ${result.url}")
                    sendAudioMessage(conversationId, result.url, result.duration ?: duration)
                } else {
                    Log.e(TAG, "❌ Audio upload failed: ${result.errorMessage}")
                    socketManager.setError(result.errorMessage ?: "Erreur d'upload")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Audio upload error: ${e.message}")
                socketManager.setError(e.message ?: "Erreur d'upload")
            } finally {
                _isSending.value = false
                _uploadProgress.value = 0f
            }
        }
    }

    /**
     * Upload et envoi d'une vidéo
     */
    fun sendVideoWithUpload(conversationId: String, videoFile: File, context: Context, caption: String? = null) {
        viewModelScope.launch {
            _isSending.value = true
            _uploadProgress.value = 0f

            try {
                Log.d(TAG, "📤 Uploading video: ${videoFile.name}")

                val result = withContext(Dispatchers.IO) {
                    mediaRepository.uploadVideo(context, videoFile)
                }

                if (result.success && result.url != null) {
                    Log.d(TAG, "✅ Video uploaded: ${result.url}")
                    socketManager.sendMessage(
                        conversationId = conversationId,
                        type = DirectMessageType.VIDEO,
                        content = caption,
                        mediaUrl = result.url,
                        mediaDuration = result.duration
                    )
                } else {
                    Log.e(TAG, "❌ Video upload failed: ${result.errorMessage}")
                    socketManager.setError(result.errorMessage ?: "Erreur d'upload")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Video upload error: ${e.message}")
                socketManager.setError(e.message ?: "Erreur d'upload")
            } finally {
                _isSending.value = false
                _uploadProgress.value = 0f
            }
        }
    }

    /**
     * Upload et envoi d'un fichier
     */
    fun sendFileWithUpload(conversationId: String, file: File, context: Context) {
        viewModelScope.launch {
            _isSending.value = true
            _uploadProgress.value = 0f

            try {
                Log.d(TAG, "📤 Uploading file: ${file.name}")

                val result = withContext(Dispatchers.IO) {
                    mediaRepository.uploadFile(context, file)
                }

                if (result.success && result.url != null) {
                    Log.d(TAG, "✅ File uploaded: ${result.url}")
                    socketManager.sendMessage(
                        conversationId = conversationId,
                        type = DirectMessageType.FILE,
                        mediaUrl = result.url,
                        fileName = result.originalName ?: file.name,
                        fileSize = result.size,
                        mimeType = result.mimeType
                    )
                } else {
                    Log.e(TAG, "❌ File upload failed: ${result.errorMessage}")
                    socketManager.setError(result.errorMessage ?: "Erreur d'upload")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ File upload error: ${e.message}")
                socketManager.setError(e.message ?: "Erreur d'upload")
            } finally {
                _isSending.value = false
                _uploadProgress.value = 0f
            }
        }
    }

    /**
     * Afficher une erreur
     */
    fun showError(message: String) {
        socketManager.setError(message)
    }

    fun sendLocationMessage(conversationId: String, latitude: Double, longitude: Double, address: String?) {
        socketManager.sendMessage(
            conversationId = conversationId,
            type = DirectMessageType.LOCATION,
            location = MessageLocation(latitude, longitude, address)
        )
    }

    fun markAsRead(conversationId: String) {
        socketManager.markAsRead(conversationId)
    }

    fun loadMoreMessages(conversationId: String, oldestMessageId: String) {
        socketManager.getMessages(conversationId, limit = 30, before = oldestMessageId)
    }

    // ==================== TYPING INDICATOR ====================
    fun setTyping(conversationId: String, isTyping: Boolean) {
        socketManager.sendTyping(conversationId, isTyping)
    }

    // ==================== ERROR HANDLING ====================
    fun clearError() {
        socketManager.clearError()
    }

    // ==================== CLEANUP ====================
    fun disconnect() {
        socketManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
        Log.d(TAG, "🧹 ViewModel cleared")
    }

    // ==================== HELPER FUNCTIONS ====================
    fun getOtherUserInConversation(conversationId: String): ConversationUser? {
        val conversation = conversations.value.find { it.id == conversationId }
        return conversation?.getOtherParticipant(currentUserId)
    }

    fun getTotalUnreadCount(): Int {
        return conversationsUI.value.sumOf { it.unreadCount }
    }
}

