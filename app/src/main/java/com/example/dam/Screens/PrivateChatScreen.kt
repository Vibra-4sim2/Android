// Screens/PrivateChatScreen.kt
package com.example.dam.Screens

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dam.components.RecordingIndicator
import com.example.dam.models.*
import com.example.dam.ui.theme.*
import com.example.dam.utils.AudioRecorder
import com.example.dam.utils.ImagePickerUtil
import com.example.dam.utils.PermissionHelper
import com.example.dam.utils.UserAvatar
import com.example.dam.utils.rememberImagePickerLauncher
import com.example.dam.utils.rememberRecordAudioPermissionLauncher
import com.example.dam.viewmodel.ConversationsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ✅ SCREEN DE CHAT PRIVÉ (1-1)
 * Utilisé pour les conversations entre deux utilisateurs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatScreen(
    navController: NavHostController,
    conversationId: String,
    otherUserId: String,
    otherUserName: String,
    otherUserAvatar: String?,
    viewModel: ConversationsViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    var typingJob by remember { mutableStateOf<Job?>(null) }

    // ✅ Media states
    var showAttachmentOptions by remember { mutableStateOf(false) }

    // ✅ Audio recording states
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    // États du ViewModel
    val messages by viewModel.messagesUI.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    // Liste state pour auto-scroll
    val listState = rememberLazyListState()

    // ✅ Snackbar host for messages
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Permission RECORD_AUDIO Launcher
    val recordAudioPermissionLauncher = rememberRecordAudioPermissionLauncher(
        onPermissionGranted = {
            android.util.Log.d("PrivateChatScreen", "🎤 Permission accordée, démarrage enregistrement")
            audioRecorder.startRecording().fold(
                onSuccess = { file ->
                    android.util.Log.d("PrivateChatScreen", "✅ Enregistrement démarré: ${file.absolutePath}")
                    isRecordingAudio = true
                    recordingDuration = 0
                    recordingJob = coroutineScope.launch {
                        while (isRecordingAudio) {
                            delay(1000)
                            recordingDuration = audioRecorder.getCurrentDuration()
                        }
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("PrivateChatScreen", "❌ Erreur enregistrement: ${error.message}")
                    viewModel.showError(error.message ?: "Impossible d'enregistrer")
                }
            )
        },
        onPermissionDenied = {
            android.util.Log.e("PrivateChatScreen", "❌ Permission RECORD_AUDIO refusée")
            viewModel.showError("Permission d'enregistrement audio requise")
        }
    )

    // ✅ Image Picker Launcher
    val imagePickerLauncher = rememberImagePickerLauncher(
        onImageSelected = { uri ->
            android.util.Log.d("PrivateChatScreen", "🖼️ Image sélectionnée: $uri")
            val imageFile = ImagePickerUtil.uriToFile(context, uri)
            if (imageFile != null) {
                ImagePickerUtil.validateImage(imageFile).fold(
                    onSuccess = {
                        android.util.Log.d("PrivateChatScreen", "✅ Image valide, envoi en cours...")
                        viewModel.sendImageWithUpload(conversationId, imageFile, context)
                    },
                    onFailure = { error ->
                        android.util.Log.e("PrivateChatScreen", "❌ Image invalide: ${error.message}")
                        viewModel.showError(error.message ?: "Image invalide")
                    }
                )
            } else {
                viewModel.showError("Impossible de charger l'image")
            }
        },
        onError = { error ->
            android.util.Log.e("PrivateChatScreen", "❌ Erreur sélection image: $error")
            viewModel.showError(error)
        }
    )

    // ✅ Initialize and open conversation
    LaunchedEffect(conversationId) {
        viewModel.initialize(context)
        delay(500) // Wait for connection
        viewModel.openConversation(conversationId)
    }

    // ✅ Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // ✅ Show errors with Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // ✅ Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.closeConversation(conversationId)
            // Cancel recording if active
            if (isRecordingAudio) {
                audioRecorder.cancelRecording()
                recordingJob?.cancel()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    } else {
                        listOf(BackgroundLightGradientStart, BackgroundLight, BackgroundLightGradientEnd)
                    }
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== HEADER ====================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardDark.copy(alpha = 0.4f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }

                    // User avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenAccent.copy(alpha = 0.2f))
                            .clickable {
                                // Navigate to user profile
                                navController.navigate("userProfile/$otherUserId")
                            }
                    ) {
                        if (otherUserAvatar != null) {
                            AsyncImage(
                                model = otherUserAvatar,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            com.example.dam.utils.UserAvatar(
                                avatarUrl = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // User name and status
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = otherUserName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        // Typing indicator or connection status
                        when {
                            typingUsers.contains(otherUserId) -> {
                                Text(
                                    text = "En train d'écrire...",
                                    fontSize = 12.sp,
                                    color = GreenAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            !isConnected -> {
                                Text(
                                    text = "Connexion...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            else -> {
                                Text(
                                    text = "En ligne",
                                    fontSize = 12.sp,
                                    color = GreenAccent
                                )
                            }
                        }
                    }

                    // More options
                    IconButton(onClick = { /* TODO: Show options menu */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // ==================== MESSAGES LIST ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    !isConnected -> {
                        // Connection indicator
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = GreenAccent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Connexion au serveur...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                    errorMessage != null -> {
                        // Error state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = ErrorRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage ?: "Erreur inconnue",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.clearError()
                                    viewModel.openConversation(conversationId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                            ) {
                                Text("Réessayer")
                            }
                        }
                    }
                    messages.isEmpty() -> {
                        // Empty state
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
                                tint = TextTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun message",
                                color = TextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Commencez la conversation !",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        // Messages list
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = messages,
                                key = { it.id }
                            ) { message ->
                                PrivateMessageBubble(
                                    message = message,
                                    otherUserAvatar = otherUserAvatar,
                                    otherUserName = otherUserName
                                )
                            }
                        }
                    }
                }
            }

            // ==================== RECORDING INDICATOR ====================
            AnimatedVisibility(
                visible = isRecordingAudio,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                RecordingIndicator(
                    duration = recordingDuration,
                    onCancel = {
                        android.util.Log.d("PrivateChatScreen", "🚫 Annulation enregistrement")
                        audioRecorder.cancelRecording()
                        isRecordingAudio = false
                        recordingJob?.cancel()
                        recordingJob = null
                        recordingDuration = 0
                    },
                    onStop = {
                        android.util.Log.d("PrivateChatScreen", "⏹️ Arrêt enregistrement")
                        audioRecorder.stopRecording().fold(
                            onSuccess = { result ->
                                android.util.Log.d("PrivateChatScreen", "✅ Enregistrement terminé: ${result.file.name} (${result.durationSeconds}s)")

                                // Valider le fichier audio
                                AudioRecorder.validateAudioFile(result.file).fold(
                                    onSuccess = {
                                        // Envoyer le message vocal
                                        viewModel.sendAudioWithUpload(conversationId, result.file, result.durationSeconds, context)
                                    },
                                    onFailure = { error ->
                                        android.util.Log.e("PrivateChatScreen", "❌ Audio invalide: ${error.message}")
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
                                android.util.Log.e("PrivateChatScreen", "❌ Erreur arrêt: ${error.message}")
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

            // ==================== ATTACHMENT OPTIONS ====================
            AnimatedVisibility(
                visible = showAttachmentOptions,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PrivateChatAttachmentPanel(
                    onImageClick = {
                        android.util.Log.d("PrivateChatScreen", "📸 Bouton Image cliqué")
                        showAttachmentOptions = false
                        imagePickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onFileClick = {
                        android.util.Log.d("PrivateChatScreen", "📁 Bouton Fichier cliqué")
                        showAttachmentOptions = false
                        // TODO: Implement file picker
                        viewModel.showError("Fonctionnalité fichier bientôt disponible")
                    }
                )
            }

            // ==================== INPUT BAR ====================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1a3a2e),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ Attachment button / Close button
                    if (showAttachmentOptions) {
                        IconButton(onClick = { showAttachmentOptions = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showAttachmentOptions = true },
                            enabled = isConnected && !isSending
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Pièces jointes",
                                tint = if (isConnected && !isSending) GreenAccent else TextSecondary
                            )
                        }
                    }

                    // ✅ Message input field - Style like group chat
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF2d5a45).copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = messageText,
                                onValueChange = { newText ->
                                    messageText = newText

                                    // Send typing indicator
                                    typingJob?.cancel()
                                    if (newText.text.isNotEmpty()) {
                                        viewModel.setTyping(conversationId, true)
                                        typingJob = coroutineScope.launch {
                                            delay(2000)
                                            viewModel.setTyping(conversationId, false)
                                        }
                                    } else {
                                        viewModel.setTyping(conversationId, false)
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = if (isConnected) "Votre message..." else "Connexion...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = GreenAccent
                                ),
                                modifier = Modifier.weight(1f),
                                maxLines = 4,
                                enabled = isConnected && !isSending
                            )

                            // Emoji button like group chat
                            IconButton(
                                onClick = { /* Emoji picker */ },
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

                    // ✅ Send / Microphone button - Green like group chat
                    FloatingActionButton(
                        onClick = {
                            when {
                                // Si texte présent → envoyer message texte
                                messageText.text.isNotBlank() && isConnected && !isSending -> {
                                    viewModel.sendMessage(conversationId, messageText.text.trim())
                                    viewModel.setTyping(conversationId, false)
                                    typingJob?.cancel()
                                    messageText = TextFieldValue("")
                                }
                                // Si texte vide → démarrer/arrêter enregistrement audio
                                messageText.text.isEmpty() && isConnected && !isSending -> {
                                    if (!isRecordingAudio) {
                                        // Vérifier la permission RECORD_AUDIO
                                        android.util.Log.d("PrivateChatScreen", "🎤 Clic sur bouton microphone")
                                        if (PermissionHelper.hasRecordAudioPermission(context)) {
                                            // Permission déjà accordée, démarrer directement
                                            android.util.Log.d("PrivateChatScreen", "✅ Permission déjà accordée, démarrage enregistrement")
                                            audioRecorder.startRecording().fold(
                                                onSuccess = { file ->
                                                    android.util.Log.d("PrivateChatScreen", "✅ Enregistrement démarré: ${file.absolutePath}")
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
                                                    android.util.Log.e("PrivateChatScreen", "❌ Erreur enregistrement: ${error.message}")
                                                    viewModel.showError(error.message ?: "Impossible d'enregistrer")
                                                }
                                            )
                                        } else {
                                            // Demander la permission
                                            android.util.Log.d("PrivateChatScreen", "🔐 Demande permission RECORD_AUDIO")
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(52.dp),
                        containerColor = GreenAccent,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (messageText.text.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                                contentDescription = if (messageText.text.isNotBlank()) "Envoyer" else "Microphone",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // ✅ Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==================== MESSAGE BUBBLE ====================
@Composable
fun PrivateMessageBubble(
    message: DirectMessageUI,
    otherUserAvatar: String?,
    otherUserName: String
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        // Avatar for received messages
        if (!message.isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GreenAccent.copy(alpha = 0.2f))
            ) {
                if (otherUserAvatar != null) {
                    AsyncImage(
                        model = otherUserAvatar,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    UserAvatar(
                        avatarUrl = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.isMe) 16.dp else 4.dp,
                topEnd = if (message.isMe) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (message.isMe) GreenAccent.copy(alpha = 0.2f) else CardDark,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (message.isMe) GreenAccent.copy(alpha = 0.3f) else BorderColor
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Message content based on type
                when (message.type) {
                    DirectMessageType.TEXT -> {
                        Text(
                            text = message.content ?: "",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    DirectMessageType.IMAGE -> {
                        // ✅ AFFICHAGE DE L'IMAGE
                        PrivateImageMessage(
                            imageUrl = message.mediaUrl,
                            caption = message.content
                        )
                    }
                    DirectMessageType.AUDIO -> {
                        // ✅ LECTEUR AUDIO
                        PrivateAudioMessage(
                            audioUrl = message.mediaUrl,
                            duration = message.audioDuration ?: 0,
                            context = context
                        )
                    }
                    DirectMessageType.VIDEO -> {
                        // ✅ AFFICHAGE VIDÉO (thumbnail + play)
                        PrivateVideoMessage(
                            videoUrl = message.mediaUrl,
                            thumbnailUrl = message.thumbnailUrl,
                            duration = message.audioDuration
                        )
                    }
                    DirectMessageType.FILE -> {
                        // ✅ AFFICHAGE FICHIER
                        PrivateFileMessage(
                            fileName = message.fileName ?: "Fichier",
                            fileSize = message.fileSize,
                            fileUrl = message.mediaUrl
                        )
                    }
                    DirectMessageType.LOCATION -> {
                        Text(
                            text = "📍 Position partagée",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and status
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.time,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    if (message.isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.status) {
                                DirectMessageStatus.SENT -> Icons.Default.Done
                                DirectMessageStatus.DELIVERED -> Icons.Default.DoneAll
                                DirectMessageStatus.READ -> Icons.Default.DoneAll
                                DirectMessageStatus.FAILED -> Icons.Default.Error
                            },
                            contentDescription = message.status.toString(),
                            tint = when (message.status) {
                                DirectMessageStatus.READ -> GreenAccent
                                DirectMessageStatus.FAILED -> ErrorRed
                                else -> TextSecondary
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Spacer for sent messages
        if (message.isMe) {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

// ==================== IMAGE MESSAGE ====================
@Composable
private fun PrivateImageMessage(
    imageUrl: String?,
    caption: String?
) {
    Column {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 250.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder si pas d'URL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(CardDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Image",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Caption si présent
        if (!caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = caption,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== AUDIO MESSAGE ====================
@Composable
private fun PrivateAudioMessage(
    audioUrl: String?,
    duration: Int,
    context: android.content.Context
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    // Cleanup media player on dispose
    DisposableEffect(audioUrl) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isPlaying) GreenAccent.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        IconButton(
            onClick = {
                if (audioUrl == null) return@IconButton

                if (isPlaying) {
                    // Pause
                    mediaPlayer?.pause()
                    isPlaying = false
                } else {
                    // Play
                    if (mediaPlayer == null) {
                        mediaPlayer = android.media.MediaPlayer().apply {
                            try {
                                setDataSource(audioUrl)
                                prepareAsync()
                                setOnPreparedListener { mp ->
                                    mp.start()
                                    isPlaying = true
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    currentProgress = 0f
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PrivateAudioMessage", "Error playing audio: ${e.message}")
                            }
                        }
                    } else {
                        mediaPlayer?.start()
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(GreenAccent, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Waveform placeholder + progress
        Column(modifier = Modifier.weight(1f)) {
            // Waveform bars (simple representation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(20) { index ->
                    val height = remember { (8..24).random().dp }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height)
                            .background(
                                if (index < (currentProgress * 20).toInt()) GreenAccent
                                else TextSecondary.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Duration
            Text(
                text = formatAudioDuration(duration),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ==================== VIDEO MESSAGE ====================
@Composable
private fun PrivateVideoMessage(
    videoUrl: String?,
    thumbnailUrl: String?,
    duration: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardDark)
            .clickable {
                // TODO: Open video player
            },
        contentAlignment = Alignment.Center
    ) {
        // Thumbnail
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Play button overlay
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Duration badge
        if (duration != null && duration > 0) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = formatAudioDuration(duration),
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ==================== FILE MESSAGE ====================
@Composable
private fun PrivateFileMessage(
    fileName: String,
    fileSize: Long?,
    fileUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable {
                // TODO: Download/open file
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File icon
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = GreenAccent.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.InsertDriveFile,
                    contentDescription = "File",
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // File info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            if (fileSize != null && fileSize > 0) {
                Text(
                    text = formatFileSize(fileSize),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Download icon
        Icon(
            Icons.Default.Download,
            contentDescription = "Download",
            tint = GreenAccent,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==================== HELPER FUNCTIONS ====================
private fun formatAudioDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, secs)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

// ==================== ATTACHMENT PANEL ====================
@Composable
fun PrivateChatAttachmentPanel(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Options row - Style like group chat (2 options)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Image option - Purple
                PrivateAttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Image",
                    color = Color(0xFF9C27B0),
                    onClick = onImageClick
                )

                // File option - Pink
                PrivateAttachmentOption(
                    icon = Icons.Default.Description,
                    label = "Fichier",
                    color = Color(0xFFE91E63),
                    onClick = onFileClick
                )
            }
        }
    }
}

@Composable
private fun PrivateAttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        // Square rounded button like group chat
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color,
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

