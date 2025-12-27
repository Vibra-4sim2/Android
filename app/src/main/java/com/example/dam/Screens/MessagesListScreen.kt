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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    viewModel: MessagesViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("groups") }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // États du ViewModel
    val chatGroups by viewModel.chatGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ✅ Initialize ChatStateManager with context to enable persistence
    LaunchedEffect(Unit) {
        ChatStateManager.initialize(context)
    }

    // ✅ Charger les chats au démarrage
    LaunchedEffect(Unit) {
        viewModel.loadUserChats(context)
    }

    // ✅ Rafraîchir quand on revient de la conversation
    // Utiliser lifecycle pour détecter quand l'écran devient visible
    DisposableEffect(lifecycleOwner) {
        val callback = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("MessagesListScreen", "========================================")
                android.util.Log.d("MessagesListScreen", "🔄 ON_RESUME: Starting refresh cycle...")
                android.util.Log.d("MessagesListScreen", "========================================")

                // Rafraîchir la liste quand on revient sur cet écran
                // Multiple refresh strategy with optimized timings
                coroutineScope.launch {
                    // ✅ First refresh after 500ms (give backend time to process markAsRead)
                    delay(500)
                    android.util.Log.d("MessagesListScreen", "🔄 Refresh #1: After 500ms (backend sync time)")
                    viewModel.loadUserChats(context)

                    // Deuxième refresh après 1.5s total
                    delay(1000) // total 1.5s
                    android.util.Log.d("MessagesListScreen", "🔄 Refresh #2: After 1.5s")
                    viewModel.loadUserChats(context)

                    // Troisième refresh après 3s total
                    delay(1500) // total 3s
                    android.util.Log.d("MessagesListScreen", "🔄 Refresh #3: After 3s")
                    viewModel.loadUserChats(context)

                    // Quatrième refresh après 5s
                    delay(2000) // total 5s
                    android.util.Log.d("MessagesListScreen", "🔄 Refresh #4: After 5s")
                    viewModel.loadUserChats(context)

                    // Cinquième refresh après 10s (pour backends lents)
                    delay(5000) // total 10s
                    android.util.Log.d("MessagesListScreen", "🔄 Refresh #5 (FINAL): After 10s")
                    viewModel.loadUserChats(context)

                    android.util.Log.d("MessagesListScreen", "✅ Refresh cycle complete (5 refreshes over 10s)")
                    android.util.Log.d("MessagesListScreen", "========================================")
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
                    colors = listOf(
                        BackgroundGradientStart,
                        BackgroundDark,
                        BackgroundGradientEnd
                    )
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

                    // Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardDark.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Rechercher une conversation...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
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
                                if (chatGroups.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ErrorRed
                                    ) {
                                        Text(
                                            text = chatGroups.size.toString(),
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
                    // Liste des chats
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = chatGroups,
                                key = { group -> group.sortieId }
                            ) { group ->
                                GroupChatItem(
                                    group = group,
                                    onClick = {
                                        // ✅ Utiliser sortieId au lieu de group.id
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
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { /* New message */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp),
            containerColor = GreenAccent,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "New message",
                tint = Color.White
            )
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

    // ✅ Check optimistic state for immediate badge hiding
    val recentlyOpenedChats by ChatStateManager.recentlyOpenedChats.collectAsState()
    val isOptimisticallyRead = recentlyOpenedChats.contains(group.sortieId)

    // ✅ Remember the last message timestamp to detect new messages
    // Initialize with current timestamp to establish baseline
    val lastMessageTime = remember(group.sortieId) { mutableStateOf(group.timestamp ?: "") }

    // ✅ Update lastMessageTime when not in optimistic state (to track actual last message)
    LaunchedEffect(group.timestamp) {
        if (!isOptimisticallyRead && group.timestamp != null) {
            lastMessageTime.value = group.timestamp
        }
    }

    // ✅ CRITICAL: Badge is ALWAYS hidden if optimistically marked as read
    // UNLESS a new message has arrived (different timestamp)
    val hasNewMessage = remember(isOptimisticallyRead, group.timestamp, lastMessageTime.value) {
        if (isOptimisticallyRead && group.timestamp != null && lastMessageTime.value.isNotEmpty()) {
            group.timestamp != lastMessageTime.value
        } else {
            false
        }
    }

    val effectiveUnreadCount = remember(isOptimisticallyRead, group.unreadCount, hasNewMessage) {
        if (isOptimisticallyRead && !hasNewMessage) {
            0  // Force badge to be hidden (optimistic)
        } else {
            group.unreadCount  // Show backend's unread count
        }
    }

    // ✅ Debug logging - runs on every state change
    LaunchedEffect(group.sortieId, group.unreadCount, isOptimisticallyRead, group.timestamp) {
        android.util.Log.d("GroupChatItem", "========================================")
        android.util.Log.d("GroupChatItem", "📊 Badge State for ${group.name}")
        android.util.Log.d("GroupChatItem", "   sortieId: ${group.sortieId}")
        android.util.Log.d("GroupChatItem", "   unreadCount (from backend): ${group.unreadCount}")
        android.util.Log.d("GroupChatItem", "   isOptimisticallyRead: $isOptimisticallyRead")
        android.util.Log.d("GroupChatItem", "   effectiveUnreadCount (displayed): $effectiveUnreadCount")
        android.util.Log.d("GroupChatItem", "   lastMessage timestamp: ${group.timestamp}")
        android.util.Log.d("GroupChatItem", "   lastMessageTime (stored): ${lastMessageTime.value}")
        android.util.Log.d("GroupChatItem", "   All optimistic chats: $recentlyOpenedChats")

        // ✅ Clear optimistic state ONLY when backend confirms read (unreadCount = 0)
        if (isOptimisticallyRead && group.unreadCount == 0) {
            // Backend confirmed all messages are read
            ChatStateManager.clearOptimisticState(group.sortieId)
            android.util.Log.d("GroupChatItem", "✅ Backend confirmed read (unreadCount=0), cleared optimistic state for ${group.sortieId}")
            // Update the last message time
            lastMessageTime.value = group.timestamp ?: ""
        }
        // ✅ NEW: Clear optimistic state if a NEW message arrives (timestamp changed)
        else if (isOptimisticallyRead && group.unreadCount > 0) {
            val currentTimestamp = group.timestamp ?: ""
            if (currentTimestamp != lastMessageTime.value && lastMessageTime.value.isNotEmpty()) {
                // New message detected (timestamp changed)
                android.util.Log.d("GroupChatItem", "🆕 NEW MESSAGE detected! Clearing optimistic state")
                android.util.Log.d("GroupChatItem", "   Old timestamp: ${lastMessageTime.value}")
                android.util.Log.d("GroupChatItem", "   New timestamp: $currentTimestamp")
                ChatStateManager.clearOptimisticState(group.sortieId)
                lastMessageTime.value = currentTimestamp
            } else {
                // Backend still shows unread, keep optimistic state ACTIVE to hide badge
                android.util.Log.d("GroupChatItem", "⏳ Optimistic state ACTIVE - keeping badge HIDDEN while waiting for backend (current unreadCount=${group.unreadCount})")
            }
        }
        else if (!isOptimisticallyRead && group.unreadCount > 0) {
            android.util.Log.d("GroupChatItem", "🔴 Badge should be VISIBLE - unread message exists and not optimistically read")
            // Update the last message time
            lastMessageTime.value = group.timestamp ?: ""
        }
        android.util.Log.d("GroupChatItem", "========================================")
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
