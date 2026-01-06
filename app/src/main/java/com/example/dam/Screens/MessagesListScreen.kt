package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dam.models.ChatGroupUI
import com.example.dam.ui.theme.*
import com.example.dam.utils.ChatStateManager
import com.example.dam.viewmodel.MessagesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessagesListScreen(
    navController: NavHostController,
    viewModel: MessagesViewModel = viewModel(),
    conversationsViewModel: com.example.dam.viewmodel.ConversationsViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    var selectedTab by remember { mutableStateOf("groups") }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // ✅ ADD: Search functionality
    var searchQuery by remember { mutableStateOf("") }

    // États du ViewModel (CHAT DE GROUPE)
    val chatGroups by viewModel.chatGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ✅ États du ConversationsViewModel (CHAT PERSONNEL)
    val privateConversations by conversationsViewModel.conversationsUI.collectAsState()
    val privateIsConnected by conversationsViewModel.isConnected.collectAsState()
    val privateErrorMessage by conversationsViewModel.errorMessage.collectAsState()

    // ✅ Filter chats based on search query
    val filteredChatGroups = remember(chatGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            chatGroups
        } else {
            chatGroups.filter { chat ->
                // Search by chat name
                chat.name.contains(searchQuery, ignoreCase = true) ||
                // Search by last message author
                chat.lastMessageAuthor.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // ✅ Filter private conversations based on search query
    val filteredPrivateConversations = remember(privateConversations, searchQuery) {
        if (searchQuery.isBlank()) {
            privateConversations
        } else {
            privateConversations.filter { conversation ->
                conversation.otherUser.displayName.contains(searchQuery, ignoreCase = true) ||
                conversation.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // ✅ Initialize ChatStateManager and ReadMessagesManager
    LaunchedEffect(Unit) {
        ChatStateManager.initialize(context)
        com.example.dam.utils.ReadMessagesManager.initialize(context)
        viewModel.loadUserChats(context)
        // ✅ Initialize (sans connexion socket automatique)
        conversationsViewModel.initialize(context)
    }

    // ✅ NOUVEAU: Charger les conversations privées quand l'onglet est sélectionné
    LaunchedEffect(selectedTab) {
        if (selectedTab == "personal") {
            android.util.Log.d("MessagesListScreen", "🔄 Tab switched to Personal - Loading conversations")
            // Connecter au socket si pas déjà connecté
            if (!privateIsConnected) {
                conversationsViewModel.initialize(context)
                delay(500) // Attendre la connexion
            }
            // Charger les conversations
            conversationsViewModel.loadConversations()
        }
    }

    // ✅ Refresh when returning to messages list
    DisposableEffect(lifecycleOwner) {
        val callback = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("MessagesListScreen", "🔄 ON_RESUME: Refreshing chat list")
                // Small delay to let any pending operations complete
                coroutineScope.launch {
                    delay(300) // Quick refresh
                    android.util.Log.d("MessagesListScreen", "🔄 Loading updated chat list after delay...")
                    viewModel.loadUserChats(context)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(callback)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(callback)
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
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Messages",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = {
                            viewModel.loadUserChats(context)
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = GreenAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ Interactive Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardDark.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (searchQuery.isNotEmpty()) GreenAccent else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // ✅ TextField for search input
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Rechercher une conversation...",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = GreenAccent
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // ✅ Clear button when text is entered
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Groups Tab
                        Surface(
                            onClick = { selectedTab = "groups" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTab == "groups")
                                GreenAccent.copy(alpha = 0.2f)
                            else
                                CardDark.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedTab == "groups") GreenAccent else BorderColor
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = "Groups",
                                    tint = if (selectedTab == "groups") GreenAccent else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Groupes",
                                    color = if (selectedTab == "groups") GreenAccent else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                // ✅ Show total UNREAD count, not total chats count
                                val totalUnreadCount = chatGroups.sumOf { it.unreadCount }
                                if (totalUnreadCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ErrorRed
                                    ) {
                                        Text(
                                            text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Personal Tab
                        Surface(
                            onClick = { selectedTab = "personal" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTab == "personal")
                                GreenAccent.copy(alpha = 0.2f)
                            else
                                CardDark.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedTab == "personal") GreenAccent else BorderColor
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Personal",
                                    tint = if (selectedTab == "personal") GreenAccent else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Personnel",
                                    color = if (selectedTab == "personal") GreenAccent else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                // ✅ Show total UNREAD count for private conversations
                                val totalPrivateUnreadCount = privateConversations.sumOf { it.unreadCount }
                                if (totalPrivateUnreadCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ErrorRed
                                    ) {
                                        Text(
                                            text = if (totalPrivateUnreadCount > 99) "99+" else totalPrivateUnreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Messages List avec gestion des états
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // ✅ AFFICHER LE CONTENU SELON L'ONGLET SÉLECTIONNÉ
                when (selectedTab) {
                    "groups" -> {
                        // ==================== CHATS DE GROUPE (NE PAS TOUCHER) ====================
                        when {
                            // État de chargement
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = GreenAccent
                                )
                            }
                            // État d'erreur
                            errorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = errorMessage ?: "Erreur inconnue",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            viewModel.clearError()
                                            viewModel.loadUserChats(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenAccent
                                        )
                                    ) {
                                        Text("Réessayer")
                                    }
                                }
                            }
                            // État vide
                            chatGroups.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.ChatBubbleOutline,
                                        contentDescription = "No chats",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aucune discussion",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Rejoignez une sortie pour commencer à discuter",
                                        color = TextTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            // ✅ Show "No results" if search has no matches
                            filteredChatGroups.isEmpty() && searchQuery.isNotEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = "No results",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aucun résultat",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Aucune discussion ne correspond à \"$searchQuery\"",
                                        color = TextTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            // ✅ Liste des chats filtrés
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 100.dp, top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = filteredChatGroups, // ✅ Use filtered list
                                        key = { group -> group.sortieId }
                                    ) { group ->
                                        GroupChatItem(
                                            group = group,
                                            onClick = {
                                                // ✅ Mark chat as read immediately when clicked
                                                com.example.dam.utils.ReadMessagesManager.markChatAsRead(context, group.sortieId)

                                                // Navigate to chat conversation
                                                val encodedGroupName = java.net.URLEncoder.encode(group.name, "UTF-8")
                                                val encodedEmoji = java.net.URLEncoder.encode(group.emoji, "UTF-8")

                                                navController.navigate(
                                                    "chatConversation/${group.sortieId}/$encodedGroupName/$encodedEmoji/${group.participantsCount}"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "personal" -> {
                        // ==================== CONVERSATIONS PRIVÉES ====================
                        when {
                            // État de connexion
                            !privateIsConnected -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = GreenAccent)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Connexion...",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            // État d'erreur
                            privateErrorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = privateErrorMessage ?: "Erreur inconnue",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            conversationsViewModel.clearError()
                                            conversationsViewModel.loadConversations()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenAccent
                                        )
                                    ) {
                                        Text("Réessayer")
                                    }
                                }
                            }
                            // État vide
                            privateConversations.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.PersonOutline,
                                        contentDescription = "No conversations",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aucune conversation",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Démarrez une conversation depuis un profil",
                                        color = TextTertiary,
                                        fontSize = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            // ✅ Show "No results" if search has no matches
                            filteredPrivateConversations.isEmpty() && searchQuery.isNotEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = "No results",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aucun résultat",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Aucune conversation ne correspond à \"$searchQuery\"",
                                        color = TextTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            // ✅ Liste des conversations privées
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 100.dp, top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = filteredPrivateConversations,
                                        key = { conversation -> conversation.id }
                                    ) { conversation ->
                                        PrivateConversationItem(
                                            conversation = conversation,
                                            onClick = {
                                                // Navigate to private chat
                                                val encodedName = java.net.URLEncoder.encode(
                                                    conversation.otherUser.displayName,
                                                    "UTF-8"
                                                )
                                                val encodedAvatar = if (conversation.otherUser.avatar != null) {
                                                    java.net.URLEncoder.encode(conversation.otherUser.avatar, "UTF-8")
                                                } else {
                                                    "empty"  // ✅ Utiliser "empty" au lieu de chaîne vide
                                                }

                                                android.util.Log.d("MessagesListScreen", "📱 Navigating to conversation ${conversation.id}")

                                                navController.navigate(
                                                    "privateChat/${conversation.id}/${conversation.otherUser.id}/$encodedName/$encodedAvatar"
                                                )
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

        // ✅ REMOVED: Floating Action Button (user doesn't need it)
    }
}

// ==================== COMPOSANT POUR LES CONVERSATIONS PRIVÉES ====================
@Composable
fun PrivateConversationItem(conversation: com.example.dam.models.ConversationUI, onClick: () -> Unit) {
    // ✅ État pour forcer le rafraîchissement du temps affiché
    var refreshTrigger by remember { mutableStateOf(0) }

    // ✅ Recalculer le temps toutes les 30 secondes
    LaunchedEffect(conversation.lastMessageTimestamp) {
        while (true) {
            delay(30_000) // 30 secondes
            refreshTrigger++
        }
    }

    // ✅ Calculer le temps en temps réel
    val displayTime = remember(conversation.lastMessageTimestamp, refreshTrigger) {
        conversation.lastMessageTime
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar de l'autre utilisateur
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GreenAccent.copy(alpha = 0.2f))
            ) {
                com.example.dam.utils.UserAvatar(
                    avatarUrl = conversation.otherUser.avatar,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.otherUser.displayName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = displayTime,
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // ✅ Affichage du dernier message avec style différent si non lu
                Text(
                    text = conversation.lastMessage,
                    color = if (conversation.unreadCount > 0) TextPrimary else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }

            // ✅ Badge de messages non lus
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = ErrorRed
                ) {
                    Text(
                        text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ✅ Indicateur de conversation mutée
            if (conversation.isMuted) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.NotificationsOff,
                    contentDescription = "Muté",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun GroupChatItem(group: ChatGroupUI, onClick: () -> Unit) {
    // Génération de gradient aléatoire basé sur l'ID
    val gradientColors = remember(group.id) {
        listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
    }

    // ✅ État pour forcer le rafraîchissement du temps affiché
    var refreshTrigger by remember { mutableStateOf(0) }

    // ✅ Recalculer le temps toutes les 30 secondes
    LaunchedEffect(group.timestamp) {
        while (true) {
            delay(30_000) // 30 secondes
            refreshTrigger++
        }
    }

    // ✅ Calculer le temps en temps réel
    val displayTime = remember(group.timestamp, refreshTrigger) {
        group.timestamp?.let { com.example.dam.models.formatTime(it) } ?: group.time
    }

    // ✅ PERSISTENT BADGE LOGIC: Show badge based on ReadMessagesManager
    // This persists across app restarts and chat navigation
    val readChatIds by com.example.dam.utils.ReadMessagesManager.readChatIds.collectAsState()
    val isChatRead = readChatIds.contains(group.sortieId)

    // Show badge if there's an unread message (group.unreadCount > 0) AND chat hasn't been read
    val effectiveUnreadCount = if (group.unreadCount > 0 && !isChatRead) {
        group.unreadCount
    } else {
        0
    }

    // ✅ Log for debugging
    LaunchedEffect(group.unreadCount, isChatRead) {
        android.util.Log.d("GroupChatItem", "[${group.sortieId.take(8)}] 📊 Badge=$effectiveUnreadCount (backend=${group.unreadCount}, read=$isChatRead)")
    }


    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.radialGradient(gradientColors),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.emoji,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = group.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = displayTime, // ✅ Utiliser le temps recalculé
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // ✅ Affichage du dernier message avec style différent si non lu
                Text(
                    text = if (group.lastMessage.isNotEmpty()) {
                        "${group.lastMessageAuthor}: ${group.lastMessage}"
                    } else {
                        "Aucun message"
                    },
                    color = if (effectiveUnreadCount > 0) TextPrimary else TextSecondary, // ✅ Blanc si non lu, gris sinon
                    fontSize = 14.sp,
                    fontWeight = if (effectiveUnreadCount > 0) FontWeight.SemiBold else FontWeight.Normal, // ✅ Bold si non lu
                    maxLines = 1
                )
            }

            // ✅ Badge de messages non lus (style WhatsApp/Messenger)
            if (effectiveUnreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = ErrorRed // ✅ Rouge comme WhatsApp/Messenger
                ) {
                    Text(
                        text = if (effectiveUnreadCount > 99) "99+" else effectiveUnreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
