// remote/SocketService.kt
package com.example.dam.remote

import android.util.Log
import com.example.dam.models.CreateMessageDto
import com.example.dam.models.MessageResponse
import com.google.gson.Gson
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
    private const val SOCKET_URL = "http://10.0.2.2:3000/chat" // Émulateur Android
    // Pour appareil physique, utilise : "http://TON_IP:3000/chat"

    private var socket: Socket? = null
    private val gson = Gson()

    // Callbacks pour les événements
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onMessageReceived: ((MessageResponse) -> Unit)? = null
    var onJoinedRoom: ((List<MessageResponse>) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onUserTyping: ((String, Boolean) -> Unit)? = null

    /**
     * Se connecter au serveur WebSocket
     * @param token Token JWT d'authentification
     */
    fun connect(token: String) {
        try {
            if (socket?.connected() == true) {
                Log.d(TAG, "Already connected")
                return
            }

            Log.d(TAG, "Connecting to $SOCKET_URL")

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 10000
            }

            socket = IO.socket(SOCKET_URL, options)

            // Événement : Connexion établie
            socket?.on(Socket.EVENT_CONNECT, onConnect)

            // Événement : Déconnexion
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)

            // Événement : Erreur de connexion
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            // Événement : Confirmation de connexion du serveur
            socket?.on("connected", onServerConnected)

            // Événement : Rejoindre une room avec succès
            socket?.on("joinedRoom", onRoomJoined)

            // Événement : Nouveau message reçu
            socket?.on("receiveMessage", onReceiveMessage)

            // Événement : Utilisateur en train de taper
            socket?.on("userTyping", onUserTypingEvent)

            // Événement : Erreur générale
            socket?.on("error", onErrorEvent)

            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket connection error: ${e.message}", e)
            onError?.invoke("Erreur de connexion: ${e.message}")
        }
    }

    /**
     * Se déconnecter du serveur
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting...")
        socket?.disconnect()
        socket?.off() // Enlever tous les listeners
        socket = null
    }

    /**
     * Rejoindre une room de chat
     * @param sortieId ID de la sortie
     */
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

    /**
     * Quitter une room
     * @param sortieId ID de la sortie
     */
    fun leaveRoom(sortieId: String) {
        if (!isConnected()) return

        Log.d(TAG, "Leaving room for sortie: $sortieId")

        val data = JSONObject().apply {
            put("sortieId", sortieId)
        }

        socket?.emit("leaveRoom", data)
    }

    /**
     * Envoyer un message
     * @param sortieId ID de la sortie
     * @param messageDto Contenu du message
     */
    fun sendMessage(sortieId: String, messageDto: CreateMessageDto) {
        if (!isConnected()) {
            Log.e(TAG, "Cannot send message: not connected")
            onError?.invoke("Non connecté au serveur")
            return
        }

        Log.d(TAG, "Sending message to sortie: $sortieId")

        val data = JSONObject().apply {
            put("sortieId", sortieId)
            put("type", messageDto.type.name.lowercase())
            messageDto.content?.let { put("content", it) }
            messageDto.mediaUrl?.let { put("mediaUrl", it) }
            messageDto.thumbnailUrl?.let { put("thumbnailUrl", it) }
            messageDto.mediaDuration?.let { put("mediaDuration", it) }
            messageDto.fileSize?.let { put("fileSize", it) }
            messageDto.fileName?.let { put("fileName", it) }
            messageDto.mimeType?.let { put("mimeType", it) }
            messageDto.replyTo?.let { put("replyTo", it) }
        }

        socket?.emit("sendMessage", data)
    }

    /**
     * Envoyer l'indicateur de frappe
     * @param sortieId ID de la sortie
     * @param isTyping true si en train de taper
     */
    fun sendTypingIndicator(sortieId: String, isTyping: Boolean) {
        if (!isConnected()) return

        val data = JSONObject().apply {
            put("sortieId", sortieId)
            put("isTyping", isTyping)
        }

        socket?.emit("typing", data)
    }

    /**
     * Marquer un message comme lu
     * @param messageId ID du message
     * @param sortieId ID de la sortie
     */
    fun markAsRead(messageId: String, sortieId: String) {
        if (!isConnected()) return

        val data = JSONObject().apply {
            put("messageId", messageId)
            put("sortieId", sortieId)
        }

        socket?.emit("markAsRead", data)
    }

    /**
     * Vérifier si connecté
     */
    fun isConnected(): Boolean = socket?.connected() == true

    // ========== Event Handlers ==========

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "✅ Socket connected")
        onConnected?.invoke()
    }

    private val onDisconnect = Emitter.Listener {
        Log.d(TAG, "❌ Socket disconnected")
        onDisconnected?.invoke()
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = args.getOrNull(0)
        Log.e(TAG, "💥 Connection error: $error")
        onError?.invoke("Erreur de connexion: $error")
    }

    private val onServerConnected = Emitter.Listener { args ->
        val data = args.getOrNull(0) as? JSONObject
        val message = data?.optString("message") ?: "Connected"
        Log.d(TAG, "🟢 Server says: $message")
    }

    private val onRoomJoined = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val messagesArray = data?.optJSONArray("messages")

            if (messagesArray != null) {
                val messages = mutableListOf<MessageResponse>()
                for (i in 0 until messagesArray.length()) {
                    val messageJson = messagesArray.getJSONObject(i).toString()
                    val message = gson.fromJson(messageJson, MessageResponse::class.java)
                    messages.add(message)
                }

                Log.d(TAG, "🏠 Joined room, received ${messages.size} messages")
                onJoinedRoom?.invoke(messages)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing joined room data: ${e.message}", e)
        }
    }

    private val onReceiveMessage = Emitter.Listener { args ->
        try {
            val data = args.getOrNull(0) as? JSONObject
            val messageJson = data?.optJSONObject("message")?.toString()

            if (messageJson != null) {
                val message = gson.fromJson(messageJson, MessageResponse::class.java)
                Log.d(TAG, "📨 Message received: ${message.id}")
                onMessageReceived?.invoke(message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}", e)
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

    private val onErrorEvent = Emitter.Listener { args ->
        val data = args.getOrNull(0) as? JSONObject
        val message = data?.optString("message") ?: "Unknown error"
        Log.e(TAG, "⚠️ Server error: $message")
        onError?.invoke(message)
    }
}