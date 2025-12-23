package com.example.dam.remote

import android.util.Log
import com.example.dam.models.CreateMessageDto
import com.example.dam.models.MessageResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Service pour gérer la connexion WebSocket avec Socket.IO
 */
object SocketService {

    private const val TAG = "SocketService"


    private const val BASE_URL = "https://dam-4sim2.onrender.com"
    private const val SOCKET_NAMESPACE = "/chat"

    private var socket: Socket? = null
    // ✅ CORRIGÉ: URL du serveur déployé sur Render




    //private const val BASE_URL = "http://192.168.1.169:3000/"
    // private const val BASE_URL = "http://10.0.2.2:3000/"


    // private const val BASE_URL = "http://192.168.1.169:3000/"
    // ✅ CORRIGÉ: Gson avec gestion des nulls
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    // Callbacks pour les événements
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onMessageReceived: ((MessageResponse) -> Unit)? = null
    var onJoinedRoom: ((List<MessageResponse>) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onUserTyping: ((String, Boolean) -> Unit)? = null
    var onMessageSent: ((String, Boolean) -> Unit)? = null

    // ========== POLL CALLBACKS ==========
    var onPollCreated: ((String) -> Unit)? = null  // pollId
    var onPollUpdated: ((String) -> Unit)? = null  // pollId (après un vote)
    var onPollClosed: ((String) -> Unit)? = null   // pollId

    /**
     * Se connecter au serveur WebSocket
     */
    fun connect(token: String) {
        try {
            if (socket?.connected() == true) {
                Log.d(TAG, "Already connected")
                return
            }

            val socketUrl = "$BASE_URL$SOCKET_NAMESPACE"
            Log.d(TAG, "Connecting to $socketUrl")

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 2000
                reconnectionDelayMax = 10000
                transports = arrayOf("websocket", "polling") // ✅ Ajout du polling comme fallback
                timeout = 30000 // ✅ Augmenté à 30 secondes pour Render (cold start)
                forceNew = false
                secure = true // ✅ Pour HTTPS
            }

            socket = IO.socket(socketUrl, options)
            setupListeners()
            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket connection error: ${e.message}", e)
            onError?.invoke("Erreur de connexion: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            onError?.invoke("Erreur inattendue: ${e.message}")
        }
    }

    private fun setupListeners() {
        socket?.apply {
            // ✅ CRITIQUE: Nettoyer les listeners existants pour éviter les doublons
            off(Socket.EVENT_CONNECT)
            off(Socket.EVENT_DISCONNECT)
            off(Socket.EVENT_CONNECT_ERROR)
            off("connected")
            off("joinedRoom")
            off("receiveMessage")
            off("messageSent")
            off("userTyping")
            off("messageRead")
            off("error")
            off("pollCreated")
            off("pollUpdated")
            off("pollClosed")

            // Puis ajouter les nouveaux listeners
            on(Socket.EVENT_CONNECT, onConnect)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            on("connected", onServerConnected)
            on("joinedRoom", onRoomJoined)
            on("receiveMessage", onReceiveMessage)
            on("messageSent", onMessageSentEvent)
            on("userTyping", onUserTypingEvent)
            on("messageRead", onMessageReadEvent)
            on("error", onErrorEvent)
            on("pollCreated", onPollCreatedEvent)
            on("pollUpdated", onPollUpdatedEvent)
            on("pollClosed", onPollClosedEvent)

            Log.d(TAG, "✅ Listeners configurés (doublons évités + polls)")
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting...")
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun joinRoom(sortieId: String) {
        if (!isConnected()) {
            Log.e(TAG, "Cannot join room: not connected")
            onError?.invoke("Non connecté au serveur")
            return
        }

        Log.d(TAG, "Joining room for sortie: $sortieId")

        val data = JSONObject().apply {
            put("sortieId", sortieId)
        }

        socket?.emit("joinRoom", data)
    }

    fun leaveRoom(sortieId: String) {
        if (!isConnected()) return

        Log.d(TAG, "Leaving room for sortie: $sortieId")

        val data = JSONObject().apply {
            put("sortieId", sortieId)
        }

        socket?.emit("leaveRoom", data)
    }

    /**
     * ✅ CORRIGÉ: Envoyer un message avec type en minuscules
     */
    fun sendMessage(sortieId: String, messageDto: CreateMessageDto) {
        if (!isConnected()) {
            Log.e(TAG, "Cannot send message: not connected")
            onError?.invoke("Non connecté au serveur")
            return
        }

        Log.d(TAG, "Sending message to sortie: $sortieId, type: ${messageDto.type}")

        val data = JSONObject().apply {
            put("sortieId", sortieId)
            // ✅ CORRIGÉ: Envoyer le type en minuscules
            put("type", messageDto.type.toLowerCaseString())

            messageDto.content?.let { put("content", it) }
            messageDto.mediaUrl?.let { put("mediaUrl", it) }
            messageDto.thumbnailUrl?.let { put("thumbnailUrl", it) }
            messageDto.mediaDuration?.let { put("mediaDuration", it) }
            messageDto.fileSize?.let { put("fileSize", it) }
            messageDto.fileName?.let { put("fileName", it) }
            messageDto.mimeType?.let { put("mimeType", it) }

            messageDto.location?.let { location ->
                val locationObj = JSONObject().apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    location.address?.let { put("address", it) }
                    location.name?.let { put("name", it) }
                }
                put("location", locationObj)
            }

            messageDto.replyTo?.let { put("replyTo", it) }
        }

        Log.d(TAG, "📤 Data envoyée: $data")
        socket?.emit("sendMessage", data)
    }

    fun sendTypingIndicator(sortieId: String, isTyping: Boolean) {
        if (!isConnected()) return

        val data = JSONObject().apply {
            put("sortieId", sortieId)
            put("isTyping", isTyping)
        }

        socket?.emit("typing", data)
    }

    fun markAsRead(messageId: String, sortieId: String) {
        if (!isConnected()) return

        Log.d(TAG, "Marking message as read: $messageId")

        val data = JSONObject().apply {
            put("messageId", messageId)
            put("sortieId", sortieId)
        }

        socket?.emit("markAsRead", data)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    // ========== Event Handlers ==========

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "✅ Socket connected")
        onConnected?.invoke()
    }

    private val onDisconnect = Emitter.Listener { args ->
        val reason = args.getOrNull(0)?.toString() ?: "unknown"
        Log.d(TAG, "❌ Socket disconnected: $reason")
        onDisconnected?.invoke()
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = args.getOrNull(0)
        val errorType = error?.javaClass?.simpleName ?: "Unknown"
        val errorMsg = error?.toString() ?: "Unknown error"

        Log.e(TAG, "========================================")
        Log.e(TAG, "💥 SOCKET CONNECTION ERROR")
        Log.e(TAG, "========================================")
        Log.e(TAG, "Type: $errorType")
        Log.e(TAG, "Message: $errorMsg")
        Log.e(TAG, "========================================")

        when {
            errorMsg.contains("timeout", ignoreCase = true) -> {
                onError?.invoke("Erreur de connexion: timeout. Le serveur met du temps à répondre (cold start possible).")
            }
            errorMsg.contains("Authentication", ignoreCase = true) -> {
                onError?.invoke("Erreur d'authentification. Reconnectez-vous.")
            }
            errorMsg.contains("ECONNREFUSED", ignoreCase = true) -> {
                onError?.invoke("Erreur de connexion: serveur inaccessible.")
            }
            else -> {
                onError?.invoke("Erreur de connexion: $errorMsg")
            }
        }
    }

    private val onServerConnected = Emitter.Listener { args ->
        val data = args.getOrNull(0) as? JSONObject
        val message = data?.optString("message") ?: "Connected"
        val userId = data?.optString("userId")
        Log.d(TAG, "🟢 Server connected: $message (userId: $userId)")
    }

    /**
     * ✅ CORRIGÉ: Parser les messages avec senderId pouvant être un objet ou null
     */
    private val onRoomJoined = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🏠 EVENT: joinedRoom")
            Log.d(TAG, "========================================")

            val data = args.getOrNull(0) as? JSONObject
            val sortieId = data?.optString("sortieId")
            val messagesArray = data?.optJSONArray("messages")

            Log.d(TAG, "📍 sortieId: $sortieId")
            Log.d(TAG, "📨 Nombre de messages: ${messagesArray?.length() ?: 0}")

            if (messagesArray != null) {
                val messages = mutableListOf<MessageResponse>()

                for (i in 0 until messagesArray.length()) {
                    try {
                        val messageJson = messagesArray.getJSONObject(i).toString()
                        val message = gson.fromJson(messageJson, MessageResponse::class.java)
                        messages.add(message)

                        val preview = message.content?.take(30) ?:
                        message.type.uppercase()
                        Log.d(TAG, "  ✅ Message $i: $preview")

                    } catch (e: Exception) {
                        Log.e(TAG, "  ❌ Erreur parsing message $i: ${e.message}")
                        // Continuer avec les autres messages
                    }
                }

                Log.d(TAG, "✅ ${messages.size} messages parsés avec succès")
                onJoinedRoom?.invoke(messages)

            } else {
                Log.d(TAG, "ℹ️ Aucun message dans la room")
                onJoinedRoom?.invoke(emptyList())
            }

            Log.d(TAG, "========================================")

        } catch (e: Exception) {
            Log.e(TAG, "💥 Erreur parsing joinedRoom", e)
            Log.e(TAG, "Message: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            onError?.invoke("Erreur de parsing: ${e.message}")
        }
    }

    private val onReceiveMessage = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val messageJson = data?.optJSONObject("message")?.toString()
            val sortieId = data?.optString("sortieId")

            if (messageJson != null) {
                val message = gson.fromJson(messageJson, MessageResponse::class.java)
                Log.d(TAG, "📨 New message received: ${message._id} (sortie: $sortieId)")
                onMessageReceived?.invoke(message)
            } else {
                Log.e(TAG, "Received message with null data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}", e)
            onError?.invoke("Erreur de parsing du message")
        }
    }

    private val onMessageSentEvent = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val messageId = data?.optString("messageId") ?: ""
            val success = data?.optBoolean("success") ?: false

            Log.d(TAG, "✅ Message sent confirmation: $messageId (success: $success)")
            onMessageSent?.invoke(messageId, success)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing messageSent: ${e.message}", e)
        }
    }

    private val onUserTypingEvent = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val userId = data?.optString("userId") ?: ""
            val isTyping = data?.optBoolean("isTyping") ?: false

            Log.d(TAG, "⌨️ User $userId typing: $isTyping")
            onUserTyping?.invoke(userId, isTyping)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing typing event: ${e.message}", e)
        }
    }

    private val onMessageReadEvent = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val messageId = data?.optString("messageId")
            val userId = data?.optString("userId")

            Log.d(TAG, "👁️ Message read: $messageId by $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message read: ${e.message}", e)
        }
    }

    private val onErrorEvent = Emitter.Listener { args ->
        Log.e(TAG, "========================================")
        Log.e(TAG, "⚠️ EVENT: error")
        Log.e(TAG, "========================================")

        val data = args.getOrNull(0) as? JSONObject
        Log.e(TAG, "📦 Data d'erreur: $data")

        val message = data?.optString("message") ?: "Unknown error"
        val statusCode = data?.optInt("statusCode")

        Log.e(TAG, "❌ Code: $statusCode")
        Log.e(TAG, "❌ Message: $message")
        Log.e(TAG, "========================================")

        onError?.invoke(message)
    }

    // ========== POLL EVENTS ==========

    private val onPollCreatedEvent = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📊 EVENT: pollCreated")
            Log.d(TAG, "========================================")

            val data = args.getOrNull(0) as? JSONObject
            val pollId = data?.optString("pollId")
            val sortieId = data?.optString("sortieId")

            Log.d(TAG, "📊 New poll created: $pollId in sortie: $sortieId")

            if (pollId != null) {
                onPollCreated?.invoke(pollId)
            }

            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error parsing pollCreated: ${e.message}", e)
        }
    }

    private val onPollUpdatedEvent = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📊 EVENT: pollUpdated")
            Log.d(TAG, "========================================")

            val data = args.getOrNull(0) as? JSONObject
            val pollId = data?.optString("pollId")
            val sortieId = data?.optString("sortieId")

            Log.d(TAG, "📊 Poll updated: $pollId in sortie: $sortieId")

            if (pollId != null) {
                onPollUpdated?.invoke(pollId)
            }

            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error parsing pollUpdated: ${e.message}", e)
        }
    }

    private val onPollClosedEvent = Emitter.Listener { args ->
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📊 EVENT: pollClosed")
            Log.d(TAG, "========================================")

            val data = args.getOrNull(0) as? JSONObject
            val pollId = data?.optString("pollId")
            val sortieId = data?.optString("sortieId")

            Log.d(TAG, "📊 Poll closed: $pollId in sortie: $sortieId")

            if (pollId != null) {
                onPollClosed?.invoke(pollId)
            }

            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error parsing pollClosed: ${e.message}", e)
        }
    }
}
