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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.models.ParticipationResponse
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.ParticipationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipationRequestsScreen(
    navController: NavController,
    sortieId: String,
    viewModel: ParticipationViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ CORRECTION: Utiliser UserPreferences au lieu de SharedPreferences direct
    val token = remember { UserPreferences.getToken(context) ?: "" }

    val participations by viewModel.participations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Charger les demandes
    LaunchedEffect(sortieId) {
        viewModel.loadParticipations(sortieId)
    }

    // Afficher les messages de succès
    LaunchedEffect(successMessage) {
        successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Afficher les messages d'erreur
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    val pendingRequests = participations.filter { it.status == "EN_ATTENTE" }
    val acceptedRequests = participations.filter { it.status == "ACCEPTEE" }
    val refusedRequests = participations.filter { it.status == "REFUSEE" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Demandes de participation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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

                participations.isEmpty() -> {
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les demandes de participation apparaîtront ici",
                            color = TextTertiary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // === En attente ===
                        if (pendingRequests.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "En attente",
                                    count = pendingRequests.size,
                                    color = Color(0xFFFFA500)
                                )
                            }
                            items(pendingRequests) { participation ->
                                ParticipationCard(
                                    participation = participation,
                                    onAccept = {
                                        viewModel.acceptParticipation(participation._id, sortieId, token)
                                    },
                                    onRefuse = {
                                        viewModel.refuseParticipation(participation._id, sortieId, token)
                                    }
                                )
                            }
                        }

                        // === Acceptées ===
                        if (acceptedRequests.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionHeader(
                                    title = "Acceptées",
                                    count = acceptedRequests.size,
                                    color = SuccessGreen
                                )
                            }
                            items(acceptedRequests) { participation ->
                                ParticipationCard(participation = participation, showActions = false)
                            }
                        }

                        // === Refusées ===
                        if (refusedRequests.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionHeader(
                                    title = "Refusées",
                                    count = refusedRequests.size,
                                    color = ErrorRed
                                )
                            }
                            items(refusedRequests) { participation ->
                                ParticipationCard(participation = participation, showActions = false)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ParticipationCard(
    participation: ParticipationResponse,
    showActions: Boolean = true,
    onAccept: () -> Unit = {},
    onRefuse: () -> Unit = {}
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
        shape = RoundedCornerShape(16.dp),
        color = CardGlass,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(CardDark.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GreenAccent, TealAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participation.userId.email.first().uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = participation.userId.email.substringBefore("@"),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatDate(participation.createdAt),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                StatusBadge(status = participation.status)
            }

            if (showActions && participation.status == "EN_ATTENTE") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRefuse,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Refuser")
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Accepter", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, tint, icon, text) = when (status) {
        "EN_ATTENTE" -> Quad(Color(0xFFFFA500).copy(0.2f), Color(0xFFFFA500), Icons.Default.Schedule, "En attente")
        "ACCEPTEE" -> Quad(SuccessGreen.copy(0.2f), SuccessGreen, Icons.Default.CheckCircle, "Acceptée")
        "REFUSEE" -> Quad(ErrorRed.copy(0.2f), ErrorRed, Icons.Default.Cancel, "Refusée")
        else -> Quad(Color.Gray.copy(0.2f), Color.Gray, Icons.Default.Help, status)
    }

    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
            Text(text, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)