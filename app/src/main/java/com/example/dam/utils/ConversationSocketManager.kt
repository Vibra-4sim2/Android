// utils/ConversationSocketManager.kt
package com.example.dam.utils

import android.util.Log
import com.example.dam.models.*
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * ✅ SOCKET MANAGER POUR LES CONVERSATIONS PRIVÉES (1-1)
 * Complètement séparé du SocketService pour les chats de groupe
 * Namespace: /conversations
 */
class ConversationSocketManager private constructor() {

    companion object {
        private const val TAG = "ConversationSocket"
        private const val NAMESPACE = "/conversations"
        private var INSTANCE: ConversationSocketManager? = null

        fun getInstance(): ConversationSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConversationSocketManager().also { INSTANCE = it }
            }
        }
    }

    private var socket: Socket? = null
    private var serverUrl: String = ""
    private var jwtToken: String = ""
    private var currentUserId: String = ""

    // ==================== STATE FLOWS ====================
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation

    private val _messages = MutableStateFlow<List<DirectMessage>>(emptyList())
    val messages: StateFlow<List<DirectMessage>> = _messages

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ==================== INITIALIZATION ====================
    fun initialize(serverUrl: String, jwtToken: String, currentUserId: String) {
        this.serverUrl = serverUrl
        this.jwtToken = jwtToken
        this.currentUserId = currentUserId

        setupSocket()
    }

    private fun setupSocket() {
        try {
            val options = IO.Options().apply {
                // ✅ CORRECTION: Utiliser auth comme SocketService au lieu de query
                auth = mapOf("token" to jwtToken)
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 2000
                reconnectionDelayMax = 10000
                transports = arrayOf("websocket", "polling") // ✅ Fallback polling
                timeout = 30000 // ✅ 30 secondes pour Render cold start
                forceNew = false
                secure = true // ✅ Pour HTTPS
            }

            // ✅ CRITICAL: Connect to /conversations namespace (NOT /chat)
            socket = IO.socket("$serverUrl$NAMESPACE", options)
            setupEventListeners()

            Log.d(TAG, "✅ Socket initialized for namespace: $NAMESPACE")
            Log.d(TAG, "📡 Server URL: $serverUrl$NAMESPACE")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "❌ Socket initialization error", e)
            _errorMessage.value = "Erreur de connexion: ${e.message}"
        }
    }

    private fun setupEventListeners() {
        socket?.apply {
            // Connection lifecycle
            on(Socket.EVENT_CONNECT, onConnect)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            // ✅ CORRECTION: Utiliser les VRAIS noms d'événements du backend
            // Business events
            on("conversationsList", onConversations)  // Backend émet 'conversationsList'
            on("conversationReady", onConversationReady)  // Backend émet 'conversationReady' après initiateConversation
            on("receiveDirectMessage", onReceiveMessage)
            on("messagesList", onMessageHistory)  // Backend émet 'messagesList'
            on("messagesRead", onMessageRead)  // Backend émet 'messagesRead'
            on("userTyping", onUserTyping)
            on("error", onError)

            // ✅ Événements supplémentaires
            on("joinedConversation", onJoinedConversation)
            on("directMessageSent", onDirectMessageSent)
            on("markedAsRead", onMarkedAsRead)
            on("conversationDeleted", onConversationDeleted)
            on("conversationMuted", onConversationMuted)
            on("unreadCount", onUnreadCount)

            // Legacy fallbacks (au cas où)
            on("conversations", onConversations)
            on("conversationCreated", onConversationReady)
            on("messageHistory", onMessageHistory)
            on("messageRead", onMessageRead)
        }
    }

    // ==================== LIFECYCLE LISTENERS ====================
    private val onConnect = Emitter.Listener {
        Log.d(TAG, "🟢 Connected to $NAMESPACE namespace")
        _isConnected.value = true
        _errorMessage.value = null
        // ✅ RÉACTIVÉ: Auto-load conversations on connect
        getMyConversations()
    }

    private val onDisconnect = Emitter.Listener {
        Log.d(TAG, "🔴 Disconnected from $NAMESPACE")
        _isConnected.value = false
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.e(TAG, "❌ Connection error: ${args.contentToString()}")
        _isConnected.value = false
        _errorMessage.value = "Erreur de connexion au serveur"
    }

    // ==================== BUSINESS EVENT LISTENERS ====================
    private val onConversations = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 EVENT: conversationsList")
            Log.d(TAG, "Args count: ${args.size}")
            Log.d(TAG, "Args[0] type: ${args.getOrNull(0)?.javaClass?.simpleName}")
            Log.d(TAG, "Args[0] content: ${args.getOrNull(0)}")
            Log.d(TAG, "========================================")

            // ✅ CORRECTION: Backend émet { conversations: [...] } et non un array direct
            val data = args[0] as JSONObject
            val conversationsArray = data.getJSONArray("conversations")

            val conversationList = (0 until conversationsArray.length()).map { i ->
                Conversation.fromJson(conversationsArray.getJSONObject(i))
            }
            _conversations.value = conversationList
            Log.d(TAG, "📬 Received ${conversationList.size} conversations")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing conversations", e)
            Log.e(TAG, "Exception details: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            _errorMessage.value = "Erreur de chargement des conversations"
        }
    }

    private val onConversationReady = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 EVENT: conversationReady")
            Log.d(TAG, "Args count: ${args.size}")
            Log.d(TAG, "Args[0] type: ${args.getOrNull(0)?.javaClass?.simpleName}")
            Log.d(TAG, "Args[0] content: ${args.getOrNull(0)}")
            Log.d(TAG, "========================================")

            // Backend émet: { conversation: {...}, messages: [...], room: "conv_xxx" }
            val data = args[0] as JSONObject
            val conversationJson = data.getJSONObject("conversation")
            val conversation = Conversation.fromJson(conversationJson)
            _currentConversation.value = conversation
            Log.d(TAG, "✅ Conversation ready: ${conversation.id}")

            // Charger les messages initiaux si présents
            if (data.has("messages")) {
                val messagesArray = data.getJSONArray("messages")
                val messageList = (0 until messagesArray.length()).map { i ->
                    DirectMessage.fromJson(messagesArray.getJSONObject(i))
                }
                _messages.value = messageList
                Log.d(TAG, "✅ Loaded ${messageList.size} initial messages")
            }

            // Also update conversations list
            getMyConversations()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing conversationReady", e)
            Log.e(TAG, "Exception details: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            _errorMessage.value = "Erreur de création de conversation"
        }
    }

    private val onReceiveMessage = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 EVENT: receiveDirectMessage")
            Log.d(TAG, "Args[0] content: ${args.getOrNull(0)}")
            Log.d(TAG, "========================================")

            val data = args[0] as JSONObject

            // ✅ CORRECTION: Backend envoie { message: {...}, conversationId: "..." }
            val messageJson = if (data.has("message")) {
                data.getJSONObject("message")
            } else {
                // Fallback: si c'est directement le message
                data
            }

            val message = DirectMessage.fromJson(messageJson)

            // Add to messages list
            _messages.value = _messages.value + message

            Log.d(TAG, "📨 New message received: ${message.content?.take(50)}")

            // Update conversations list to reflect new message
            getMyConversations()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing received message", e)
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private val onMessageHistory = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 EVENT: messagesList")
            Log.d(TAG, "Args count: ${args.size}")
            Log.d(TAG, "Args[0] content: ${args.getOrNull(0)}")
            Log.d(TAG, "========================================")

            // ✅ CORRECTION: Backend émet { conversationId, messages: [...], hasMore }
            val data = args[0] as JSONObject
            val messagesArray = data.getJSONArray("messages")

            val messageList = (0 until messagesArray.length()).map { i ->
                DirectMessage.fromJson(messagesArray.getJSONObject(i))
            }
            _messages.value = messageList
            Log.d(TAG, "📜 Loaded ${messageList.size} messages")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing message history", e)
            Log.e(TAG, "Exception details: ${e.message}")
            _errorMessage.value = "Erreur de chargement des messages"
        }
    }

    private val onMessageRead = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")
            Log.d(TAG, "✓ Messages marked as read in conversation $conversationId")

            // Update local messages
            _messages.value = _messages.value.map { message ->
                if (message.conversationId == conversationId &&
                    message.senderId?.id == currentUserId) {
                    message.copy(
                        isRead = true,
                        status = DirectMessageStatus.READ
                    )
                } else {
                    message
                }
            }

            // Update conversations list
            getMyConversations()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing message read", e)
        }
    }

    private val onUserTyping = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val userId = data.getString("userId")
            val isTyping = data.getBoolean("isTyping")

            _typingUsers.value = if (isTyping) {
                _typingUsers.value + userId
            } else {
                _typingUsers.value - userId
            }

            Log.d(TAG, "⌨️ User $userId typing: $isTyping")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing typing event", e)
        }
    }

    private val onError = Emitter.Listener { args ->
        try {
            val error = args[0] as JSONObject
            val message = error.getString("message")
            Log.e(TAG, "❌ Server error: $message")
            _errorMessage.value = message
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing error event", e)
            _errorMessage.value = "Erreur serveur inconnue"
        }
    }

    // ✅ NOUVEAUX LISTENERS pour les événements manquants
    private val onJoinedConversation = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")

            if (data.has("messages")) {
                val messagesArray = data.getJSONArray("messages")
                val messageList = (0 until messagesArray.length()).map { i ->
                    DirectMessage.fromJson(messagesArray.getJSONObject(i))
                }
                _messages.value = messageList
                Log.d(TAG, "✅ Joined conversation $conversationId with ${messageList.size} messages")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing joinedConversation", e)
        }
    }

    private val onDirectMessageSent = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val messageId = data.optString("messageId")
            val success = data.optBoolean("success", false)
            Log.d(TAG, "✅ Message sent confirmation: $messageId, success: $success")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing directMessageSent", e)
        }
    }

    private val onMarkedAsRead = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")
            val success = data.optBoolean("success", false)
            Log.d(TAG, "✅ Marked as read: $conversationId, success: $success")

            // Rafraîchir les conversations pour mettre à jour le badge
            getMyConversations()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing markedAsRead", e)
        }
    }

    private val onConversationDeleted = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")
            val success = data.optBoolean("success", false)
            Log.d(TAG, "✅ Conversation deleted: $conversationId, success: $success")

            // Retirer de la liste locale
            _conversations.value = _conversations.value.filter { it.id != conversationId }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing conversationDeleted", e)
        }
    }

    private val onConversationMuted = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")
            val muted = data.optBoolean("muted", false)
            val success = data.optBoolean("success", false)
            Log.d(TAG, "✅ Conversation muted: $conversationId, muted: $muted, success: $success")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing conversationMuted", e)
        }
    }

    private val onUnreadCount = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            val conversationId = data.getString("conversationId")
            val count = data.getInt("count")
            Log.d(TAG, "📊 Unread count for $conversationId: $count")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing unreadCount", e)
        }
    }

    // ==================== PUBLIC METHODS ====================
    fun connect() {
        if (socket == null) {
            Log.e(TAG, "❌ Socket not initialized. Call initialize() first")
            return
        }
        socket?.connect()
        Log.d(TAG, "🔌 Connecting to $NAMESPACE...")
    }

    fun disconnect() {
        socket?.disconnect()
        Log.d(TAG, "🔌 Disconnected from $NAMESPACE")
    }

    fun getMyConversations() {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Not connected - cannot get conversations")
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📡 EMITTING: getMyConversations")
        Log.d(TAG, "Connected: ${_isConnected.value}")
        Log.d(TAG, "Socket ID: ${socket?.id()}")
        Log.d(TAG, "========================================")

        socket?.emit("getMyConversations")
        Log.d(TAG, "📡 Requesting conversations...")
    }

    fun initiateConversation(recipientId: String) {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Not connected - cannot initiate conversation")
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        val payload = JSONObject().apply {
            put("recipientId", recipientId)
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📡 EMITTING: initiateConversation")
        Log.d(TAG, "Recipient ID: $recipientId")
        Log.d(TAG, "Payload: $payload")
        Log.d(TAG, "Connected: ${_isConnected.value}")
        Log.d(TAG, "Socket ID: ${socket?.id()}")
        Log.d(TAG, "========================================")

        socket?.emit("initiateConversation", payload)
        Log.d(TAG, "💬 Initiating conversation with $recipientId")
    }

    fun joinConversation(conversationId: String) {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Not connected - cannot join conversation")
            return
        }

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("joinConversation", payload)
        Log.d(TAG, "🚪 Joined conversation $conversationId")
    }

    fun leaveConversation(conversationId: String) {
        if (!_isConnected.value) return

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("leaveConversation", payload)
        Log.d(TAG, "🚪 Left conversation $conversationId")
    }

    fun sendTextMessage(conversationId: String, content: String) {
        sendMessage(conversationId, DirectMessageType.TEXT, content = content)
    }

    fun sendMessage(
        conversationId: String,
        type: DirectMessageType,
        content: String? = null,
        mediaUrl: String? = null,
        thumbnailUrl: String? = null,
        mediaDuration: Int? = null,
        fileSize: Long? = null,
        fileName: String? = null,
        mimeType: String? = null,
        location: MessageLocation? = null,
        replyTo: String? = null,
        tempId: String? = null
    ) {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Not connected - cannot send message")
            _errorMessage.value = "Non connecté au serveur"
            return
        }

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("type", type.toString())
            content?.let { put("content", it) }
            mediaUrl?.let { put("mediaUrl", it) }
            thumbnailUrl?.let { put("thumbnailUrl", it) }
            mediaDuration?.let { put("mediaDuration", it) }
            fileSize?.let { put("fileSize", it) }
            fileName?.let { put("fileName", it) }
            mimeType?.let { put("mimeType", it) }
            location?.let {
                put("location", JSONObject().apply {
                    put("latitude", it.latitude)
                    put("longitude", it.longitude)
                    it.address?.let { addr -> put("address", addr) }
                })
            }
            replyTo?.let { put("replyTo", it) }
            tempId?.let { put("tempId", it) }
        }

        socket?.emit("sendDirectMessage", payload)
        Log.d(TAG, "📤 Sent message in conversation $conversationId")
    }

    fun getMessages(conversationId: String, limit: Int = 50, before: String? = null) {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Not connected - cannot get messages")
            return
        }

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("limit", limit)
            before?.let { put("before", it) }
        }
        socket?.emit("getMessages", payload)
        Log.d(TAG, "📡 Requesting messages for conversation $conversationId")
    }

    fun markAsRead(conversationId: String) {
        if (!_isConnected.value) return

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("markAsRead", payload)
        Log.d(TAG, "✓ Marking conversation as read: $conversationId")
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        if (!_isConnected.value) return

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("isTyping", isTyping)
        }
        socket?.emit("typing", payload)
    }

    fun deleteConversation(conversationId: String) {
        if (!_isConnected.value) return

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("deleteConversation", payload)
        Log.d(TAG, "🗑️ Deleting conversation: $conversationId")
    }

    fun muteConversation(conversationId: String, muted: Boolean) {
        if (!_isConnected.value) return

        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("muted", muted)
        }
        socket?.emit("muteConversation", payload)
        Log.d(TAG, "🔇 ${if (muted) "Muting" else "Unmuting"} conversation: $conversationId")
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setError(message: String) {
        _errorMessage.value = message
        Log.e(TAG, "❌ Error: $message")
    }

    fun cleanup() {
        socket?.off()
        socket?.disconnect()
        socket = null
        _isConnected.value = false
        _conversations.value = emptyList()
        _messages.value = emptyList()
        _currentConversation.value = null
        _typingUsers.value = emptySet()
        _errorMessage.value = null
        Log.d(TAG, "🧹 Cleaned up ConversationSocketManager")
    }
}

