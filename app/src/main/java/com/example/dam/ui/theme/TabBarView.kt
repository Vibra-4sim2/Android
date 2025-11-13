package com.example.dam.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.dam.Screens.HomeExploreScreen
import com.example.dam.Screens.ProfileScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabBarView(navController: NavHostController) {

    var showOptions by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Tabs
    val tabs = listOf("Home", "Map", "Community", "Profile", "Add")
    var selectedTab by remember { mutableStateOf(0) }

    // Animation for dropdown rotation
    val rotation by animateFloatAsState(
        targetValue = if (showOptions) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Content with TabView
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Space for top bar
            Spacer(modifier = Modifier.height(60.dp))

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> HomeExploreScreen(navController = navController)
                    1 -> ScreenPlaceholder("Map View")
                    2 -> ScreenPlaceholder("Community View")
                    3 -> ProfileScreen(navController = navController)
                    4 -> ScreenPlaceholder("Add View")
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        // Top Bar with Dropdown (overlaying content)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(5.dp)
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "VIBRA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = { showOptions = !showOptions }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                }
            }

            // Dropdown Menu with Animation
            AnimatedVisibility(
                visible = showOptions,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                DropdownMenuContent(onLogout = { showLogoutDialog = true })
            }
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Logout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout?",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Confirm", color = Color.Red, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun DropdownMenuContent(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .background(
                Color(0xFF2C2C2E).copy(alpha = 0.95f),
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MenuItem(icon = Icons.Default.BookmarkBorder, label = "Saved")
        MenuItem(icon = Icons.Default.HelpOutline, label = "Help Center")
        MenuItem(icon = Icons.Default.Settings, label = "Settings")
        MenuItem(
            icon = Icons.Default.Logout,
            label = "Logout",
            onClick = onLogout
        )
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ScreenPlaceholder(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun BottomNavigationBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = when (label) {
                            "Home" -> Icons.Default.Home
                            "Map" -> Icons.Default.Map
                            "Community" -> Icons.Default.Group
                            "Profile" -> Icons.Default.Person
                            "Add" -> Icons.Default.Add
                            else -> Icons.Default.Home
                        },
                        contentDescription = label,
                        tint = if (index == selectedIndex)
                            Color(0xFF4CAF50)
                        else
                            Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (index == selectedIndex)
                            Color(0xFF4CAF50)
                        else
                            Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = if (index == selectedIndex)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4CAF50),
                    selectedTextColor = Color(0xFF4CAF50),
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}