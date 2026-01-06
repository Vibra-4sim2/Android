package com.example.dam.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.models.SortieResponse
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.MyParticipationRequestsViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class pour regrouper les demandes par sortie
 */
data class SortieWithRequests(
    val sortie: SortieResponse,
    val requests: List<MyParticipationRequestsViewModel.ParticipationWithSortie>,
    val pendingCount: Int,
    val acceptedCount: Int,
    val refusedCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllParticipationRequestsScreen(
    navController: NavController,
    viewModel: MyParticipationRequestsViewModel = viewModel()
) {
    val context = LocalContext.current
    val token = remember { UserPreferences.getToken(context) ?: "" }
    val currentUserId = remember { UserPreferences.getUserId(context) ?: "" }

    val allRequests by viewModel.allRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // État pour les sorties expandées
    var expandedSortieIds by remember { mutableStateOf(setOf<String>()) }

    // Charger les demandes au démarrage
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadAllRequestsForUser(currentUserId)
        }
    }

    // Messages de succès
    LaunchedEffect(successMessage) {
        successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Messages d'erreur
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    // ✅ Regrouper les demandes par sortie
    val sortiesWithRequests = remember(allRequests) {
        allRequests
            .groupBy { it.sortie.id }
            .map { (_, requests) ->
                val sortie = requests.first().sortie
                SortieWithRequests(
                    sortie = sortie,
                    requests = requests.sortedByDescending { it.participation.createdAt },
                    pendingCount = requests.count { it.participation.status == "EN_ATTENTE" },
                    acceptedCount = requests.count { it.participation.status == "ACCEPTEE" },
                    refusedCount = requests.count { it.participation.status == "REFUSEE" }
                )
            }
            .sortedByDescending { it.pendingCount } // Sorties avec le plus de demandes en attente en premier
    }

    // Compter le total des demandes en attente
    val totalPending = sortiesWithRequests.sumOf { it.pendingCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Demandes de participation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (totalPending > 0) {
                            Text(
                                text = "$totalPending en attente",
                                fontSize = 12.sp,
                                color = Color(0xFFFFA500)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.9f),
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    )
                )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = GreenAccent)
                            Text(
                                "Chargement des demandes...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                allRequests.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupOff,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Aucune demande pour le moment",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les demandes de participation à vos sorties apparaîtront ici",
                            color = TextTertiary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Info résumé
                        item {
                            Text(
                                text = "${sortiesWithRequests.size} sortie${if (sortiesWithRequests.size > 1) "s" else ""} avec des demandes",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Liste des sorties avec leurs demandes
                        items(sortiesWithRequests, key = { it.sortie.id }) { sortieWithRequests ->
                            val isExpanded = expandedSortieIds.contains(sortieWithRequests.sortie.id)

                            SortieRequestsCard(
                                sortieWithRequests = sortieWithRequests,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    expandedSortieIds = if (isExpanded) {
                                        expandedSortieIds - sortieWithRequests.sortie.id
                                    } else {
                                        expandedSortieIds + sortieWithRequests.sortie.id
                                    }
                                },
                                onNavigateToSortie = {
                                    navController.navigate("sortieDetail/${sortieWithRequests.sortie.id}")
                                },
                                onAccept = { participationId ->
                                    viewModel.acceptParticipation(
                                        participationId,
                                        sortieWithRequests.sortie.id,
                                        token,
                                        currentUserId
                                    )
                                },
                                onRefuse = { participationId ->
                                    viewModel.refuseParticipation(
                                        participationId,
                                        sortieWithRequests.sortie.id,
                                        token,
                                        currentUserId
                                    )
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SortieRequestsCard(
    sortieWithRequests: SortieWithRequests,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigateToSortie: () -> Unit,
    onAccept: (String) -> Unit,
    onRefuse: (String) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = CardGlass,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.background(CardDark.copy(alpha = 0.7f))
        ) {
            // === Header de la sortie (toujours visible, cliquable pour expand) ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icône selon le type
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (sortieWithRequests.sortie.type) {
                                "VELO" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                "RANDONNEE" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                else -> GreenAccent.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (sortieWithRequests.sortie.type) {
                            "VELO" -> Icons.AutoMirrored.Filled.DirectionsBike
                            "RANDONNEE" -> Icons.Default.Hiking
                            else -> Icons.Default.Explore
                        },
                        contentDescription = null,
                        tint = when (sortieWithRequests.sortie.type) {
                            "VELO" -> Color(0xFF4CAF50)
                            "RANDONNEE" -> Color(0xFFFF9800)
                            else -> GreenAccent
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sortieWithRequests.sortie.titre,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sortieWithRequests.sortie.date,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    // Badges de comptage
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (sortieWithRequests.pendingCount > 0) {
                            CountBadge(
                                count = sortieWithRequests.pendingCount,
                                label = "en attente",
                                color = Color(0xFFFFA500)
                            )
                        }
                        if (sortieWithRequests.acceptedCount > 0) {
                            CountBadge(
                                count = sortieWithRequests.acceptedCount,
                                label = "acceptée${if (sortieWithRequests.acceptedCount > 1) "s" else ""}",
                                color = SuccessGreen
                            )
                        }
                        if (sortieWithRequests.refusedCount > 0) {
                            CountBadge(
                                count = sortieWithRequests.refusedCount,
                                label = "refusée${if (sortieWithRequests.refusedCount > 1) "s" else ""}",
                                color = ErrorRed
                            )
                        }
                    }
                }

                // Flèche d'expansion
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Réduire" else "Développer",
                    tint = GreenAccent,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotationAngle)
                )
            }

            // === Liste des demandes (visible seulement si expanded) ===
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bouton voir détails sortie
                    OutlinedButton(
                        onClick = onNavigateToSortie,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
                    ) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Voir les détails de la sortie")
                    }

                    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

                    // Liste des demandes
                    sortieWithRequests.requests.forEach { request ->
                        ParticipantRequestItem(
                            request = request,
                            onAccept = { onAccept(request.participation._id) },
                            onRefuse = { onRefuse(request.participation._id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountBadge(
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$count $label",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ParticipantRequestItem(
    request: MyParticipationRequestsViewModel.ParticipationWithSortie,
    onAccept: () -> Unit,
    onRefuse: () -> Unit
) {
    fun formatDate(dateString: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val output = SimpleDateFormat("dd MMM à HH:mm", Locale.getDefault())
            val date = input.parse(dateString)
            date?.let { output.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardDark.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GreenAccent, TealAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.participation.userId.email.first().uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.participation.userId.email.substringBefore("@"),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDate(request.participation.createdAt),
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(status = request.participation.status)
            }

            // Actions pour les demandes en attente
            if (request.participation.status == "EN_ATTENTE") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefuse,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refuser", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Accepter", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

