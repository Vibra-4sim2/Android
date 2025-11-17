package com.example.dam.ui.theme

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dam.Screens.AddPublicationScreen
import com.example.dam.Screens.HomeExploreScreen
import com.example.dam.Screens.ProfileScreen
import com.example.dam.Screens.CreateAdventureScreen
import com.example.dam.Screens.FeedScreen
import com.example.dam.Screens.EditProfile1Screen
import com.example.dam.utils.UserPreferences

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabBarView(navController: NavHostController) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Create internal nav controller for screens within TabBar
    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf("Home", "Map", "Add", "Community", "Profile")
    var selectedTab by remember { mutableStateOf(0) }

    // Update selected tab based on current route
    LaunchedEffect(currentRoute) {
        selectedTab = when (currentRoute) {
            "home" -> 0
            "map" -> 1
            "add" -> 2
            "feed" -> 3
            "profile" -> 4
            else -> selectedTab
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (showOptions) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    // Check if we should show bars (hide on edit profile, add publication, etc.)
    val shouldShowBars = currentRoute !in listOf("edit_profile", "addpublication")

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
        // Main Content
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            if (shouldShowBars) {
                Spacer(modifier = Modifier.height(60.dp))
            }

            // Content Area with Navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NavHost(
                    navController = internalNavController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeExploreScreen(navController = internalNavController)
                    }
                    composable("map") {
                        ScreenPlaceholder("Map View")
                    }
                    composable("add") {
                        val token = UserPreferences.getToken(context) ?: ""
                        CreateAdventureScreen(
                            navController = internalNavController,
                            token = token
                        )
                    }
                    composable("feed") {
                        FeedScreen(navController = internalNavController)
                    }
                    composable("profile") {
                        ProfileScreen(navController = internalNavController)
                    }
                    composable("edit_profile") {
                        EditProfile1Screen(navController = internalNavController)
                    }
                    composable("addpublication") {
                        AddPublicationScreen(navController = internalNavController)
                    }
                }
            }
        }

        // Top Bar with Dropdown
        if (shouldShowBars) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                // Top Bar - Glass Design
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BackgroundGradientStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CardDark.copy(alpha = 0.8f),
                                        CardDark.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "V!BRA",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Explore Adventures",
                                fontSize = 12.sp,
                                color = GreenAccent.copy(alpha = 0.7f)
                            )
                        }

                        Surface(
                            onClick = { showOptions = !showOptions },
                            shape = CircleShape,
                            color = BackgroundDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CardDark.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Menu",
                                    tint = GreenAccent,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(rotation)
                                )
                            }
                        }
                    }
                }

                // Dropdown Menu
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
                    GlassDropdownMenu(onLogout = { showLogoutDialog = true })
                }
            }
        }

        // Glass Bottom Navigation Bar
        if (shouldShowBars) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                GlassBottomNav(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        when (index) {
                            0 -> internalNavController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            1 -> internalNavController.navigate("map") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            2 -> internalNavController.navigate("add") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            3 -> internalNavController.navigate("feed") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            4 -> internalNavController.navigate("profile") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = ErrorRed
                    )
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("TabBarView", "========== LOGOUT ==========")
                        Log.d("TabBarView", "Token avant: ${UserPreferences.getToken(context)?.take(20)}")

                        UserPreferences.clear(context)

                        Log.d("TabBarView", "Token après: ${UserPreferences.getToken(context)}")
                        Log.d("TabBarView", "============================")

                        showLogoutDialog = false

                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(ErrorRed)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextTertiary)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun GlassDropdownMenu(onLogout: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardGlass,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardDark.copy(alpha = 0.7f),
                            CardDark.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassMenuItem(icon = Icons.Default.BookmarkBorder, label = "Saved")
            GlassMenuItem(icon = Icons.Default.HelpOutline, label = "Help Center")
            GlassMenuItem(icon = Icons.Default.Settings, label = "Settings")
            GlassMenuItem(
                icon = Icons.Default.Logout,
                label = "Logout",
                onClick = onLogout,
                tintColor = ErrorRed
            )
        }
    }
}

@Composable
fun GlassMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    tintColor: Color = GreenAccent
) {
    Surface(
        onClick = { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        color = CardGlass,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(25.dp)
            )
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun GlassBottomNav(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.Center)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.2f),
                            TealAccent.copy(alpha = 0.2f),
                            GreenAccent.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(35.dp)
                )
                .blur(12.dp)
        )

        // Glass navbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(35.dp),
            color = CardGlass,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CardDark.copy(alpha = 0.5f),
                                CardDark.copy(alpha = 0.6f),
                                CardDark.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, label ->
                    if (index == 2) {
                        // Center Add Button - Elevated
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                        ) {
                            // Glow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                GreenAccent.copy(alpha = 0.6f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .blur(8.dp)
                            )

                            // Button
                            Surface(
                                onClick = { onTabSelected(index) },
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 4.dp,
                                        color = BackgroundDark,
                                        shape = CircleShape
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    GreenAccent,
                                                    TealAccent
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Regular Tab
                        Surface(
                            onClick = { onTabSelected(index) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedIndex == index)
                                GreenAccent.copy(alpha = 0.15f)
                            else
                                Color.Transparent,
                            modifier = Modifier
                                .height(48.dp)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (label) {
                                        "Home" -> Icons.Default.Home
                                        "Map" -> Icons.Default.Map
                                        "Community" -> Icons.Default.Group
                                        "Profile" -> Icons.Default.Person
                                        else -> Icons.Default.Home
                                    },
                                    contentDescription = label,
                                    tint = if (selectedIndex == index)
                                        GreenAccent
                                    else
                                        TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardGlass,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Box(
                modifier = Modifier
                    .background(CardDark.copy(alpha = 0.6f))
                    .padding(32.dp)
            ) {
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}