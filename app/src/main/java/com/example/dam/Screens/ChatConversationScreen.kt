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
import kotlinx.coroutines.launch

@Composable
fun ChatConversationScreen(
    navController: NavHostController,
    groupId: String,
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
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Liste state pour auto-scroll
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Charger les messages au démarrage
//    LaunchedEffect(groupId) {
//        viewModel.resetPagination()
//        viewModel.loadMessages(groupId, context)
//    }

    // Auto-scroll quand un nouveau message arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Afficher les erreurs
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // TODO: Afficher un Snackbar avec l'erreur
            viewModel.clearError()
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
            // Header
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
                        Text(
                            text = "$participantsCount participants",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
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

            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && messages.isEmpty() -> {
                        // Loading initial messages
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF25D366)
                        )
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
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                ChatMessageBubble(message)
                            }
                        }
                    }
                }
            }

            // Attachment Options
            AnimatedVisibility(
                visible = showAttachmentOptions,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AttachmentOptionsPanel(
                    onDismiss = { showAttachmentOptions = false }
                )
            }

            // Input Bar
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
                        IconButton(onClick = { showAttachmentOptions = true }) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = Color(0xFF25D366)
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
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF25D366)),
                                decorationBox = { innerTextField ->
                                    if (messageText.text.isEmpty()) {
                                        Text(
                                            text = "Votre message...",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            IconButton(
                                onClick = { /* Emoji */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Mood,
                                    contentDescription = "Emoji",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageText.text.isNotBlank()) {
                                viewModel.sendTextMessage(groupId, messageText.text, context)
                                messageText = TextFieldValue("")
                            }
                        },
                        containerColor = Color(0xFF25D366),
                        modifier = Modifier.size(48.dp),
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
                                if (messageText.text.isEmpty()) Icons.Default.Mic else Icons.Default.Send,
                                contentDescription = if (messageText.text.isEmpty()) "Voice" else "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
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
                        text = message.author.firstOrNull()?.toString() ?: "?",
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
            if (!message.isMe && message.content?.isNotEmpty() == true) {
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
                    // Image
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
fun AttachmentOptionsPanel(onDismiss: () -> Unit) {
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
                    onClick = { /* TODO: Handle Image */ }
                )
                AttachmentOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Caméra",
                    color = Color(0xFF2196F3),
                    onClick = { /* TODO: Handle Camera */ }
                )
                AttachmentOption(
                    icon = Icons.Default.Description,
                    label = "Fichier",
                    color = Color(0xFFFF9800),
                    onClick = { /* TODO: Handle File */ }
                )
                AttachmentOption(
                    icon = Icons.Default.MusicNote,
                    label = "Audio",
                    color = Color(0xFFE91E63),
                    onClick = { /* TODO: Handle Audio */ }
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
                    onClick = { /* TODO: Handle Location */ }
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