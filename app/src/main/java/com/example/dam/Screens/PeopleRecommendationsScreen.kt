package com.example.dam.Screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dam.models.UserMatch
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.FlaskAiViewModel

/**
 * Screen displaying people matching user's preferences using Flask AI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleRecommendationsScreen(
    navController: NavController,
    flaskViewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ FLASK STATE - Reusing existing AI
    val flaskMatches by flaskViewModel.matches.collectAsState()
    val isLoading by flaskViewModel.matchmakingLoading.collectAsState()
    val error by flaskViewModel.matchmakingError.collectAsState()

    // Load Flask matchmaking data on startup
    LaunchedEffect(Unit) {
        val token = UserPreferences.getToken(context)
        if (token != null) {
            flaskViewModel.loadMatchmaking(token, minSimilarity = 0.05, limit = 10)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                )
            )
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Spacer(Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "👥 People Like You",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Connect with adventurers who match your vibe",
                            fontSize = 14.sp,
                            color = GreenAccent
                        )
                    }
                }
            }
        }

        // Loading State
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenAccent)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "🔥 Finding people who share your interests...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Matching based on your adventure preferences",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        // Error State
        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Error loading matches",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        error ?: "",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val token = UserPreferences.getToken(context)
                            token?.let {
                                flaskViewModel.loadMatchmaking(it, 0.05, 10)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
            return
        }

        // Content - List of matched people
        if (flaskMatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        "🔍",
                        fontSize = 64.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No matches yet",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Complete your preferences to find people with similar interests",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${flaskMatches.size} adventurers share your interests",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(flaskMatches) { match ->
                    PersonMatchCard(match = match, onClick = {
                        // TODO: Navigate to profile
                    })
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PersonMatchCard(
    match: UserMatch,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CardDark,
                            Color(0xFF252525)
                        )
                    )
                )
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B),
                                    Color(0xFFFF8E53)
                                )
                            )
                        )
                        .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                ) {
                    if (!match.user.avatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = match.user.avatar,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${match.user.firstName.firstOrNull()?.uppercaseChar() ?: ""}${match.user.lastName.firstOrNull()?.uppercaseChar() ?: ""}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${match.user.firstName} ${match.user.lastName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Match badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenAccent.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${(match.similarity * 100).toInt()}% Vibe Match",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = GreenAccent
                            )
                        }
                    }

                    // Distance info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${match.distance.toInt()} km away",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Arrow
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View profile",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

