package com.example.dam.Screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dam.R
import com.example.dam.models.FlaskSortieResponse
import com.example.dam.models.UserMatch
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.FlaskAiViewModel

// ============================================================================
// MAIN RECOMMENDATION HUB SCREEN - ✅ FLASK ONLY
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationHubScreen(
    navController: NavController,
    flaskViewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedCard by remember { mutableStateOf<String?>(null) }

    // ✅ FLASK STATE
    val flaskRecommendations by flaskViewModel.recommendations.collectAsState()
    val userCluster by flaskViewModel.userCluster.collectAsState()
    val isLoading by flaskViewModel.recommendationsLoading.collectAsState()
    val error by flaskViewModel.recommendationsError.collectAsState()

    // Load Flask sortie recommendations on startup
    LaunchedEffect(Unit) {
        val token = UserPreferences.getToken(context)
        if (token != null) {
            flaskViewModel.loadAiRecommendations(token)
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
                        Text(
                            "✨ Adventures for You",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (userCluster != null) {
                            Text(
                                "${flaskRecommendations.size} adventures match your preferences",
                                fontSize = 14.sp,
                                color = GreenAccent
                            )
                        } else {
                            Text(
                                "Discover adventures tailored to your style",
                                fontSize = 14.sp,
                                color = GreenAccent
                            )
                        }
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
                        "🔥 Finding perfect adventures for you...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Analyzing your adventure preferences",
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
                        "Error loading recommendations",
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
                                flaskViewModel.loadAiRecommendations(it)
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

        // Content - Directly show sortie recommendations card
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Adventures picked just for you",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // ✅ CARD 1: Personalized Sortie Recommendations
            item {
                RecommendationCard(
                    title = "✨ Sorties for You",
                    subtitle = "Adventures that match your interests and style",
                    icon = Icons.Default.Star,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFF673AB7)
                        )
                    ),
                    count = flaskRecommendations.size,
                    isSelected = selectedCard == "flask",
                    onClick = {
                        selectedCard = "flask"
                        navController.navigate("flask_recommendations")
                    }
                )
            }


            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun RecommendationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    count: Int = 0,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = if (isSelected) 12.dp else 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (count > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(24.dp)
                )
            }
        }
    }
}

// ============================================================================
// ✅ FLASK AI RECOMMENDATIONS SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlaskAiRecommendationsScreen(
    navController: NavController,
    viewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val recommendations by viewModel.recommendations.collectAsState()
    val userCluster by viewModel.userCluster.collectAsState()
    val isLoading by viewModel.recommendationsLoading.collectAsState()
    val error by viewModel.recommendationsError.collectAsState()

    LaunchedEffect(Unit) {
        val token = UserPreferences.getToken(context)
        if (token != null && recommendations.isEmpty()) {
            viewModel.loadAiRecommendations(token)
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
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "✨ Adventures for You",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (userCluster != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${recommendations.size} adventures match your style",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenAccent)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "🔥 Finding perfect adventures for you...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Analyzing your preferences",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

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
                    Text("Oops! Something went wrong", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(error ?: "Unable to load recommendations", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val token = UserPreferences.getToken(context)
                            token?.let { viewModel.loadAiRecommendations(it) }
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

        if (recommendations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No adventures yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Complete your preferences to discover adventures tailored to you",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardGlass,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0).copy(0.2f), Color(0xFF673AB7).copy(0.2f))
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Personalized for You", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "Adventures matching your vibe",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            items(recommendations) { sortie ->
                FlaskSortieCard(sortie = sortie, onClick = {
                    // TODO: Navigate to sortie details
                })
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ============================================================================
// ✅ FLASK MATCHMAKING SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlaskMatchmakingScreen(
    navController: NavController,
    viewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val matches by viewModel.matches.collectAsState()
    val totalMatches by viewModel.totalMatches.collectAsState()
    val algorithm by viewModel.matchmakingAlgorithm.collectAsState()
    val isLoading by viewModel.matchmakingLoading.collectAsState()
    val error by viewModel.matchmakingError.collectAsState()

    LaunchedEffect(Unit) {
        val token = UserPreferences.getToken(context)
        if (token != null && matches.isEmpty()) {
            viewModel.loadMatchmaking(token, minSimilarity = 0.05, limit = 10)
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
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "🎯 Matchmaking",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (algorithm != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "$algorithm • $totalMatches matches",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GreenAccent)
                    Spacer(Modifier.height(16.dp))
                    Text("🧠 Running KNN...", color = TextSecondary, fontSize = 14.sp)
                }
            }
            return
        }

        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Matchmaking Error", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(error ?: "Unknown error", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val token = UserPreferences.getToken(context)
                            token?.let { viewModel.loadMatchmaking(it, 0.05, 10) }
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardGlass,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF6B6B).copy(0.2f), Color(0xFFFF8E53).copy(0.2f))
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("K-Nearest Neighbors", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "Euclidean distance similarity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            items(matches) { match ->
                UserMatchCard(match = match, onClick = {
                    // TODO: Navigate to user profile
                })
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ============================================================================
// FLASK SORTIE CARD - ✅ COMPLETE WITH ALL FIELDS
// ============================================================================

@Composable
private fun FlaskSortieCard(
    sortie: FlaskSortieResponse,
    onClick: () -> Unit
) {
    fun formatType(type: String) = when (type) {
        "RANDONNEE" -> "Hiking"
        "VELO" -> "Cycling"
        "CAMPING" -> "Camping"
        else -> type.capitalize()
    }

    fun getDefaultImage(type: String) = when (type) {
        "VELO" -> R.drawable.homme
        "RANDONNEE" -> R.drawable.jbal
        "CAMPING" -> R.drawable.camping
        else -> R.drawable.download
    }

    fun formatDifficulty(difficulty: String?) = when (difficulty) {
        "FACILE" -> "Easy"
        "MOYEN" -> "Medium"
        "DIFFICILE" -> "Hard"
        else -> difficulty ?: "N/A"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardGlass,
        border = BorderStroke(1.dp, BorderColor),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(CardDark.copy(0.4f), CardDark.copy(0.6f))
                    )
                )
        ) {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!sortie.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = sortie.photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(getDefaultImage(sortie.type))
                    )
                } else {
                    Image(
                        painter = painterResource(getDefaultImage(sortie.type)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Type Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = GreenAccent
                ) {
                    Text(
                        formatType(sortie.type),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Gradient Overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.7f))
                            )
                        )
                )
            }

            // Content Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    sortie.titre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                if (sortie.description.isNotEmpty()) {
                    Text(
                        sortie.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            sortie.date,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Difficulty
                    if (sortie.difficulte != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = when (sortie.difficulte) {
                                    "FACILE" -> Color(0xFF4CAF50)
                                    "MOYEN" -> Color(0xFFFF9800)
                                    "DIFFICILE" -> Color(0xFFF44336)
                                    else -> GreenAccent
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                formatDifficulty(sortie.difficulte),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom row: Participants & Camping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Participants
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${sortie.participants?.size ?: 0}/${sortie.capacite}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Camping option
                    if (sortie.optionCamping) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF9800).copy(0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Camping",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }

                // Distance if available
                if (sortie.itineraire != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "%.1f km • %.1f hrs".format(
                                sortie.itineraire.distance,
                                sortie.itineraire.dureeEstimee
                            ),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// USER MATCH CARD - ✅ COMPLETE WITH SIMILARITY SCORE
// ============================================================================

@Composable
private fun UserMatchCard(
    match: UserMatch,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardGlass,
        border = BorderStroke(1.dp, BorderColor),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(CardDark.copy(0.4f), CardDark.copy(0.6f))
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = Color(0xFF2A2A2A),
                modifier = Modifier.size(64.dp),
                border = BorderStroke(
                    2.dp,
                    when {
                        match.similarity >= 0.8 -> Color(0xFF4CAF50)
                        match.similarity >= 0.6 -> Color(0xFFFF9800)
                        else -> Color(0xFF9E9E9E)
                    }
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!match.user.avatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = match.user.avatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${match.user.firstName} ${match.user.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))

                // Similarity Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        match.similarity >= 0.8 -> Color(0xFF4CAF50).copy(0.2f)
                        match.similarity >= 0.6 -> Color(0xFFFF9800).copy(0.2f)
                        else -> Color(0xFF9E9E9E).copy(0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = when {
                                match.similarity >= 0.8 -> Color(0xFF4CAF50)
                                match.similarity >= 0.6 -> Color(0xFFFF9800)
                                else -> Color(0xFF9E9E9E)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            match.similarityPercent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                match.similarity >= 0.8 -> Color(0xFF4CAF50)
                                match.similarity >= 0.6 -> Color(0xFFFF9800)
                                else -> Color(0xFF9E9E9E)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Distance metric
                Text(
                    "Distance: %.3f".format(match.distance),
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            // Arrow Icon
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}