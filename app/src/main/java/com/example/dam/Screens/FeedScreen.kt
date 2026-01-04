package com.example.dam.Screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dam.models.PublicationResponse
import com.example.dam.models.fullName
import com.example.dam.models.hasImage
import com.example.dam.utils.UserAvatarWithInitials
import com.example.dam.viewmodel.FeedViewModel
import com.example.dam.viewmodel.FeedUiState
import java.text.SimpleDateFormat
import java.util.*

// ==================== COULEURS ====================
private val BackgroundDark = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1A1A1A)
private val GlassBackground = Color(0xFF2A2A2A).copy(alpha = 0.4f)
private val GreenAccent = Color(0xFF4ADE80)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

// ==================== SCREEN PRINCIPAL ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController) {
    val context = LocalContext.current

    // ✅ ViewModel avec factory
    val viewModel: FeedViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FeedViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val publications by viewModel.publications.collectAsState()

    // Pull to refresh avec Material 3
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshFeed()
        }
    }

    // Arrêter le refresh une fois terminé
    LaunchedEffect(uiState) {
        if (uiState !is FeedUiState.Loading && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp) // ✅ Space for top bar (adjust based on your top bar height)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when (uiState) {
                is FeedUiState.Loading -> {
                    if (publications.isEmpty()) {
                        LoadingState()
                    } else {
                        // Afficher les publications pendant le refresh
                        FeedContent(
                            publications = publications,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }

                is FeedUiState.Success -> {
                    FeedContent(
                        publications = publications,
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                is FeedUiState.Empty -> {
                    EmptyState(navController = navController)
                }

                is FeedUiState.Error -> {
                    ErrorState(
                        message = (uiState as FeedUiState.Error).message,
                        onRetry = { viewModel.refreshFeed() }
                    )
                }
            }

            // Pull to refresh indicator Material 3
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color(0xFF1A1A1A),
                contentColor = GreenAccent
            )
        }
    }
}

// ==================== CONTENU DU FEED ====================
@Composable
private fun FeedContent(
    publications: List<PublicationResponse>,
    viewModel: FeedViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 96.dp // ✅ Space for bottom navigation bar (increased from 80dp)
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête avec bouton "What's on your mind?"
        item {
            FeedHeader(navController = navController)
        }

        // Liste des publications
        items(
            items = publications,
            key = { it.id }
        ) { publication ->
            PostCard(
                publication = publication,
                isLiked = viewModel.isLikedByCurrentUser(publication),
                onLikeClick = { viewModel.toggleLike(publication.id) },
                onCommentClick = { /* TODO: Navigation vers les commentaires */ },
                onShareClick = { /* TODO: Partage */ },
                onMenuClick = { /* TODO: Menu options */ }
            )
        }
    }
}

// ==================== HEADER DU FEED ====================
@Composable
fun FeedHeader(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = GlassBackground,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A).copy(alpha = 0.6f),
                            Color(0xFF2A2A2A).copy(alpha = 0.4f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recent Activity Feed",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "Stay connected with your community",
//                            color = TextSecondary,
//                            fontSize = 13.sp
//                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { /* TODO: Filter action */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton "What's on your mind?"
                Surface(
                    onClick = { navController.navigate("addpublication") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.Gray.copy(alpha = 0.3f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "User",
                                tint = TextSecondary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Text(
                            text = "What's on your mind?",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = GreenAccent.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = "Add image",
                                tint = GreenAccent,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== CARD DE PUBLICATION ====================
@Composable
fun PostCard(
    publication: PublicationResponse,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var localIsLiked by remember { mutableStateOf(isLiked) }
    var localLikesCount by remember { mutableStateOf(publication.likesCount) }
    var showShareDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF151515)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // ========== HEADER ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .border(2.dp, GreenAccent.copy(alpha = 0.3f), CircleShape)
                                .padding(3.dp)
                        ) {
                            UserAvatarWithInitials(
                                avatarUrl = publication.author?.avatar,
                                firstName = publication.author?.firstName,
                                lastName = publication.author?.lastName,
                                modifier = Modifier.fillMaxSize(),
                                backgroundColor = Color(0xFF374151),
                                textColor = GreenAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = publication.author?.fullName() ?: "Unknown User",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatTimestamp(publication.createdAt),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== CONTENU ==========
                Text(
                    text = publication.content,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // ========== IMAGE ==========
                if (publication.hasImage()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    ) {
                        AsyncImage(
                            model = publication.image,
                            contentDescription = "Post image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // ========== TAGS ==========
                if (!publication.tags.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        publication.tags.take(3).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GreenAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = GreenAccent,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ========== MENTIONS ==========
                if (!publication.mentions.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        publication.mentions.take(3).forEach { mention ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AlternateEmail,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${mention.firstName} ${mention.lastName}",
                                        color = Color(0xFF3B82F6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        // Show "and X more" if there are more mentions
                        if (publication.mentions.size > 3) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "+${publication.mentions.size - 3} more",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ========== LOCATION ==========
                if (!publication.location.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = publication.location,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== STATISTIQUES ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (localIsLiked) GreenAccent else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = formatNumber(localLikesCount),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "${publication.commentsCount} comments • ${publication.sharesCount} shares",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== BOUTONS D'INTERACTION ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InteractionButton(
                        icon = if (localIsLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "Like",
                        tint = if (localIsLiked) GreenAccent else TextSecondary,
                        onClick = {
                            localIsLiked = !localIsLiked
                            localLikesCount += if (localIsLiked) 1 else -1
                            onLikeClick()
                        }
                    )

                    InteractionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = "Comment",
                        tint = TextSecondary,
                        onClick = onCommentClick
                    )

                    InteractionButton(
                        icon = Icons.Outlined.Share,
                        label = "Share",
                        tint = TextSecondary,
                        onClick = { showShareDialog = true }
                    )
                }
            }
        }
    }

    // ✅ Share Dialog
    if (showShareDialog) {
        SharePublicationDialog(
            publication = publication,
            onDismiss = { showShareDialog = false }
        )
    }
}

// ==================== BOUTON D'INTERACTION ====================
@Composable
private fun InteractionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== ÉTATS ====================
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = GreenAccent,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading publications...",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmptyState(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🚴",
                fontSize = 64.sp
            )
            Text(
                text = "No publications yet",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Be the first to share your cycling journey!",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("addpublication") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Post")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌",
                fontSize = 64.sp
            )
            Text(
                text = "Oops! Something went wrong",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent
                )
            ) {
                Text("Retry")
            }
        }
    }
}

// ==================== SHARE PUBLICATION DIALOG ====================
@Composable
fun SharePublicationDialog(
    publication: PublicationResponse,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val messagesViewModel: com.example.dam.viewmodel.MessagesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return com.example.dam.viewmodel.MessagesViewModel() as T
            }
        }
    )
    val chatGroups by messagesViewModel.chatGroups.collectAsState()

    LaunchedEffect(Unit) {
        messagesViewModel.loadUserChats(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = GreenAccent
                )
                Text(
                    "Partager dans une discussion",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Sélectionnez une discussion:",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                if (chatGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = GreenAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Chargement...",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatGroups.size) { index ->
                            val chatGroup = chatGroups[index]
                            PublicationShareCard(
                                chatName = chatGroup.name,
                                chatEmoji = chatGroup.emoji,
                                onClick = {
                                    // Create structured share message with publication data
                                    val chatViewModel = com.example.dam.viewmodel.ChatViewModel()
                                    val authorName = "${publication.author?.firstName ?: ""} ${publication.author?.lastName ?: ""}".trim()
                                    val shareMessage = "SHARED_PUBLICATION:${publication.id}\nAUTHOR:$authorName\nCONTENT:${publication.content}\nIMAGE:${publication.image ?: ""}\nDATE:${publication.createdAt}"

                                    // Debug log
                                    android.util.Log.d("SharePublication", "========================================")
                                    android.util.Log.d("SharePublication", "📤 Sharing publication to chat: ${chatGroup.name}")
                                    android.util.Log.d("SharePublication", "Message to send:")
                                    android.util.Log.d("SharePublication", shareMessage)
                                    android.util.Log.d("SharePublication", "========================================")

                                    // Send to chat
                                    chatViewModel.sendTextMessage(chatGroup.sortieId, shareMessage, context)

                                    android.widget.Toast.makeText(
                                        context,
                                        "Publication partagée dans ${chatGroup.name}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        },
        containerColor = CardBackground,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun PublicationShareCard(
    chatName: String,
    chatEmoji: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = CardBackground.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground.copy(alpha = 0.4f))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GreenAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chatEmoji,
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "Discussion de groupe",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Share",
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==================== HELPERS ====================
private fun formatNumber(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(timestamp)

        if (date != null) {
            val diff = System.currentTimeMillis() - date.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 30 -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        } else {
            "Recently"
        }
    } catch (e: Exception) {
        "Recently"
    }
}