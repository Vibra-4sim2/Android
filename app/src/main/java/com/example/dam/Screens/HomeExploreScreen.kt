package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dam.R
import com.example.dam.models.SortieResponse
import com.example.dam.ui.theme.*
import com.example.dam.viewmodel.HomeExploreViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeExploreScreen(
    navController: NavController,
    viewModel: HomeExploreViewModel = viewModel()
) {
    // ✅ Trier les sorties par date (plus récent en premier)
    val filteredSorties = viewModel.getFilteredSorties().sortedByDescending {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(it.date)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // ✅ Pull to refresh Material 3
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refresh()
        }
    }

    // Arrêter le refresh une fois terminé
    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
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

            // Header Section
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Glass Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GreenAccent.copy(alpha = 0.1f),
                                        TealAccent.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .blur(4.dp)
                    )

                    // Glass surface
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        color = CardGlass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CardDark.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GreenAccent.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )

                            androidx.compose.foundation.text.BasicTextField(
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                ),
                                decorationBox = { innerTextField ->
                                    if (viewModel.searchQuery.isEmpty()) {
                                        Text(
                                            "Search adventures...",
                                            color = TextSecondary,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            IconButton(
                                onClick = {
                                    if (viewModel.searchQuery.isNotEmpty()) {
                                        viewModel.updateSearchQuery("")
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.searchQuery.isEmpty())
                                        Icons.Default.Mic else Icons.Default.Clear,
                                    contentDescription = if (viewModel.searchQuery.isEmpty()) "Voice" else "Clear",
                                    tint = GreenAccent.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterPill(
                            text = "Explore",
                            icon = Icons.Default.Explore,
                            isSelected = viewModel.selectedFilter == "explore",
                            onClick = { viewModel.setFilter("explore") }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Cycling",
                            icon = Icons.Default.DirectionsBike,
                            isSelected = viewModel.selectedFilter == "cycling",
                            onClick = { viewModel.setFilter("cycling") }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Hiking",
                            icon = Icons.Default.Hiking,
                            isSelected = viewModel.selectedFilter == "hiking",
                            onClick = { viewModel.setFilter("hiking") }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Camping",
                            icon = Icons.Default.Terrain,
                            isSelected = viewModel.selectedFilter == "camping",
                            onClick = { viewModel.setFilter("camping") }
                        )
                    }
                }
            }

            // Content Section with Pull to Refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                when {
                    viewModel.isLoading && viewModel.sorties.isEmpty() -> {
                        // Initial Loading State
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
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Loading adventures...",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    viewModel.errorMessage != null && viewModel.sorties.isEmpty() -> {
                        // Error State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    viewModel.errorMessage ?: "Unknown error",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GreenAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry", fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    filteredSorties.isEmpty() -> {
                        // Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = "No results",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    "No adventures found",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Try adjusting your filters or search",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                Button(
                                    onClick = {
                                        viewModel.setFilter("explore")
                                        viewModel.updateSearchQuery("")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GreenAccent.copy(alpha = 0.2f),
                                        contentColor = GreenAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Clear Filters", fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    else -> {
                        // Success State - Show List with newest first
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredSorties) { sortie ->
                                ModernEventCard(
                                    sortie = sortie,
                                    onClick = {
                                        navController.navigate("sortieDetail/${sortie.id}")
                                    },
                                    onUserClick = { userId ->
                                        navController.navigate("userProfile/$userId")
                                    }
                                )
                            }
                            // Bottom spacing for navbar
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }

                // ✅ Pull to Refresh Indicator Material 3 (seulement visible pendant refresh)
                if (pullToRefreshState.isRefreshing || pullToRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = CardDark,
                        contentColor = GreenAccent
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GreenAccent.copy(alpha = 0.2f) else CardGlass,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) GreenAccent else BorderColor
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected)
                        Brush.horizontalGradient(
                            colors = listOf(
                                GreenAccent.copy(alpha = 0.15f),
                                TealAccent.copy(alpha = 0.15f)
                            )
                        )
                    else
                        Brush.horizontalGradient(
                            colors = listOf(
                                CardDark.copy(alpha = 0.3f),
                                CardDark.copy(alpha = 0.3f)
                            )
                        )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) GreenAccent else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = if (isSelected) GreenAccent else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ========== REPLACE YOUR ModernEventCard FUNCTION WITH THIS ==========

@Composable
fun ModernEventCard(
    sortie: SortieResponse,
    onClick: () -> Unit,
    onUserClick: ((String) -> Unit)? = null  // ✅ NEW: Callback for user profile click
) {
    // Helper functions remain the same
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getDifficultyColor(type: String): Color {
        return when (type) {
            "RANDONNEE" -> WarningOrange
            "VELO" -> TealAccent
            "CAMPING" -> SuccessGreen
            else -> TextTertiary
        }
    }

    fun formatType(type: String): String {
        return when (type) {
            "RANDONNEE" -> "Hiking"
            "VELO" -> "Cycling"
            "CAMPING" -> "Camping"
            else -> type
        }
    }

    fun getDefaultImage(type: String): Int {
        return when (type) {
            "VELO" -> R.drawable.homme
            "RANDONNEE" -> R.drawable.jbal
            "CAMPING" -> R.drawable.camping
            else -> R.drawable.download
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardGlass,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardDark.copy(alpha = 0.4f),
                            CardDark.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                if (sortie.photo != null && sortie.photo.isNotEmpty()) {
                    AsyncImage(
                        model = sortie.photo,
                        contentDescription = sortie.titre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = getDefaultImage(sortie.type)),
                        placeholder = painterResource(id = getDefaultImage(sortie.type))
                    )
                } else {
                    Image(
                        painter = painterResource(id = getDefaultImage(sortie.type)),
                        contentDescription = sortie.titre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                // Type Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getDifficultyColor(sortie.type).copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = formatType(sortie.type),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Camping Badge
                if (sortie.optionCamping) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = SuccessGreen.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = "Camping",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Camping",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Participants Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Participants",
                            tint = GreenAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${sortie.participants.size}/${sortie.capacite}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title and Avatar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // ✅ UPDATED: Clickable Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GreenAccent, TealAccent)
                                    )
                                )
                                .clickable {
                                    // ✅ Navigate to user profile when clicked
                                    onUserClick?.invoke(sortie.createurId.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val creatorAvatar = sortie.createurId.avatar

                            if (creatorAvatar != null && creatorAvatar.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(creatorAvatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.homme),
                                    placeholder = painterResource(id = R.drawable.homme)
                                )
                            } else {
                                Text(
                                    text = sortie.createurId.email.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sortie.titre,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = GreenAccent.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = sortie.itineraire?.pointArrivee?.address?.takeIf { it.isNotEmpty() }
                                        ?: sortie.itineraire?.pointArrivee?.displayName?.takeIf { it.isNotEmpty() }
                                        ?: "Unknown location",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    IconButton(onClick = { /* TODO: Add to favorites */ }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = GreenAccent.copy(alpha = 0.6f)
                        )
                    }
                }

                // Meta Info Row
                Divider(color = BorderColor, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = formatDate(sortie.date),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatTime(sortie.date),
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(BorderColor)
                    )

                    // Distance
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "Distance",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${sortie.itineraire?.distance?.div(1000) ?: 0} km",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}