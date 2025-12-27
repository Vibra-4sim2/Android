package com.example.dam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.models.*
import com.example.dam.remote.SocketService
import com.example.dam.repository.MessageRepository
import com.example.dam.utils.ChatStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.io.File

class ChatViewModel : ViewModel() {

    private val messageRepository = MessageRepository()
    private val TAG = "ChatViewModel"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageUI>>(emptyList())
    val messages: StateFlow<List<MessageUI>> = _messages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()

    private val _polls = MutableStateFlow<List<Poll>>(emptyList())
    val polls: StateFlow<List<Poll>> = _polls.asStateFlow()

    private val _isLoadingPolls = MutableStateFlow(false)
    val isLoadingPolls: StateFlow<Boolean> = _isLoadingPolls.asStateFlow()

    private var currentSortieId: String? = null
    private var currentUserId: String? = null
    private var sendTimeoutJob: Job? = null

    init {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🆕 ChatViewModel INIT - Instance créée")
        Log.d(TAG, "   Timestamp: ${System.currentTimeMillis()}")
        Log.d(TAG, "   HashCode: ${this.hashCode()}")
        Log.d(TAG, "========================================")
        setupSocketListeners()
    }

    private fun setupSocketListeners() {
        SocketService.onConnected = {
            Log.d(TAG, "✅ Socket connected")
            _isConnected.value = true
            _errorMessage.value = null

            currentSortieId?.let { sortieId ->
                SocketService.joinRoom(sortieId)
            }
        }

        SocketService.onDisconnected = {
            Log.d(TAG, "❌ Socket disconnected")
            _isConnected.value = false
        }

        SocketService.onJoinedRoom = { messages ->
            Log.d(TAG, "========================================")
            Log.d(TAG, "🏠 EVENT: joinedRoom - DIAGNOSTIC")
            Log.d(TAG, "========================================")
            Log.d(TAG, "📨 Messages reçus: ${messages.size}")
            Log.d(TAG, "🔍 État AVANT traitement joinedRoom:")
            Log.d(TAG, "   isConnected: ${_isConnected.value}")
            Log.d(TAG, "   isSending: ${_isSending.value} ⚠️")
            Log.d(TAG, "   isLoading: ${_isLoading.value}")

            // ✅ CORRECTION CRITIQUE: Mettre isConnected à true quand on a rejoint la room
            _isConnected.value = true
            _isLoading.value = false

            currentUserId?.let { userId ->
                val messagesUI = messages.map { it.toMessageUI(userId) }
                _messages.value = messagesUI.sortedBy { it.timestamp }

                Log.d(TAG, "📦 ${messagesUI.size} messages affichés")

                // ✅ NOUVEAU: Marquer tous les messages non lus comme lus avec un petit délai
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500) // Petit délai pour laisser l'UI se stabiliser
                    markAllMessagesAsRead()
                }
            }

            Log.d(TAG, "🔍 État APRÈS traitement joinedRoom:")
            Log.d(TAG, "   isConnected: ${_isConnected.value} ✅ (maintenant TRUE)")
            Log.d(TAG, "   isSending: ${_isSending.value} (devrait rester false)")
            Log.d(TAG, "   isLoading: ${_isLoading.value} (devrait être false)")
            Log.d(TAG, "========================================")
        }

        SocketService.onMessageReceived = { message ->
            Log.d(TAG, "📨 New message received: ${message._id}")

            currentUserId?.let { userId ->
                val messageUI = message.toMessageUI(userId)

                if (_messages.value.none { it.id == messageUI.id }) {
                    _messages.value = (_messages.value + messageUI).sortedBy { it.timestamp }
                    Log.d(TAG, "✅ Message added to list (total: ${_messages.value.size})")
                }
            }

            // ⚠️ NE PAS modifier _isSending ici (supprimé si existait)
        }

        SocketService.onMessageSent = { messageId, success ->
            Log.d(TAG, "✅ Message sent confirmation: $messageId (success: $success)")

            sendTimeoutJob?.cancel()
            sendTimeoutJob = null
            _isSending.value = false

            if (success) {
                _successMessage.value = "Message envoyé"

                viewModelScope.launch {
                    val updatedMessages = _messages.value.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(status = MessageStatus.SENT)
                        } else {
                            msg
                        }
                    }
                    _messages.value = updatedMessages
                }
            } else {
                _errorMessage.value = "Échec de l'envoi"

                viewModelScope.launch {
                    val updatedMessages = _messages.value.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(status = MessageStatus.FAILED)
                        } else {
                            msg
                        }
                    }
                    _messages.value = updatedMessages
                }
            }
        }

        SocketService.onUserTyping = { userId, isTyping ->
            _typingUsers.value = if (isTyping) {
                _typingUsers.value + userId
            } else {
                _typingUsers.value - userId
            }

            Log.d(TAG, "⌨️ Typing users: ${_typingUsers.value.size}")
        }

        SocketService.onError = { error ->
            Log.e(TAG, "⚠️ Socket error: $error")
            _errorMessage.value = error
            _isLoading.value = false
            _isSending.value = false
            sendTimeoutJob?.cancel()
        }

        SocketService.onMessageRead = { messageId, userId ->
            Log.d(TAG, "📖 Message marked as read: $messageId by $userId")

            // Update local message status
            viewModelScope.launch {
                val updatedMessages = _messages.value.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(status = MessageStatus.READ)
                    } else {
                        msg
                    }
                }
                _messages.value = updatedMessages
            }
        }

        // ========== POLL LISTENERS ==========
        SocketService.onPollCreated = { pollId ->
            Log.d(TAG, "📊 New poll created via WebSocket: $pollId")
            // Recharger tous les sondages
            currentSortieId?.let { sortieId ->
                viewModelScope.launch {
                    val context = getApplicationContext()
                    if (context != null) {
                        loadPolls(sortieId, context)
                    }
                }
            }
        }

        SocketService.onPollUpdated = { pollId ->
            Log.d(TAG, "📊 Poll updated via WebSocket: $pollId")
            // Recharger tous les sondages
            currentSortieId?.let { sortieId ->
                viewModelScope.launch {
                    val context = getApplicationContext()
                    if (context != null) {
                        loadPolls(sortieId, context)
                    }
                }
            }
        }

        SocketService.onPollClosed = { pollId ->
            Log.d(TAG, "📊 Poll closed via WebSocket: $pollId")
            // Recharger tous les sondages
            currentSortieId?.let { sortieId ->
                viewModelScope.launch {
                    val context = getApplicationContext()
                    if (context != null) {
                        loadPolls(sortieId, context)
                    }
                }
            }
        }
    }

    // Helper pour obtenir le contexte (à ajouter si nécessaire)
    private var applicationContext: Context? = null

    private fun getApplicationContext(): Context? = applicationContext

    fun setApplicationContext(context: Context) {
        applicationContext = context.applicationContext
    }

    fun connectAndJoinRoom(sortieId: String, context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🔌 DÉBUT CONNEXION CHAT - DIAGNOSTIC COMPLET")
            Log.d(TAG, "========================================")
            Log.d(TAG, "📍 Paramètres:")
            Log.d(TAG, "   sortieId demandé: $sortieId")
            Log.d(TAG, "   currentSortieId actuel: $currentSortieId")
            Log.d(TAG, "   Même sortie? ${sortieId == currentSortieId}")

            // ✅ OPTIMISTIC BADGE CLEARING: Mark chat as opened immediately
            ChatStateManager.markChatAsOpened(sortieId)

            Log.d(TAG, "🔍 État AVANT nettoyage:")
            Log.d(TAG, "   isConnected: ${_isConnected.value}")
            Log.d(TAG, "   isSending: ${_isSending.value} ⚠️ CRITIQUE")
            Log.d(TAG, "   isLoading: ${_isLoading.value}")
            Log.d(TAG, "   Socket.isConnected: ${SocketService.isConnected()}")
            Log.d(TAG, "   errorMessage: ${_errorMessage.value}")
            Log.d(TAG, "   successMessage: ${_successMessage.value}")
            Log.d(TAG, "   sendTimeoutJob: ${if (sendTimeoutJob != null) "ACTIF ⚠️" else "null"}")
            Log.d(TAG, "   messages: ${_messages.value.size}")

            // ✅ NETTOYER COMPLÈTEMENT l'état précédent
            Log.d(TAG, "🧹 Nettoyage de l'état...")
            sendTimeoutJob?.cancel()
            sendTimeoutJob = null

            _isSending.value = false
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _typingUsers.value = emptySet()

            Log.d(TAG, "🔍 État APRÈS nettoyage:")
            Log.d(TAG, "   isSending: ${_isSending.value} (devrait être false)")
            Log.d(TAG, "   isLoading: ${_isLoading.value} (devrait être true)")

            // ⚠️ NE PAS réinitialiser _messages ici (on veut les garder)
            // ⚠️ NE PAS réinitialiser _isConnected (le socket peut être déjà connecté)

            currentSortieId = sortieId
            currentUserId = getUserId(context)

            try {
                Log.d(TAG, "👤 userId: $currentUserId")

                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Token non trouvé!")
                    _errorMessage.value = "Token non trouvé. Reconnectez-vous."
                    _isLoading.value = false
                    return@launch
                }

                Log.d(TAG, "🔑 Token: ${token.take(20)}...")

                if (currentUserId.isNullOrEmpty()) {
                    Log.e(TAG, "❌ ID utilisateur non trouvé!")
                    _errorMessage.value = "ID utilisateur non trouvé"
                    _isLoading.value = false
                    return@launch
                }

                if (!SocketService.isConnected()) {
                    Log.d(TAG, "🔌 Connexion au serveur Socket.IO...")
                    SocketService.connect(token)

                    // ✅ CORRECTION: Augmentation à 60 secondes pour Render cold start
                    var attempts = 0
                    val maxAttempts = 120 // 120 * 500ms = 60 secondes
                    while (!SocketService.isConnected() && attempts < maxAttempts) {
                        kotlinx.coroutines.delay(500)
                        attempts++
                        if (attempts % 10 == 0) {
                            Log.d(TAG, "⏳ Tentative $attempts/$maxAttempts... (${attempts * 500 / 1000}s)")
                        }
                    }

                    if (!SocketService.isConnected()) {
                        Log.e(TAG, "❌ Échec de connexion après $attempts tentatives (${attempts * 500 / 1000}s)")
                        _errorMessage.value = "Impossible de se connecter au serveur. Le serveur met du temps à démarrer (Render cold start). Veuillez réessayer."
                        _isLoading.value = false
                        return@launch
                    }

                    Log.d(TAG, "✅ Connexion Socket.IO établie après ${attempts * 500 / 1000}s!")
                } else {
                    Log.d(TAG, "✅ Socket déjà connecté")
                    // ✅ CORRECTION: Synchroniser _isConnected avec l'état réel du socket
                    _isConnected.value = true
                    Log.d(TAG, "🔄 _isConnected forcé à true (socket déjà connecté)")
                }

                Log.d(TAG, "🏠 Tentative de rejoindre la room: $sortieId")
                Log.d(TAG, "🔍 État avant joinRoom:")
                Log.d(TAG, "   _isConnected: ${_isConnected.value}")
                Log.d(TAG, "   SocketService.isConnected(): ${SocketService.isConnected()}")
                SocketService.joinRoom(sortieId)

                Log.d(TAG, "✅ Demande de join envoyée, en attente de confirmation...")
                Log.d(TAG, "========================================")

            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception lors de la connexion", e)
                Log.e(TAG, "Message: ${e.message}")
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Erreur de connexion: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun leaveRoom() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "👋 LEAVE ROOM APPELÉ")
        Log.d(TAG, "========================================")
        Log.d(TAG, "📍 currentSortieId: $currentSortieId")
        Log.d(TAG, "🔍 État AVANT leave:")
        Log.d(TAG, "   isConnected: ${_isConnected.value}")
        Log.d(TAG, "   isSending: ${_isSending.value}")
        Log.d(TAG, "   isLoading: ${_isLoading.value}")
        Log.d(TAG, "   messages: ${_messages.value.size}")
        Log.d(TAG, "   errorMessage: ${_errorMessage.value}")
        Log.d(TAG, "   sendTimeoutJob: ${if (sendTimeoutJob != null) "ACTIF" else "null"}")

        currentSortieId?.let { sortieId ->
            // ✅ CRITICAL FIX: Mark all messages as read BEFORE leaving
            // Use runBlocking to ensure this completes before we leave the room
            Log.d(TAG, "📖 Marquage final des messages comme lus avant de quitter...")

            try {
                kotlinx.coroutines.runBlocking {
                    // Mark all messages as read
                    markAllMessagesAsRead()

                    // CRITICAL: Wait extra time to ensure WebSocket events are sent
                    // This prevents badge from reappearing when user navigates back quickly
                    kotlinx.coroutines.delay(800) // 800ms to ensure backend receives events
                    Log.d(TAG, "✅ Waited 800ms for mark-as-read events to be sent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error waiting for mark-as-read: ${e.message}")
            }

            // ✅ Nettoyer TOUT l'état au leave
            sendTimeoutJob?.cancel()
            sendTimeoutJob = null
            _isSending.value = false
            _isLoading.value = false
            _errorMessage.value = null
            _successMessage.value = null
            _typingUsers.value = emptySet()

            Log.d(TAG, "📤 Émission leaveRoom pour sortieId: $sortieId")
            SocketService.leaveRoom(sortieId)
        } ?: Log.w(TAG, "⚠️ currentSortieId est NULL, pas de leaveRoom émis")

        currentSortieId = null

        Log.d(TAG, "🔍 État APRÈS leave:")
        Log.d(TAG, "   isConnected: ${_isConnected.value}")
        Log.d(TAG, "   isSending: ${_isSending.value}")
        Log.d(TAG, "   isLoading: ${_isLoading.value}")
        Log.d(TAG, "   currentSortieId: $currentSortieId")
        Log.d(TAG, "✅ LeaveRoom terminé")
        Log.d(TAG, "========================================")
    }

    /**
     * ✅ Déconnecter complètement (appelé au logout)
     */
    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting and resetting state")

        leaveRoom()
        sendTimeoutJob?.cancel()
        sendTimeoutJob = null

        _isSending.value = false
        _isLoading.value = false
        _isConnected.value = false
        _messages.value = emptyList()
        _errorMessage.value = null
        _successMessage.value = null
        _typingUsers.value = emptySet()

        SocketService.disconnect()

        currentSortieId = null
        currentUserId = null

        Log.d(TAG, "✅ Disconnected and reset complete")
    }

    fun sendTextMessage(sortieId: String, content: String, context: Context) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 ENVOI MESSAGE TEXTE")
        Log.d(TAG, "========================================")

        if (content.isBlank()) {
            Log.e(TAG, "❌ Message vide")
            _errorMessage.value = "Le message ne peut pas être vide"
            return
        }

        Log.d(TAG, "📍 sortieId: $sortieId")
        Log.d(TAG, "💬 content: $content")
        Log.d(TAG, "🔌 isConnected: ${SocketService.isConnected()}")
        Log.d(TAG, "🔍 isSending avant: ${_isSending.value}")

        if (!SocketService.isConnected()) {
            Log.e(TAG, "❌ Non connecté au serveur!")
            _errorMessage.value = "Non connecté au serveur. Veuillez vous reconnecter."
            return
        }

        if (_isSending.value) {
            Log.w(TAG, "⚠️ Envoi déjà en cours, message ignoré")
            return
        }

        if (currentSortieId != sortieId) {
            Log.w(TAG, "⚠️ Avertissement: sortieId différent du currentSortieId")
            Log.w(TAG, "   sortieId envoyé: $sortieId")
            Log.w(TAG, "   currentSortieId: $currentSortieId")
        }

        sendTimeoutJob?.cancel()

        _isSending.value = true
        _errorMessage.value = null

        Log.d(TAG, "🔍 isSending après: ${_isSending.value}")

        try {
            val messageDto = CreateMessageDto(
                type = MessageType.TEXT,
                content = content.trim()
            )

            Log.d(TAG, "📨 Emission du message via Socket.IO...")
            SocketService.sendMessage(sortieId, messageDto)
            Log.d(TAG, "✅ Message émis, en attente de confirmation...")

            sendTimeoutJob = viewModelScope.launch {
                kotlinx.coroutines.delay(10000)
                if (_isSending.value) {
                    Log.e(TAG, "⏱️ Timeout : aucune confirmation reçue après 10 secondes")
                    _isSending.value = false
                    _errorMessage.value = "Délai d'envoi dépassé. Le message a peut-être été envoyé."
                }
            }

            Log.d(TAG, "========================================")

        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception lors de l'envoi", e)
            Log.e(TAG, "Message: ${e.message}")
            _errorMessage.value = "Erreur d'envoi: ${e.message}"
            _isSending.value = false
            sendTimeoutJob?.cancel()
        }
    }

    fun sendImageMessage(sortieId: String, imageFile: File, context: Context) {
        if (!SocketService.isConnected()) {
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isSending.value = false
                    return@launch
                }

                Log.d(TAG, "📤 Uploading image: ${imageFile.name}")

                val uploadResult = messageRepository.uploadMedia(imageFile, "Bearer $token")

                uploadResult.fold(
                    onSuccess = { uploadResponse ->
                        Log.d(TAG, "✅ Image uploaded: ${uploadResponse.url}")

                        val messageDto = CreateMessageDto(
                            type = MessageType.IMAGE,
                            mediaUrl = uploadResponse.url,
                            fileName = uploadResponse.originalName,
                            fileSize = uploadResponse.size,
                            mimeType = uploadResponse.mimeType
                        )

                        SocketService.sendMessage(sortieId, messageDto)
                        _successMessage.value = "Image envoyée"
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error uploading image: ${error.message}")
                        _errorMessage.value = "Échec de l'upload: ${error.message}"
                        _isSending.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception: ${e.message}", e)
                _errorMessage.value = "Erreur d'envoi: ${e.message}"
                _isSending.value = false
            }
        }
    }

    /**
     * ✅ NOUVEAU: Envoyer un message audio (vocal)
     */
    fun sendAudioMessage(sortieId: String, audioFile: File, durationSeconds: Int, context: Context) {
        if (!SocketService.isConnected()) {
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isSending.value = false
                    return@launch
                }

                Log.d(TAG, "🎤 Uploading audio: ${audioFile.name} (${durationSeconds}s)")

                val uploadResult = messageRepository.uploadMedia(audioFile, "Bearer $token")

                uploadResult.fold(
                    onSuccess = { uploadResponse ->
                        Log.d(TAG, "✅ Audio uploaded: ${uploadResponse.url}")

                        val messageDto = CreateMessageDto(
                            type = MessageType.AUDIO,
                            mediaUrl = uploadResponse.url,
                            mediaDuration = durationSeconds.toDouble(),
                            fileName = uploadResponse.originalName,
                            fileSize = uploadResponse.size,
                            mimeType = uploadResponse.mimeType
                        )

                        SocketService.sendMessage(sortieId, messageDto)
                        _successMessage.value = "Message vocal envoyé"
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error uploading audio: ${error.message}")
                        _errorMessage.value = "Échec de l'upload audio: ${error.message}"
                        _isSending.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception: ${e.message}", e)
                _errorMessage.value = "Erreur d'envoi audio: ${e.message}"
                _isSending.value = false
            }
        }
    }

    fun sendTypingIndicator(sortieId: String, isTyping: Boolean) {
        if (SocketService.isConnected()) {
            SocketService.sendTypingIndicator(sortieId, isTyping)
        }
    }

    fun markMessageAsRead(messageId: String, sortieId: String) {
        if (SocketService.isConnected()) {
            SocketService.markAsRead(messageId, sortieId)
        }
    }

    /**
     * ✅ NOUVEAU: Marquer tous les messages non lus comme lus
     */
    private fun markAllMessagesAsRead() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    // Trouver tous les messages qui ne sont pas "read" par l'utilisateur courant
                    val unreadMessages = _messages.value.filter { message ->
                        !message.isMe && message.status != MessageStatus.READ
                    }

                    Log.d(TAG, "========================================")
                    Log.d(TAG, "📖 Marquage de ${unreadMessages.size} messages comme lus")
                    Log.d(TAG, "👤 Current userId: $userId")
                    Log.d(TAG, "📍 Current sortieId: $currentSortieId")

                    // Marquer chaque message comme lu via WebSocket avec un petit délai entre chaque
                    unreadMessages.forEachIndexed { index, message ->
                        currentSortieId?.let { sortieId ->
                            Log.d(TAG, "   📧 Message ${index + 1}/${unreadMessages.size}: ${message.id}")
                            SocketService.markAsRead(message.id, sortieId)

                            // ✅ Petit délai entre chaque message pour éviter de surcharger le WebSocket
                            if (index < unreadMessages.size - 1) {
                                kotlinx.coroutines.delay(50) // 50ms entre chaque message
                            }
                        }
                    }

                    // ✅ Délai final pour s'assurer que tous les événements sont envoyés
                    if (unreadMessages.isNotEmpty()) {
                        kotlinx.coroutines.delay(200)
                        Log.d(TAG, "✅ Tous les ${unreadMessages.size} messages marqués comme lus avec délais appropriés")
                    } else {
                        Log.d(TAG, "ℹ️ Aucun message non lu à marquer")
                    }

                    Log.d(TAG, "========================================")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur marquage messages lus: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun showError(message: String) {
        _errorMessage.value = message
    }

    fun showSuccess(message: String) {
        _successMessage.value = message
    }

    fun resetMessages() {
        _messages.value = emptyList()
    }

    private fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun getUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences("cycle_app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", null)
    }

    // ========== POLL FUNCTIONS ==========

    /**
     * Créer un sondage pour une sortie
     */
    fun createPoll(
        sortieId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean,
        context: Context
    ) {
        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isSending.value = false
                    return@launch
                }

                Log.d(TAG, "📊 Creating poll: $question with ${options.size} options")

                val createPollDto = CreatePollDto(
                    question = question,
                    options = options,
                    allowMultiple = allowMultiple
                )

                val response = com.example.dam.remote.RetrofitInstance.pollApi
                    .createPollForSortie("Bearer $token", sortieId, createPollDto)

                if (response.isSuccessful) {
                    val poll = response.body()
                    Log.d(TAG, "✅ Poll created: ${poll?.id}")
                    _successMessage.value = "Sondage créé avec succès"

                    // Recharger les sondages
                    loadPolls(sortieId, context)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Error creating poll: $errorBody")
                    _errorMessage.value = "Erreur lors de la création du sondage"
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception creating poll: ${e.message}", e)
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Charger les sondages d'une sortie
     */
    fun loadPolls(sortieId: String, context: Context) {
        viewModelScope.launch {
            _isLoadingPolls.value = true

            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    _isLoadingPolls.value = false
                    return@launch
                }

                Log.d(TAG, "📊 Loading polls for sortie: $sortieId")

                val response = com.example.dam.remote.RetrofitInstance.pollApi
                    .getSortiePolls("Bearer $token", sortieId, page = 1, limit = 50)

                if (response.isSuccessful) {
                    val pollResponse = response.body()
                    _polls.value = pollResponse?.polls ?: emptyList()
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "✅ Loaded ${_polls.value.size} polls")
                    _polls.value.forEach { poll ->
                        Log.d(TAG, "   📊 Poll: ${poll.question} (${poll.options.size} options, ${poll.totalVotes} votes)")
                    }
                    Log.d(TAG, "========================================")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Error loading polls: $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception loading polls: ${e.message}", e)
            } finally {
                _isLoadingPolls.value = false
            }
        }
    }

    /**
     * Voter sur un sondage
     */
    fun voteOnPoll(
        pollId: String,
        optionIds: List<String>,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token non trouvé"
                    return@launch
                }

                Log.d(TAG, "📊 Voting on poll: $pollId with options: $optionIds")

                val voteDto = VoteDto(optionIds = optionIds)

                val response = com.example.dam.remote.RetrofitInstance.pollApi
                    .voteOnPoll("Bearer $token", pollId, voteDto)

                if (response.isSuccessful) {
                    val updatedPoll = response.body()
                    Log.d(TAG, "✅ Vote recorded")
                    _successMessage.value = "Vote enregistré"

                    // Mettre à jour le sondage dans la liste
                    if (updatedPoll != null) {
                        _polls.value = _polls.value.map { poll ->
                            if (poll.id == updatedPoll.id) updatedPoll else poll
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Error voting: $errorBody")
                    _errorMessage.value = "Erreur lors du vote"
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception voting: ${e.message}", e)
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel cleared, leaving room")
        sendTimeoutJob?.cancel()
        leaveRoom()
    }
}
