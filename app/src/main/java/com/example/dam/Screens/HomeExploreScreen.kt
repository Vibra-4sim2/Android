package com.example.dam.Screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.dam.ui.theme.DamTheme

data class RideEvent(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val difficulty: String,
    val distance: String,
    val imageRes: Int,
    val profileImageRes: Int,
    val category: String // "cycling", "hiking", "camping"
)

@Composable
fun HomeExploreScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("Explore") }
    val greenColor = Color(0xFF4CAF50)

    // Sample data
    val allEvents = listOf(
        RideEvent(
            id = 1,
            title = "Morning Ride",
            location = "at La Marsa",
            time = "8:30 AM",
            difficulty = "Medium",
            distance = "25 km",
            imageRes = R.drawable.homme,
            profileImageRes = R.drawable.camping,
            category = "cycling"
        ),
        RideEvent(
            id = 2,
            title = "Weekend Mountain",
            location = "at Mountain",
            time = "8:30 AM",
            difficulty = "Hard",
            distance = "45 km",
            imageRes = R.drawable.camping,
            profileImageRes = R.drawable.homme,
            category = "cycling"
        ),
        RideEvent(
            id = 3,
            title = "Forest Trail",
            location = "at Ain Draham",
            time = "9:00 AM",
            difficulty = "Medium",
            distance = "15 km",
            imageRes = R.drawable.jbal,
            profileImageRes = R.drawable.homme,
            category = "hiking"
        ),
        RideEvent(
            id = 4,
            title = "Desert Camp",
            location = "at Tozeur",
            time = "6:00 PM",
            difficulty = "Easy",
            distance = "5 km",
            imageRes = R.drawable.download,
            profileImageRes = R.drawable.homme,
            category = "camping"
        )
    )

    val filteredEvents = when (selectedFilter) {
        "Cycling" -> allEvents.filter { it.category == "cycling" }
        "Hiking" -> allEvents.filter { it.category == "hiking" }
        "Camping" -> allEvents.filter { it.category == "camping" }
        else -> allEvents
    }

    // ✅ REMOVED Scaffold - just use Column instead
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            SearchBar(greenColor)

            Spacer(modifier = Modifier.height(20.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterTab(
                    text = "Followers",
                    icon = Icons.Default.Group,
                    isSelected = false,
                    onClick = { }
                )
                FilterTab(
                    text = "Recommendations",
                    icon = Icons.Default.Star,
                    isSelected = false,
                    onClick = { }
                )
                FilterTab(
                    text = "Explore",
                    icon = Icons.Default.Explore,
                    isSelected = true,
                    onClick = { },
                    greenColor = greenColor
                )
            }
        }

        // Content Section
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(filteredEvents) { event ->
                RideEventCard(event, greenColor)
            }

            // ✅ Add bottom spacing to prevent last item from being hidden by navbar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SearchBar(greenColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(26.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = greenColor.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = "Search",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice Search",
            tint = greenColor.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun FilterTab(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    greenColor: Color = Color(0xFF4CAF50)
) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            greenColor.copy(alpha = 0.3f),
                            greenColor.copy(alpha = 0.2f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF1A1A1A)
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) greenColor else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) greenColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                color = if (isSelected) greenColor else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun RideEventCard(event: RideEvent, greenColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profile Image
                        Image(
                            painter = painterResource(id = event.profileImageRes),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        Column {
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = event.location,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Event Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = event.time,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        // Difficulty Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (event.difficulty) {
                                        "Easy" -> Color(0xFF4CAF50)
                                        "Medium" -> Color(0xFFFFA726)
                                        "Hard" -> Color(0xFFEF5350)
                                        else -> Color.Gray
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.difficulty,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Distance
                        Text(
                            text = event.distance,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    // Bookmark Icon
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeExplorePreview() {
    DamTheme {
        val navController = rememberNavController()
        HomeExploreScreen(navController = navController)
    }
}