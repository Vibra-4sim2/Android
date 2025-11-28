package com.example.dam.Screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dam.models.MessageStatus
import com.example.dam.models.MessageType
import com.example.dam.models.MessageUI
import com.example.dam.viewmodel.ChatViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // États du ViewModel
    val messages by viewModel.messages.collectAsState()
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

    // ✅ Se connecter et rejoindre la room au démarrage
    LaunchedEffect(sortieId) {
        android.util.Log.d("ChatConversationScreen", "========================================")
        android.util.Log.d("ChatConversationScreen", "🚀 LaunchedEffect(sortieId) DÉCLENCHÉ")
        android.util.Log.d("ChatConversationScreen", "📍 sortieId: $sortieId")
        android.util.Log.d("ChatConversationScreen", "🔍 États UI actuels:")
        android.util.Log.d("ChatConversationScreen", "   isConnected: $isConnected")
        android.util.Log.d("ChatConversationScreen", "   isSending: $isSending")
        android.util.Log.d("ChatConversationScreen", "   isLoading: $isLoading")
        android.util.Log.d("ChatConversationScreen", "   messages count: ${messages.size}")
        android.util.Log.d("ChatConversationScreen", "🔄 Appel de viewModel.connectAndJoinRoom()...")
        android.util.Log.d("ChatConversationScreen", "========================================")
        viewModel.connectAndJoinRoom(sortieId, context)
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
        android.util.Log.d("ChatConversationScreen", "========================================")
        android.util.Log.d("ChatConversationScreen", "🎬 DisposableEffect CRÉÉ pour sortieId: $sortieId")
        android.util.Log.d("ChatConversationScreen", "========================================")

        onDispose {
            android.util.Log.d("ChatConversationScreen", "========================================")
            android.util.Log.d("ChatConversationScreen", "🚪 DisposableEffect onDispose APPELÉ")
            android.util.Log.d("ChatConversationScreen", "📍 sortieId concerné: $sortieId")
            android.util.Log.d("ChatConversationScreen", "🔄 Appel de viewModel.leaveRoom()...")
            android.util.Log.d("ChatConversationScreen", "========================================")
            viewModel.leaveRoom()
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
                        // ✅ Messages list
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            reverseLayout = true
                        ) {
                            items(messages.reversed(), key = { it.id }) { message ->
                                ChatMessageBubble(message)
                            }
                        }
                    }
                }
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
                        // TODO: Implémenter la sélection d'image
                        showAttachmentOptions = false
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

                    // ✅ Bouton d'envoi amélioré
                    FloatingActionButton(
                        onClick = {
                            if (messageText.text.isNotBlank() && isConnected && !isSending) {
                                // Envoyer le message
                                viewModel.sendTextMessage(sortieId, messageText.text.trim(), context)

                                // Envoyer isTyping = false
                                viewModel.sendTypingIndicator(sortieId, false)
                                typingJob?.cancel()

                                // Vider le champ
                                messageText = TextFieldValue("")
                            }
                        },
                        containerColor = if (isConnected) Color(0xFF25D366) else Color.Gray,
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

        // ✅ Snackbar pour les messages d'erreur/succès
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = Color(0xFF2d4a3e),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun ChatMessageBubble(message: MessageUI) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMe) {
            // Avatar for other users
            if (message.authorAvatar != null) {
                AsyncImage(
                    model = message.authorAvatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFdc4e41), CircleShape),
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
                    // ✅ Image
                    if (message.type == MessageType.IMAGE && message.imageUrl != null) {
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

                    // Text content
                    if (!message.content.isNullOrEmpty()) {
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
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onFileClick: () -> Unit,
    onAudioClick: () -> Unit,
    onLocationClick: () -> Unit
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
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}