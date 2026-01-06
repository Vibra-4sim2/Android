package com.example.dam.Screens

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dam.components.AudioMessageBubble
import com.example.dam.components.CreatePollDialog
import com.example.dam.components.PollMessageBubble
import com.example.dam.components.RecordingIndicator
import com.example.dam.models.MessageStatus
import com.example.dam.models.MessageType
import com.example.dam.models.MessageUI
import com.example.dam.models.Poll
import android.Manifest
import com.example.dam.utils.AudioRecorder
 import com.example.dam.utils.ChatStateManager
import com.example.dam.utils.ImagePickerUtil
import com.example.dam.utils.PermissionHelper
import com.example.dam.utils.rememberImagePickerLauncher
import com.example.dam.utils.rememberRecordAudioPermissionLauncher
import com.example.dam.viewmodel.ChatViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
@Composable
fun ChatConversationScreen(
    navController: NavHostController,
    sortieId: String,
    groupName: String,
    groupEmoji: String,
    participantsCount: String,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }

    // ✅ Mention system states
    var showMemberList by remember { mutableStateOf(false) }
    var mentionStartPosition by remember { mutableIntStateOf(-1) }
    val groupMembers = remember { mutableStateOf<List<com.example.dam.models.UserProfileResponse>>(emptyList()) }
    // États du ViewModel
    val messages by viewModel.messages.collectAsState()
    val polls by viewModel.polls.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()

    // Liste state pour auto-scroll
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Job pour l'indicateur de frappe
    var typingJob by remember { mutableStateOf<Job?>(null) }

    // ✅ ORDRE CHRONOLOGIQUE : Mélanger messages et sondages par date
    data class ChatItem(
        val id: String,
        val timestamp: Long,
        val type: String,
        val messageUI: MessageUI? = null,
        val poll: Poll? = null
    )

    val combinedItems = remember(messages, polls) {
        // Parser pour les dates ISO
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val messageItems = messages.map { msg ->
            val timestamp = try {
                // Essayer différents formats de parsing
                when {
                    // Si c'est déjà un Long en String
                    msg.timestamp.toLongOrNull() != null -> msg.timestamp.toLong()
                    // Si c'est un format ISO (ex: "2025-12-08T22:30:00.000Z")
                    msg.timestamp.contains("T") -> {
                        sdf.parse(msg.timestamp)?.time ?: run {
                            android.util.Log.w("ChatConversation", "⚠️ Impossible de parser msg timestamp: ${msg.timestamp}")
                            System.currentTimeMillis()
                        }
                    }
                    // Sinon, utiliser maintenant comme fallback
                    else -> {
                        android.util.Log.w("ChatConversation", "⚠️ Format timestamp inconnu pour message: ${msg.timestamp}")
                        System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatConversation", "❌ Erreur parsing timestamp message: ${e.message}")
                System.currentTimeMillis()
            }

            android.util.Log.d("ChatConversation", "📝 Message: ${msg.id} -> timestamp: $timestamp (${msg.timestamp})")

            ChatItem(
                id = msg.id,
                timestamp = timestamp,
                type = "message",
                messageUI = msg
            )
        }

        val pollItems = polls.map { poll ->
            val timestamp = try {
                sdf.parse(poll.createdAt)?.time ?: run {
                    android.util.Log.w("ChatConversation", "⚠️ Impossible de parser poll createdAt: ${poll.createdAt}")
                    System.currentTimeMillis()
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatConversation", "❌ Erreur parsing date poll: ${e.message}")
                System.currentTimeMillis()
            }

            android.util.Log.d("ChatConversation", "📊 Poll: ${poll.id} -> timestamp: $timestamp (${poll.createdAt})")

            ChatItem(
                id = poll.id,
                timestamp = timestamp,
                type = "poll",
                poll = poll
            )
        }

        val sorted = (messageItems + pollItems).sortedBy { it.timestamp }

        android.util.Log.d("ChatConversation", "========================================")
        android.util.Log.d("ChatConversation", "📊 TRI CHRONOLOGIQUE:")
        android.util.Log.d("ChatConversation", "   ${messageItems.size} messages + ${pollItems.size} polls = ${sorted.size} items")
        sorted.forEach { item ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(item.timestamp))
            android.util.Log.d("ChatConversation", "   ${item.type}: ${item.id} -> $date")
        }
        android.util.Log.d("ChatConversation", "========================================")

        sorted
    }

    // ✅ États pour l'enregistrement audio
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    // ✅ Permission RECORD_AUDIO Launcher
    val recordAudioPermissionLauncher = rememberRecordAudioPermissionLauncher(
        onPermissionGranted = {
            // Permission accordée, démarrer l'enregistrement
            android.util.Log.d("ChatConversation", "🎤 Permission accordée, démarrage enregistrement")
            audioRecorder.startRecording().fold(
                onSuccess = { file ->
                    android.util.Log.d("ChatConversation", "✅ Enregistrement démarré: ${file.absolutePath}")
                    isRecordingAudio = true
                    recordingDuration = 0

                    // Mettre à jour la durée toutes les secondes
                    recordingJob = coroutineScope.launch {
                        while (isRecordingAudio) {
                            delay(1000)
                            recordingDuration = audioRecorder.getCurrentDuration()
                        }
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("ChatConversation", "❌ Erreur enregistrement: ${error.message}")
                    viewModel.showError(error.message ?: "Impossible d'enregistrer")
                }
            )
        },
        onPermissionDenied = {
            // Permission refusée
            android.util.Log.e("ChatConversation", "❌ Permission RECORD_AUDIO refusée")
            viewModel.showError("Permission d'enregistrement audio requise pour envoyer des messages vocaux")
        }
    )

    // ✅ Image Picker Launcher
    val imagePickerLauncher = rememberImagePickerLauncher(
        onImageSelected = { uri ->
            android.util.Log.d("ChatConversation", "🖼️ Image sélectionnée: $uri")

            // Convertir Uri en File
            val imageFile = ImagePickerUtil.uriToFile(context, uri)

            if (imageFile != null) {
                // Valider l'image
                ImagePickerUtil.validateImage(imageFile).fold(
                    onSuccess = {
                        android.util.Log.d("ChatConversation", "✅ Image valide, envoi en cours...")
                        // Envoyer l'image via le ViewModel
                        viewModel.sendImageMessage(sortieId, imageFile, context)
                    },
                    onFailure = { error ->
                        android.util.Log.e("ChatConversation", "❌ Image invalide: ${error.message}")
                        viewModel.showError(error.message ?: "Image invalide")
                    }
                )
            } else {
                android.util.Log.e("ChatConversation", "❌ Impossible de convertir l'Uri en File")
                viewModel.showError("Impossible de charger l'image")
            }
        },
        onError = { error ->
            android.util.Log.e("ChatConversation", "❌ Erreur sélection image: $error")
            viewModel.showError(error)
        }
    )

    // ✅ Se connecter et rejoindre la room au démarrage
    LaunchedEffect(sortieId) {
        android.util.Log.d("ChatConversationScreen", "========================================")
        android.util.Log.d("ChatConversationScreen", "🚀 LaunchedEffect(sortieId) DÉCLENCHÉ")
        android.util.Log.d("ChatConversationScreen", "📍 sortieId: $sortieId")
        android.util.Log.d("ChatConversationScreen", "========================================")

        // ✅ MARK CHAT AS OPENED - This will hide the badge instantly
        ChatStateManager.markChatAsOpened(sortieId)
        android.util.Log.d("ChatConversationScreen", "✅ Chat marked as opened in ChatStateManager")

        // ✅ MARK AS READ in ReadMessagesManager for persistent badge state
        com.example.dam.utils.ReadMessagesManager.markChatAsRead(context, sortieId)
        android.util.Log.d("ChatConversationScreen", "✅ Chat marked as read in ReadMessagesManager")

        // ✅ Initialiser le contexte du ViewModel pour les WebSockets
        viewModel.setApplicationContext(context)

        viewModel.connectAndJoinRoom(sortieId, context)

        // ✅ Charger les sondages
        android.util.Log.d("ChatConversationScreen", "📊 Chargement des sondages...")
        viewModel.loadPolls(sortieId, context)

        // ✅ IMPORTANT: Mark messages as read immediately when entering chat
        android.util.Log.d("ChatConversationScreen", "📖 Marking all messages as read on entry...")
        viewModel.markAllMessagesAsRead(sortieId, context)

        // ✅ Load sortie participants for mentions
        android.util.Log.d("ChatConversationScreen", "👥 Loading sortie participants...")
        try {
            val token = com.example.dam.utils.UserPreferences.getToken(context)
            if (token != null) {
                // Step 1: Get sortie details
                val sortieResponse = com.example.dam.remote.RetrofitInstance.adventureApi.getSortieById(sortieId)
                if (sortieResponse.isSuccessful && sortieResponse.body() != null) {
                    val sortie = sortieResponse.body()!!

                    // Step 2: Collect all participant user IDs (including creator)
                    val participantUserIds = mutableSetOf<String>()

                    // Add creator
                    participantUserIds.add(sortie.createurId.id)

                    // Add all participants with status "ACCEPTED" or "PENDING"
                    sortie.participants.forEach { participant ->
                        participant.userId?.let { userId ->
                            if (participant.status == "ACCEPTED" || participant.status == "PENDING") {
                                participantUserIds.add(userId)
                            }
                        }
                    }

                    android.util.Log.d("ChatConversationScreen", "📋 Found ${participantUserIds.size} participant IDs")

                    // Step 3: Fetch user details for each participant
                    val usersList = mutableListOf<com.example.dam.models.UserProfileResponse>()

                    participantUserIds.forEach { userId ->
                        try {
                            val userResponse = com.example.dam.remote.RetrofitInstance.authApi.getUserById(userId, "Bearer $token")
                            if (userResponse.isSuccessful && userResponse.body() != null) {
                                usersList.add(userResponse.body()!!)
                                android.util.Log.d("ChatConversationScreen", "   ✅ Loaded: ${userResponse.body()!!.firstName} ${userResponse.body()!!.lastName}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ChatConversationScreen", "   ❌ Error loading user $userId: ${e.message}")
                        }
                    }

                    groupMembers.value = usersList
                    android.util.Log.d("ChatConversationScreen", "✅ Loaded ${usersList.size} participants for mentions")
                } else {
                    android.util.Log.e("ChatConversationScreen", "❌ Failed to load sortie: ${sortieResponse.code()}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatConversationScreen", "❌ Error loading participants: ${e.message}", e)
        }
    }

    // ✅ Monitor text changes for @ detection
    LaunchedEffect(messageText.text) {
        val text = messageText.text
        val lastAtIndex = text.lastIndexOf('@')

        if (lastAtIndex >= 0) {
            val beforeAt = if (lastAtIndex > 0) text[lastAtIndex - 1] else ' '
            val isValidStart = beforeAt == ' ' || lastAtIndex == 0

            if (isValidStart) {
                showMemberList = groupMembers.value.isNotEmpty()
                mentionStartPosition = lastAtIndex
                android.util.Log.d("ChatMention", "✅ @ detected at position $lastAtIndex, showing ${groupMembers.value.size} members")
            } else {
                showMemberList = false
            }
        } else {
            showMemberList = false
        }
    }

    // ✅ CLEANUP: Clear opened state when leaving chat
    DisposableEffect(sortieId) {
        onDispose {
            android.util.Log.d("ChatConversationScreen", "🧹 Leaving chat, clearing opened state for: $sortieId")
            ChatStateManager.clearOptimisticState(sortieId)
        }
    }

    // ✅ Auto-scroll quand un nouveau message arrive (index 0 = bas avec reverseLayout)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // ✅ Afficher les erreurs avec Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // ✅ Afficher les succès
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
        }
    }

    // ✅ Cleanup: Quitter la room quand on quitte l'écran
    DisposableEffect(Unit) {
        android.util.Log.d("ChatConversationScreen", "🎬 Screen entered for sortieId: $sortieId")

        onDispose {
            android.util.Log.d("ChatConversationScreen", "========================================")
            android.util.Log.d("ChatConversationScreen", "🚪 Leaving chat screen")
            android.util.Log.d("ChatConversationScreen", "📍 sortieId: $sortieId")

            // ✅ Force mark as read one more time before leaving
            android.util.Log.d("ChatConversationScreen", "📖 Final markAllMessagesAsRead() before leaving...")
            viewModel.forceMarkAllAsReadSync(sortieId, context)

            // ✅ DON'T clear optimistic state - keep badge hidden until new message arrives
            // ChatStateManager.clearOptimisticState(sortieId)  // REMOVED - Let badges stay hidden!
            android.util.Log.d("ChatConversationScreen", "✅ Keeping chat marked as read (not clearing optimistic state)")

            // Leave WebSocket room
            viewModel.leaveRoom()

            android.util.Log.d("ChatConversationScreen", "✅ Chat screen cleanup completed")
            android.util.Log.d("ChatConversationScreen", "========================================")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a3a2e),
                        Color(0xFF0d1f17),
                        Color(0xFF0a1510)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========== HEADER ==========
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1a3a2e),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF667eea), Color(0xFF764ba2))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = groupEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = groupName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // ✅ CORRIGÉ: Afficher le bon statut
                        when {
                            // 1. Si des utilisateurs écrivent
                            typingUsers.isNotEmpty() -> {
                                Text(
                                    text = "${typingUsers.size} personne(s) écrit...",
                                    color = Color(0xFF25D366),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // 2. Si NON connecté
                            !isConnected -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Red, CircleShape)
                                    )
                                    Text(
                                        text = "Connexion...",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            // 3. Si connecté (état normal) ✅
                            else -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF25D366), CircleShape)
                                    )
                                    Text(
                                        text = "$participantsCount participants",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = { /* Call */ }) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { /* Video call */ }) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Video",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { /* More */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                }
            }

            // ========== MESSAGES LIST ==========
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && messages.isEmpty() -> {
                        // ✅ Loading initial messages
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF25D366)
                            )
                            Text(
                                text = "Chargement des messages...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    !isConnected && messages.isEmpty() -> {
                        // ✅ Pas connecté
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = "Disconnected",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Non connecté",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Vérifiez votre connexion internet",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    messages.isEmpty() -> {
                        // ✅ Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "No messages",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun message",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Soyez le premier à envoyer un message!",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        // ✅ ORDRE CHRONOLOGIQUE : Messages et Polls mélangés par date
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            reverseLayout = true
                        ) {
                            // Items triés chronologiquement (reversed car reverseLayout)
                            items(
                                count = combinedItems.size,
                                key = { index -> combinedItems.reversed()[index].id }
                            ) { index ->
                                val item = combinedItems.reversed()[index]
                                when (item.type) {
                                    "message" -> {
                                        item.messageUI?.let { msg ->
                                            ChatMessageBubble(msg, navController)
                                        }
                                    }
                                    "poll" -> {
                                        item.poll?.let { poll ->
                                            PollMessageBubble(
                                                poll = poll,
                                                onVote = { optionIds ->
                                                    android.util.Log.d("ChatConversation", "📊 Vote sur poll: ${poll.id} options: $optionIds")
                                                    viewModel.voteOnPoll(poll.id, optionIds, context)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ========== RECORDING INDICATOR ==========
            AnimatedVisibility(
                visible = isRecordingAudio,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                RecordingIndicator(
                    duration = recordingDuration,
                    onCancel = {
                        android.util.Log.d("ChatConversation", "🚫 Annulation enregistrement")
                        audioRecorder.cancelRecording()
                        isRecordingAudio = false
                        recordingJob?.cancel()
                        recordingJob = null
                        recordingDuration = 0
                    },
                    onStop = {
                        android.util.Log.d("ChatConversation", "⏹️ Arrêt enregistrement")
                        audioRecorder.stopRecording().fold(
                            onSuccess = { result ->
                                android.util.Log.d("ChatConversation", "✅ Enregistrement terminé: ${result.file.name} (${result.durationSeconds}s)")

                                // Valider le fichier audio
                                AudioRecorder.validateAudioFile(result.file).fold(
                                    onSuccess = {
                                        // Envoyer le message vocal
                                        viewModel.sendAudioMessage(sortieId, result.file, result.durationSeconds, context)
                                    },
                                    onFailure = { error ->
                                        android.util.Log.e("ChatConversation", "❌ Audio invalide: ${error.message}")
                                        viewModel.showError(error.message ?: "Fichier audio invalide")
                                    }
                                )

                                // Réinitialiser l'état
                                isRecordingAudio = false
                                recordingJob?.cancel()
                                recordingJob = null
                                recordingDuration = 0
                            },
                            onFailure = { error ->
                                android.util.Log.e("ChatConversation", "❌ Erreur arrêt: ${error.message}")
                                viewModel.showError(error.message ?: "Erreur d'enregistrement")
                                isRecordingAudio = false
                                recordingJob?.cancel()
                                recordingJob = null
                                recordingDuration = 0
                            }
                        )
                    }
                )
            }

            // ========== ATTACHMENT OPTIONS ==========
            AnimatedVisibility(
                visible = showAttachmentOptions,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AttachmentOptionsPanel(
                    onDismiss = { showAttachmentOptions = false },
                    onImageClick = {
                        // ✅ Lancer le sélecteur d'image
                        android.util.Log.d("ChatConversation", "📸 Bouton Image cliqué")
                        showAttachmentOptions = false
                        imagePickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onCameraClick = {
                        // TODO: Implémenter la caméra
                        showAttachmentOptions = false
                    },
                    onFileClick = {
                        // TODO: Implémenter la sélection de fichier
                        showAttachmentOptions = false
                    },
                    onAudioClick = {
                        // TODO: Implémenter l'audio
                        showAttachmentOptions = false
                    },
                    onLocationClick = {
                        // TODO: Implémenter la localisation
                        showAttachmentOptions = false
                    },
                    onPollClick = {
                        showAttachmentOptions = false
                        showPollDialog = true
                    }
                )
            }

            // ========== INPUT BAR ==========
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1a3a2e),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAttachmentOptions) {
                        IconButton(onClick = { showAttachmentOptions = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showAttachmentOptions = true },
                            enabled = isConnected
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = if (isConnected) Color(0xFF25D366) else Color.Gray
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF2d5a45).copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = messageText,
                                onValueChange = { newValue ->
                                    messageText = newValue

                                    // ✅ Envoyer l'indicateur de frappe
                                    if (isConnected) {
                                        // Annuler le job précédent
                                        typingJob?.cancel()

                                        if (newValue.text.isNotEmpty()) {
                                            // Envoyer isTyping = true
                                            viewModel.sendTypingIndicator(sortieId, true)

                                            // Après 2 secondes d'inactivité, envoyer isTyping = false
                                            typingJob = coroutineScope.launch {
                                                delay(2000)
                                                viewModel.sendTypingIndicator(sortieId, false)
                                            }
                                        } else {
                                            // Si le texte est vide, envoyer isTyping = false immédiatement
                                            viewModel.sendTypingIndicator(sortieId, false)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = isConnected && !isSending,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF25D366)),
                                decorationBox = { innerTextField ->
                                    if (messageText.text.isEmpty()) {
                                        Text(
                                            text = if (isConnected) "Votre message..." else "Connexion...",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            IconButton(
                                onClick = { /* Emoji */ },
                                modifier = Modifier.size(24.dp),
                                enabled = isConnected
                            ) {
                                Icon(
                                    Icons.Default.Mood,
                                    contentDescription = "Emoji",
                                    tint = Color.White.copy(alpha = if (isConnected) 0.5f else 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ✅ Bouton d'envoi / microphone amélioré
                    FloatingActionButton(
                        onClick = {
                            when {
                                // Si texte présent : envoyer message texte
                                messageText.text.isNotBlank() && isConnected && !isSending -> {
                                    viewModel.sendTextMessage(sortieId, messageText.text.trim(), context)
                                    viewModel.sendTypingIndicator(sortieId, false)
                                    typingJob?.cancel()
                                    messageText = TextFieldValue("")
                                }
                                // Si texte vide : démarrer/arrêter enregistrement audio
                                messageText.text.isEmpty() && isConnected && !isSending -> {
                                    if (!isRecordingAudio) {
                                        // ✅ Vérifier la permission RECORD_AUDIO
                                        android.util.Log.d("ChatConversation", "🎤 Clic sur bouton microphone")
                                        if (PermissionHelper.hasRecordAudioPermission(context)) {
                                            // Permission déjà accordée, démarrer directement
                                            android.util.Log.d("ChatConversation", "✅ Permission déjà accordée, démarrage enregistrement")
                                            audioRecorder.startRecording().fold(
                                                onSuccess = { file ->
                                                    android.util.Log.d("ChatConversation", "✅ Enregistrement démarré: ${file.absolutePath}")
                                                    isRecordingAudio = true
                                                    recordingDuration = 0

                                                    // Mettre à jour la durée toutes les secondes
                                                    recordingJob = coroutineScope.launch {
                                                        while (isRecordingAudio) {
                                                            delay(1000)
                                                            recordingDuration = audioRecorder.getCurrentDuration()
                                                        }
                                                    }
                                                },
                                                onFailure = { error ->
                                                    android.util.Log.e("ChatConversation", "❌ Erreur enregistrement: ${error.message}")
                                                    viewModel.showError(error.message ?: "Impossible d'enregistrer")
                                                }
                                            )
                                        } else {
                                            // Demander la permission
                                            android.util.Log.d("ChatConversation", "📋 Demande de permission RECORD_AUDIO")
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = when {
                            isRecordingAudio -> Color.Red
                            isConnected -> Color(0xFF25D366)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        when {
                            isSending -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                            isRecordingAudio -> {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop Recording",
                                    tint = Color.White
                                )
                            }
                            messageText.text.isEmpty() -> {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = Color.White
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ✅ Member mention popup
        if (showMemberList && groupMembers.value.isNotEmpty()) {
            android.util.Log.d("ChatMention", "🎨 Rendering popup with ${groupMembers.value.size} members")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp)
                        .background(
                            color = Color(0xFF2A2A2A),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF3B82F6),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(vertical = 8.dp)
                ) {
                    items(groupMembers.value.size) { index ->
                        val member = groupMembers.value[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val beforeMention = messageText.text.substring(0, mentionStartPosition)
                                    val afterMention = messageText.text.substring(mentionStartPosition + 1)
                                    val newText = "$beforeMention@${member.firstName} ${member.lastName} $afterMention"
                                    messageText = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newText.length)
                                    )
                                    showMemberList = false
                                    android.util.Log.d("ChatMention", "👤 Selected: ${member.firstName} ${member.lastName}")
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            if (!member.avatar.isNullOrEmpty()) {
                                AsyncImage(
                                    model = member.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${member.firstName.firstOrNull()?.uppercaseChar() ?: ""}${member.lastName.firstOrNull()?.uppercaseChar() ?: ""}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${member.firstName} ${member.lastName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ✅ Snackbar pour les messages d'erreur/succès
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    // ========== POLL CREATION DIALOG ==========
    if (showPollDialog) {
        CreatePollDialog(
            onDismiss = { showPollDialog = false },
            onCreatePoll = { question, options, allowMultiple ->
                android.util.Log.d("ChatConversation", "📊 Création sondage: $question avec ${options.size} options")
                viewModel.createPoll(
                    sortieId = sortieId,
                    question = question,
                    options = options,
                    allowMultiple = allowMultiple,
                    context = context
                )
                showPollDialog = false
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: MessageUI,
    navController: NavHostController
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMe) {
            // ✅ Avatar for other users - CLICKABLE to navigate to profile
            if (message.authorAvatar != null) {
                AsyncImage(
                    model = message.authorAvatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            // ✅ Navigate to user profile when avatar is clicked
                            message.senderId?.let { senderId ->
                                android.util.Log.d("ChatBubble", "🔄 Navigating to profile: $senderId")
                                try {
                                    // Navigate to the user's profile screen
                                    navController.navigate("userProfile/$senderId") {
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatBubble", "❌ Navigation error: ${e.message}")
                                }
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFdc4e41), CircleShape)
                        .clickable {
                            // ✅ Navigate to user profile when avatar is clicked
                            message.senderId?.let { senderId ->
                                android.util.Log.d("ChatBubble", "🔄 Navigating to profile: $senderId")
                                try {
                                    // Navigate to the user's profile screen
                                    navController.navigate("userProfile/$senderId") {
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatBubble", "❌ Navigation error: ${e.message}")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.author.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!message.isMe) {
                Text(
                    text = message.author,
                    color = Color(0xFF25D366),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isMe) 18.dp else 4.dp,
                    topEnd = if (message.isMe) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (message.isMe) Color(0xFF056162) else Color(0xFF2d4a3e)
            ) {
                Column {
                    // ✅ Audio (Message vocal)
                    if (message.type == MessageType.AUDIO && message.audioUrl != null) {
                        AudioMessageBubble(
                            audioUrl = message.audioUrl,
                            durationSeconds = message.audioDuration ?: 0,
                            isMe = message.isMe,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Note: Le timestamp et le statut sont déjà affichés dans AudioMessageBubble
                    }

                    // ✅ Image
                    else if (message.type == MessageType.IMAGE && message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Shared image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = if (message.isMe) 18.dp else 4.dp,
                                        topEnd = if (message.isMe) 4.dp else 18.dp,
                                        bottomStart = if (message.content.isNullOrEmpty()) 18.dp else 4.dp,
                                        bottomEnd = if (message.content.isNullOrEmpty()) 18.dp else 4.dp
                                    )
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // ✅ SHARED SORTIE/PUBLICATION CARD - Check if message contains shared content
                    val isSharedSortie = message.content?.startsWith("SHARED_SORTIE:") == true
                    val isSharedPublication = message.content?.startsWith("SHARED_PUBLICATION:") == true

                    // DEBUG LOGGING
                    if (message.content != null) {
                        android.util.Log.d("ChatCard", "========================================")
                        android.util.Log.d("ChatCard", "Message ID: ${message.id}")
                        android.util.Log.d("ChatCard", "Message content preview: ${message.content.take(100)}")
                        android.util.Log.d("ChatCard", "Starts with SHARED_SORTIE: $isSharedSortie")
                        android.util.Log.d("ChatCard", "Starts with SHARED_PUBLICATION: $isSharedPublication")
                        android.util.Log.d("ChatCard", "========================================")
                    }

                    // ✅ Render SharedSortieCard if it's a shared sortie
                    if (isSharedSortie && message.content != null) {
                        SharedSortieCard(
                            messageContent = message.content,
                            navController = navController
                        )
                    }

                    // ✅ Render SharedPublicationCard if it's a shared publication
                    if (isSharedPublication && message.content != null) {
                        SharedPublicationCard(
                            messageContent = message.content,
                            navController = navController
                        )
                    }

                        // Text content (but NOT for shared content)
                        if (!message.content.isNullOrEmpty() &&
                            message.type != MessageType.AUDIO &&
                            !isSharedSortie &&
                            !isSharedPublication) {
                            Row(
                                modifier = Modifier.padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = if (message.imageUrl != null) 8.dp else 10.dp,
                                    bottom = 10.dp
                                ),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = message.content,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = message.time,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                    if (message.isMe) {
                                        Icon(
                                            when (message.status) {
                                                MessageStatus.SENDING -> Icons.Default.Schedule
                                                MessageStatus.SENT -> Icons.Default.Done
                                                MessageStatus.DELIVERED, MessageStatus.READ -> Icons.Default.DoneAll
                                                MessageStatus.FAILED -> Icons.Default.Error
                                            },
                                            contentDescription = "Status",
                                            tint = when (message.status) {
                                                MessageStatus.READ -> Color(0xFF53bdeb)
                                                MessageStatus.FAILED -> Color.Red
                                                else -> Color.White.copy(alpha = 0.6f)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else if (message.imageUrl != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message.time,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                if (message.isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Status",
                                        tint = if (message.status == MessageStatus.READ)
                                            Color(0xFF53bdeb)
                                        else
                                            Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun AttachmentOptionsPanel(
    @Suppress("UNUSED_PARAMETER") onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onFileClick: () -> Unit,
    onAudioClick: () -> Unit,
    onLocationClick: () -> Unit,
    onPollClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2d4a3e),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Image",
                    color = Color(0xFF9C27B0),
                    onClick = onImageClick
                )
                AttachmentOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Caméra",
                    color = Color(0xFF2196F3),
                    onClick = onCameraClick
                )
                AttachmentOption(
                    icon = Icons.Default.Description,
                    label = "Fichier",
                    color = Color(0xFFFF9800),
                    onClick = onFileClick
                )
                AttachmentOption(
                    icon = Icons.Default.MusicNote,
                    label = "Audio",
                    color = Color(0xFFE91E63),
                    onClick = onAudioClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                AttachmentOption(
                    icon = Icons.Default.LocationOn,
                    label = "Position",
                    color = Color(0xFF25D366),
                    onClick = onLocationClick
                )
                AttachmentOption(
                    icon = Icons.Default.PieChart,
                    label = "Sondage",
                    color = Color(0xFFFF5722),
                    onClick = onPollClick
                )
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(72.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = color,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ✅ Shared Sortie Card Component
@Composable
fun SharedSortieCard(
    messageContent: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Parse the shared sortie data
    val lines = messageContent.split("\n")

    // ✅ Handle both formats:
    // 1. Simple format: "SHARED_SORTIE:sortieId"
    // 2. Detailed format: "SHARED_SORTIE:sortieId\nTITLE:...\nCREATOR:..."
    val sortieId = if (lines.isNotEmpty()) {
        val firstLine = lines[0]
        if (firstLine.startsWith("SHARED_SORTIE:")) {
            firstLine.substringAfter("SHARED_SORTIE:").trim()
        } else {
            lines.find { it.startsWith("SHARED_SORTIE:") }?.substringAfter(":")?.trim() ?: ""
        }
    } else {
        ""
    }

    val title = lines.find { it.startsWith("TITLE:") }?.substringAfter(":")?.trim() ?: "Sortie partagée"
    val creator = lines.find { it.startsWith("CREATOR:") }?.substringAfter(":")?.trim() ?: "Utilisateur"
    val imageUrl = lines.find { it.startsWith("IMAGE:") }?.substringAfter(":")?.trim() ?: ""
    val type = lines.find { it.startsWith("TYPE:") }?.substringAfter(":")?.trim() ?: ""

    android.util.Log.d("SharedSortieCard", "========================================")
    android.util.Log.d("SharedSortieCard", "📦 Parsing shared sortie message")
    android.util.Log.d("SharedSortieCard", "Message content: $messageContent")
    android.util.Log.d("SharedSortieCard", "Parsed sortieId: $sortieId")
    android.util.Log.d("SharedSortieCard", "Parsed title: $title")
    android.util.Log.d("SharedSortieCard", "Parsed creator: $creator")
    android.util.Log.d("SharedSortieCard", "========================================")

    Surface(
        onClick = {
            if (sortieId.isNotEmpty()) {
                android.util.Log.d("SharedSortieCard", "📍 Navigating to: sortieDetail/$sortieId")
                try {
                    // Navigate to sortie detail screen
                    navController.navigate("sortieDetail/$sortieId") {
                        launchSingleTop = true
                        // Avoid putting the same destination multiple times on the back stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SharedSortieCard", "❌ Navigation error: ${e.message}", e)
                }
            } else {
                android.util.Log.e("SharedSortieCard", "❌ Empty sortie ID, cannot navigate")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF2d4a3e).copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1a3a2e).copy(alpha = 0.9f),
                            Color(0xFF1a3a2e).copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sortie Image
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Sortie image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1a3a2e)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF4ADE80).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            "VELO" -> Icons.Default.DirectionsBike
                            "RANDONNEE" -> Icons.Default.Hiking
                            else -> Icons.Default.Explore
                        },
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Sortie Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Shared indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Sortie partagée",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Creator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = creator,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View",
                tint = Color(0xFF4ADE80),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ✅ Shared Publication Card Component
@Composable
fun SharedPublicationCard(
    messageContent: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Parse the shared publication data
    val lines = messageContent.split("\n")

    // ✅ Handle both formats:
    // 1. Simple format: "SHARED_PUBLICATION:publicationId"
    // 2. Detailed format: "SHARED_PUBLICATION:publicationId\nAUTHOR:...\nCONTENT:..."
    val publicationId = if (lines.isNotEmpty()) {
        val firstLine = lines[0]
        if (firstLine.startsWith("SHARED_PUBLICATION:")) {
            firstLine.substringAfter("SHARED_PUBLICATION:").trim()
        } else {
            lines.find { it.startsWith("SHARED_PUBLICATION:") }?.substringAfter(":")?.trim() ?: ""
        }
    } else {
        ""
    }

    val author = lines.find { it.startsWith("AUTHOR:") }?.substringAfter(":")?.trim() ?: "Utilisateur"
    val content = lines.find { it.startsWith("CONTENT:") }?.substringAfter(":")?.trim() ?: ""
    val imageUrl = lines.find { it.startsWith("IMAGE:") }?.substringAfter(":")?.trim() ?: ""
    val date = lines.find { it.startsWith("DATE:") }?.substringAfter(":")?.trim() ?: ""

    android.util.Log.d("SharedPublicationCard", "========================================")
    android.util.Log.d("SharedPublicationCard", "📦 Parsing shared publication message")
    android.util.Log.d("SharedPublicationCard", "Message content: $messageContent")
    android.util.Log.d("SharedPublicationCard", "Parsed publicationId: $publicationId")
    android.util.Log.d("SharedPublicationCard", "Parsed author: $author")
    android.util.Log.d("SharedPublicationCard", "Parsed content: $content")
    android.util.Log.d("SharedPublicationCard", "========================================")

    Surface(
        onClick = {
            if (publicationId.isNotEmpty()) {
                android.util.Log.d("SharedPublicationCard", "📍 Navigating to feed (publication shared)")
                try {
                    // Navigate to feed screen where publications are displayed
                    navController.navigate("feed") {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SharedPublicationCard", "❌ Navigation error: ${e.message}", e)
                }
            } else {
                android.util.Log.e("SharedPublicationCard", "❌ Empty publication ID, cannot navigate")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF2d3a4a).copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1a2a3a).copy(alpha = 0.9f),
                            Color(0xFF1a2a3a).copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Publication Image or Icon
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Publication image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1a2a3a)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Publication Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Shared indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Publication partagée",
                        color = Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Content preview
                if (content.isNotEmpty()) {
                    Text(
                        text = content,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Author
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = author,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

