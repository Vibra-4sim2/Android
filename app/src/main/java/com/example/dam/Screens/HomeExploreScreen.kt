package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.ui.theme.*

data class RideEvent(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val difficulty: String,
    val distance: String,
    val participants: Int,
    val imageRes: Int,
    val profileImageRes: Int,
    val category: String
)

@Composable
fun HomeExploreScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("explore") }
    var searchQuery by remember { mutableStateOf("") }

    val allEvents = listOf(
        RideEvent(
            id = 1,
            title = "Morning Ride",
            location = "La Marsa",
            time = "8:30 AM",
            difficulty = "Medium",
            distance = "25 km",
            participants = 12,
            imageRes = R.drawable.homme,
            profileImageRes = R.drawable.camping,
            category = "cycling"
        ),
        RideEvent(
            id = 2,
            title = "Weekend Mountain",
            location = "Mountain",
            time = "6:30 AM",
            difficulty = "Hard",
            distance = "45 km",
            participants = 8,
            imageRes = R.drawable.camping,
            profileImageRes = R.drawable.homme,
            category = "camping"
        ),
        RideEvent(
            id = 3,
            title = "Sunrise Hike",
            location = "Atlas Mountains",
            time = "5:00 AM",
            difficulty = "Easy",
            distance = "12 km",
            participants = 15,
            imageRes = R.drawable.jbal,
            profileImageRes = R.drawable.homme,
            category = "hiking"
        ),
        RideEvent(
            id = 4,
            title = "Desert Camp",
            location = "Tozeur",
            time = "6:00 PM",
            difficulty = "Easy",
            distance = "5 km",
            participants = 6,
            imageRes = R.drawable.download,
            profileImageRes = R.drawable.homme,
            category = "camping"
        )
    )

    val filteredEvents = when (selectedFilter) {
        "cycling" -> allEvents.filter { it.category == "cycling" }
        "hiking" -> allEvents.filter { it.category == "hiking" }
        "camping" -> allEvents.filter { it.category == "camping" }
        else -> allEvents
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

                // Logo and Profile Row
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "VIBRA",
//                            color = TextPrimary,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            letterSpacing = 1.sp
//                        )
//                        Text(
//                            text = "Explore Adventures",
//                            color = GreenAccent.copy(alpha = 0.7f),
//                            fontSize = 14.sp
//                        )
//                    }
//
//                    // Profile Avatar
//                    Box(
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
//                            .background(CardDark)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Profile",
//                            tint = GreenAccent,
//                            modifier = Modifier
//                                .align(Alignment.Center)
//                                .size(24.dp)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))

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
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
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
                                onClick = { },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
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
                            isSelected = selectedFilter == "explore",
                            onClick = { selectedFilter = "explore" }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Followers",
                            icon = Icons.Default.Group,
                            isSelected = selectedFilter == "followers",
                            onClick = { selectedFilter = "followers" }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Recommended",
                            icon = Icons.Default.Star,
                            isSelected = selectedFilter == "recommended",
                            onClick = { selectedFilter = "recommended" }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Trending",
                            icon = Icons.Default.TrendingUp,
                            isSelected = selectedFilter == "trending",
                            onClick = { selectedFilter = "trending" }
                        )
                    }
                }
            }

            // Activities List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEvents) { event ->
                    ModernEventCard(event)
                }

                // Bottom spacing for navbar
                item {
                    Spacer(modifier = Modifier.height(100.dp))
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

@Composable
fun ModernEventCard(event: RideEvent) {
    Surface(
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
                Image(
                    painter = painterResource(id = event.imageRes),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
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

                // Difficulty Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    val difficultyColor = when (event.difficulty) {
                        "Easy" -> SuccessGreen
                        "Medium" -> WarningOrange
                        "Hard" -> ErrorRed
                        else -> TextTertiary
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = difficultyColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = event.difficulty,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Participants Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.4f),
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
                            text = "${event.participants}",
                            color = TextPrimary,
                            fontSize = 12.sp
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
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, GreenAccent.copy(alpha = 0.5f), CircleShape)
                                .background(CardDark)
                        ) {
                            Image(
                                painter = painterResource(id = event.profileImageRes),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
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
                                    text = event.location,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    IconButton(onClick = { }) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = event.time,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(BorderColor)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Distance",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = event.distance,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeExplorePreviewModern() {
    DamTheme {
        val navController = rememberNavController()
        HomeExploreScreen(navController = navController)
    }
}