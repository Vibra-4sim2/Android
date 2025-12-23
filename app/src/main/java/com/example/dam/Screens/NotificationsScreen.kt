package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.models.Notification
import com.example.dam.models.NotificationType
import com.example.dam.viewmodel.NotificationViewModel
import com.example.dam.ui.theme.BackgroundDark
import com.example.dam.ui.theme.CardDark
import com.example.dam.ui.theme.BorderColor
import com.example.dam.ui.theme.TextPrimary
import com.example.dam.ui.theme.TextSecondary
import com.example.dam.ui.theme.SuccessGreen
import com.example.dam.ui.theme.ErrorRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran affichant la liste des notifications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Charger les notifications au démarrage
    LaunchedEffect(Unit) {
        viewModel.loadNotifications(context, unreadOnly = false)
        viewModel.loadUnreadCount(context)
    }

    // Palette sombre alignée sur le thème global
    val backgroundColor = BackgroundDark
    // cartes légèrement plus claires que le background, avec vert profond
    val cardUnreadColor = CardDark
    val cardReadColor = CardDark.copy(alpha = 0.85f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                modifier = Modifier.size(24.dp),
                                containerColor = SuccessGreen
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Indicateur de polling actif
                    if (viewModel.isPollingActive()) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Polling actif",
                            tint = SuccessGreen,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
        ) {
            when {
                errorMessage != null -> {
                    ErrorView(
                        message = errorMessage!!,
                        onRetry = { viewModel.loadNotifications(context) }
                    )
                }
                notifications.isEmpty() && !isLoading -> {
                    EmptyNotificationsView()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    handleNotificationClick(navController, notification)
                                    viewModel.markAsRead(context, notification.id)
                                },
                                onDelete = {
                                    // Archivage côté backend + suppression UI
                                    viewModel.removeNotificationFromList(context, notification.id)
                                },
                                cardUnreadColor = cardUnreadColor,
                                cardReadColor = cardReadColor
                            )
                        }
                    }
                }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    cardUnreadColor: Color,
    cardReadColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable { onClick() }
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 0.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) cardReadColor else cardUnreadColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône du type de notification dans un rond légèrement lumineux
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Contenu
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                    }
                }

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3
                )

                Text(
                    text = formatNotificationTimestamp(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Supprimer la notification",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.6f)
            )
            Text(
                text = "Aucune notification",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Vous êtes à jour !",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = ErrorRed
            )
            Text(
                text = "Erreur",
                style = MaterialTheme.typography.titleMedium,
                color = ErrorRed
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black)) {
                Text("Réessayer")
            }
        }
    }
}

/**
 * Retourne l'icône appropriée selon le type de notification
 */
fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.NEW_PUBLICATION -> Icons.Default.Add
    NotificationType.CHAT_MESSAGE -> Icons.Default.Email
    NotificationType.NEW_SORTIE -> Icons.Default.Place
    NotificationType.PARTICIPATION_ACCEPTED -> Icons.Default.Check
    NotificationType.PARTICIPATION_REJECTED -> Icons.Default.Close
    NotificationType.TEST -> Icons.Default.Info
}

/**
 * Retourne la couleur appropriée selon le type de notification
 */
fun getNotificationColor(type: NotificationType) = when (type) {
    NotificationType.NEW_PUBLICATION -> Color(0xFF2196F3)
    NotificationType.CHAT_MESSAGE -> Color(0xFF4CAF50)
    NotificationType.NEW_SORTIE -> Color(0xFFFF9800)
    NotificationType.PARTICIPATION_ACCEPTED -> Color(0xFF4CAF50)
    NotificationType.PARTICIPATION_REJECTED -> Color(0xFFF44336)
    NotificationType.TEST -> Color(0xFF9E9E9E)
}

/**
 * Formate le timestamp en format lisible
 */
fun formatNotificationTimestamp(timestamp: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(timestamp)

        val now = System.currentTimeMillis()
        val diff = now - (date?.time ?: now)

        when {
            diff < 60_000 -> "À l'instant"
            diff < 3_600_000 -> "${diff / 60_000} min"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            else -> "${diff / 86_400_000}j"
        }
    } catch (e: Exception) {
        "Récemment"
    }
}

/**
 * Gère le clic sur une notification et navigue vers l'écran approprié
 */
fun handleNotificationClick(navController: NavController, notification: Notification) {
    when (notification.type) {
        NotificationType.NEW_PUBLICATION -> {
            // Navigation vers l'écran feed (liste des publications)
            // Car pas d'écran détail publication individuel
            navController.navigate("feed") {
                launchSingleTop = true
            }
        }
        NotificationType.CHAT_MESSAGE -> {
            notification.data.sortieId?.let { sortieId ->
                val groupName = notification.data.chatName ?: "Chat"
                val emoji = "💬"
                val count = "0"
                navController.navigate(
                    "chatConversation/$sortieId/${java.net.URLEncoder.encode(groupName, "UTF-8")}/${java.net.URLEncoder.encode(emoji, "UTF-8")}/$count"
                )
            }
        }
        NotificationType.NEW_SORTIE,
        NotificationType.PARTICIPATION_ACCEPTED,
        NotificationType.PARTICIPATION_REJECTED -> {
            notification.data.sortieId?.let {
                navController.navigate("sortieDetail/$it")
            }
        }
        NotificationType.TEST -> {
            // Pas de navigation pour les notifications de test
        }
    }
}
