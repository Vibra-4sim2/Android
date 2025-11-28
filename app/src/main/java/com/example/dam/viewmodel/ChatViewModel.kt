// viewmodel/ChatViewModel.kt
package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.remote.SocketService
import com.example.dam.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel pour gérer la conversation de chat avec Socket.IO
 */
class ChatViewModel : ViewModel() {

    private val messageRepository = MessageRepository()
    private val TAG = "ChatViewModel"

    // État de connexion Socket
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État d'envoi de message
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Liste des messages
    private val _messages = MutableStateFlow<List<MessageUI>>(emptyList())
    val messages: StateFlow<List<MessageUI>> = _messages.asStateFlow()

    // Message d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Utilisateurs en train de taper
    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()

    // Current sortieId
    private var currentSortieId: String? = null
    private var currentUserId: String? = null

    init {
        setupSocketListeners()
    }

    /**
     * Configure les listeners Socket.IO
     */
    private fun setupSocketListeners() {
        // Connexion établie
        SocketService.onConnected = {
            Log.d(TAG, "✅ Socket connected")
            _isConnected.value = true
            _errorMessage.value = null
        }

        // Déconnexion
        SocketService.onDisconnected = {
            Log.d(TAG, "❌ Socket disconnected")
            _isConnected.value = false
        }

        // Room rejointe avec messages initiaux
        SocketService.onJoinedRoom = { messages ->
            Log.d(TAG, "🏠 Joined room, received ${messages.size} messages")
            _isLoading.value = false

            currentUserId?.let { userId ->
                val messagesUI = messages.map { it.toMessageUI(userId) }
                _messages.value = messagesUI
            }
        }

        // Nouveau message reçu
        SocketService.onMessageReceived = { message ->
            Log.d(TAG, "📨 New message received: ${message.id}")

            currentUserId?.let { userId ->
                val messageUI = message.toMessageUI(userId)
                _messages.value = _messages.value + messageUI
            }

            _isSending.value = false
        }

        // Utilisateur en train de taper
        SocketService.onUserTyping = { userId, isTyping ->
            _typingUsers.value = if (isTyping) {
                _typingUsers.value + userId
            } else {
                _typingUsers.value - userId
            }
        }

        // Erreur
        SocketService.onError = { error ->
            Log.e(TAG, "⚠️ Socket error: $error")
            _errorMessage.value = error
            _isLoading.value = false
            _isSending.value = false
        }
    }

    /**
     * Se connecter au serveur Socket et rejoindre la room
     * @param sortieId ID de la sortie
     * @param context Context pour récupérer le token
     */
    fun connectAndJoinRoom(sortieId: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            currentSortieId = sortieId
            currentUserId = getUserId(context)

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isLoading.value = false
                    return@launch
                }

                if (currentUserId.isNullOrEmpty()) {
                    _errorMessage.value = "ID utilisateur non trouvé"
                    _isLoading.value = false
                    return@launch
                }

                Log.d(TAG, "🔌 Connecting to Socket for sortie: $sortieId")

                // Se connecter au serveur Socket.IO
                if (!SocketService.isConnected()) {
                    SocketService.connect(token)
                    // Attendre un peu que la connexion s'établisse
                    kotlinx.coroutines.delay(1000)
                }

                // Rejoindre la room du chat
                SocketService.joinRoom(sortieId)

            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception: ${e.message}", e)
                _errorMessage.value = "Erreur de connexion"
                _isLoading.value = false
            }
        }
    }

    /**
     * Quitter la room et se déconnecter
     */
    fun leaveRoom() {
        currentSortieId?.let { sortieId ->
            Log.d(TAG, "👋 Leaving room: $sortieId")
            SocketService.leaveRoom(sortieId)
        }
        currentSortieId = null
    }

    /**
     * Envoie un message texte via Socket.IO
     * @param sortieId ID de la sortie
     * @param content Contenu du message
     * @param context Context
     */
    fun sendTextMessage(sortieId: String, content: String, context: Context) {
        if (content.isBlank()) return

        if (!SocketService.isConnected()) {
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        _isSending.value = true

        try {
            Log.d(TAG, "📤 Sending text message: $content")

            val messageDto = CreateMessageDto(
                type = MessageType.TEXT,
                content = content
            )

            SocketService.sendMessage(sortieId, messageDto)

            // Note: Le message sera ajouté à la liste quand on recevra l'événement 'receiveMessage'

        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception: ${e.message}", e)
            _errorMessage.value = "Erreur d'envoi"
            _isSending.value = false
        }
    }

    /**
     * Upload une image puis l'envoie via Socket.IO
     * @param sortieId ID de la sortie
     * @param imageFile Fichier image
     * @param context Context
     */
    fun sendImageMessage(sortieId: String, imageFile: File, context: Context) {
        if (!SocketService.isConnected()) {
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        viewModelScope.launch {
            _isSending.value = true

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isSending.value = false
                    return@launch
                }

                Log.d(TAG, "📤 Uploading image: ${imageFile.name}")

                // 1. Upload l'image via REST (car Socket.IO ne supporte pas les fichiers)
                val uploadResult = messageRepository.uploadMedia(imageFile, "Bearer $token")

                uploadResult.fold(
                    onSuccess = { uploadResponse ->
                        Log.d(TAG, "✅ Image uploaded: ${uploadResponse.url}")

                        // 2. Envoyer le message avec l'URL via Socket.IO
                        val messageDto = CreateMessageDto(
                            type = MessageType.IMAGE,
                            mediaUrl = uploadResponse.url,
                            fileName = uploadResponse.originalName,
                            fileSize = uploadResponse.size,
                            mimeType = uploadResponse.mimeType
                        )

                        SocketService.sendMessage(sortieId, messageDto)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error uploading image: ${error.message}")
                        _errorMessage.value = "Échec de l'upload"
                        _isSending.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception: ${e.message}", e)
                _errorMessage.value = "Erreur d'envoi"
                _isSending.value = false
            }
        }
    }

    /**
     * Envoyer l'indicateur de frappe
     * @param sortieId ID de la sortie
     * @param isTyping true si en train de taper
     */
    fun sendTypingIndicator(sortieId: String, isTyping: Boolean) {
        if (SocketService.isConnected()) {
            SocketService.sendTypingIndicator(sortieId, isTyping)
        }
    }

    /**
     * Marquer un message comme lu
     * @param messageId ID du message
     * @param sortieId ID de la sortie
     */
    fun markMessageAsRead(messageId: String, sortieId: String) {
        if (SocketService.isConnected()) {
            SocketService.markAsRead(messageId, sortieId)
        }
    }

    /**
     * Réinitialise l'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Réinitialise les messages
     */
    fun resetMessages() {
        _messages.value = emptyList()
    }

    // ========== HELPERS ==========

    private fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun getUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", null)
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
        // Note: On ne déconnecte pas complètement pour permettre d'autres chats
    }
}