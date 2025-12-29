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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dam.Screens.*
import com.example.dam.models.EligibleSortieForRating
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.ChatViewModel
import com.example.dam.viewmodel.LoginViewModel
import com.example.dam.viewmodel.RatingViewModel
import com.example.dam.viewmodel.NotificationViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabBarView(
    navController: NavHostController
) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val chatViewModel: ChatViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    // ✅ NEW: Rating ViewModel for eligible sorties popup
    val ratingViewModel: RatingViewModel = viewModel()

    // ✅ NEW: Notification ViewModel for notification badge
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadNotifCount by notificationViewModel.unreadCount.collectAsState()
    val eligibleSorties: List<EligibleSortieForRating> by ratingViewModel.eligibleSorties.collectAsState()
    val ratingIsLoading: Boolean by ratingViewModel.isLoading.collectAsState()
    val ratingError: String? by ratingViewModel.errorMessage.collectAsState()
    val ratingSuccess: String? by ratingViewModel.successMessage.collectAsState()

    // ✅ NEW: State for rating dialog
    var showEligibleRatingPopup by remember { mutableStateOf(false) }
    var showIndividualRatingDialog by remember { mutableStateOf(false) }
    var selectedSortieForRating by remember { mutableStateOf<String?>(null) }
    var isHomeScreenReady by remember { mutableStateOf(false) }

    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf("Home", "Discussions", "Add", "Community", "Profile")

    // ✅ CORRECTION CRITIQUE : Utiliser derivedStateOf pour synchroniser selectedTab avec currentRoute
    // sans causer de conflits lors des recompositions
    val selectedTab by remember {
        derivedStateOf {
            when (currentRoute) {
                "home" -> 0
                "messages" -> 1
                "add" -> 2
                "feed" -> 3
                "profile" -> 4
                else -> 0
            }
        }
    }

    // ✅ NEW: Load eligible sorties when app starts
    LaunchedEffect(Unit) {
        val token = UserPreferences.getToken(context)
        if (!token.isNullOrEmpty()) {
            Log.d("TabBarView", "🔍 Loading eligible sorties for rating...")
            ratingViewModel.loadEligibleSorties(token)

            // ✅ Load notification count
            notificationViewModel.loadUnreadCount(context)
        }
    }

    // ✅ Refresh notification count periodically (every time route changes)
    LaunchedEffect(currentRoute) {
        notificationViewModel.loadUnreadCount(context)
    }

    // ✅ NEW: Wait for home screen to be ready before showing popup
    LaunchedEffect(currentRoute) {
        if (currentRoute == "home") {
            // Delay to ensure HomeExploreScreen is fully rendered
            kotlinx.coroutines.delay(1500) // Wait 1.5 seconds
            isHomeScreenReady = true
            Log.d("TabBarView", "✅ Home screen ready, can show popup now")
        }
    }

    // ✅ NEW: Show popup when eligible sorties are loaded AND home screen is ready
    LaunchedEffect(eligibleSorties, isHomeScreenReady) {
        if (eligibleSorties.isNotEmpty() && !showEligibleRatingPopup && isHomeScreenReady) {
            Log.d("TabBarView", "✅ Found ${eligibleSorties.size} eligible sorties - showing popup")
            showEligibleRatingPopup = true
        }
    }

    // ✅ NEW: Handle rating success
    LaunchedEffect(ratingSuccess) {
        ratingSuccess?.let {
            Log.d("TabBarView", "✅ Rating submitted successfully")
            // Close individual rating dialog
            showIndividualRatingDialog = false
            selectedSortieForRating = null

            // Remove rated sortie from eligible list
            selectedSortieForRating?.let { sortieId ->
                ratingViewModel.removeEligibleSortie(sortieId)
            }

            // Clear messages
            ratingViewModel.clearMessages()

            // If no more eligible sorties, close the popup
            if (eligibleSorties.isEmpty()) {
                showEligibleRatingPopup = false
            }
        }
    }

    // ✅ NEW: Handle rating error
    LaunchedEffect(ratingError) {
        ratingError?.let { error ->
            Log.e("TabBarView", "❌ Rating error: $error")
            // Auto-dismiss error after 3 seconds
            kotlinx.coroutines.delay(3000)
            ratingViewModel.clearMessages()
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

    // ✅ Liste des routes où les barres doivent être cachées
    val hiddenBarRoutes = listOf(
        "edit_profile",
        "addpublication",
        "sortieDetail/{sortieId}",
        "chatConversation/{sortieId}/{groupName}/{groupEmoji}/{participantsCount}",
        "userProfile/{userId}",
        "participation_requests/{sortieId}",
        "notifications",  // ✅ Cacher les barres aussi pour les notifications
        "saved"  // ✅ Cacher les barres pour l'écran saved
    )

    val shouldShowBars = currentRoute !in hiddenBarRoutes

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
                    composable("messages") {
                        MessagesListScreen(navController = internalNavController)
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
                    // ✅ Route pour l'écran des notifications
                    composable("notifications") {
                        NotificationsScreen(navController = internalNavController)
                    }
                    composable(
                        route = "sortieDetail/{sortieId}",
                        arguments = listOf(
                            navArgument("sortieId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
                        SortieDetailScreen(
                            navController = internalNavController,
                            sortieId = sortieId
                        )
                    }
                    composable(
                        route = "chatConversation/{sortieId}/{groupName}/{groupEmoji}/{participantsCount}",
                        arguments = listOf(
                            navArgument("sortieId") { type = NavType.StringType },
                            navArgument("groupName") { type = NavType.StringType },
                            navArgument("groupEmoji") { type = NavType.StringType },
                            navArgument("participantsCount") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
                        val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                        val groupEmoji = backStackEntry.arguments?.getString("groupEmoji") ?: ""
                        val participantsCount = backStackEntry.arguments?.getString("participantsCount") ?: "0"

                        ChatConversationScreen(
                            navController = internalNavController,
                            sortieId = sortieId,
                            groupName = groupName,
                            groupEmoji = groupEmoji,
                            participantsCount = participantsCount
                        )
                    }

                    // ✅ Participation Requests Route
                    composable(
                        route = "participation_requests/{sortieId}",
                        arguments = listOf(
                            navArgument("sortieId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
                        ParticipationRequestsScreen(
                            navController = internalNavController,
                            sortieId = sortieId
                        )
                    }

                    // ✅ User Profile Route
                    composable(
                        route = "userProfile/{userId}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        UserProfileScreen(
                            navController = internalNavController,
                            userId = userId
                        )
                    }

                    // ✅ Recommendation Routes
                    composable("recommendation_hub") {
                        RecommendationHubScreen(navController = internalNavController)
                    }


                    // ✅ NEW: Flask AI Recommendations
                    composable("flask_recommendations") {
                        FlaskAiRecommendationsScreen(navController = internalNavController)
                    }

                    // ✅ NEW: Flask Matchmaking
                    composable("flask_matchmaking") {
                        FlaskMatchmakingScreen(navController = internalNavController)
                    }

                    composable("flask_itinerary") {
                        FlaskItineraryScreen(navController = navController)
                    }

                    // ✅ Saved Sorties Route
                    composable("saved") {
                        SavedSortiesScreen(navController = internalNavController)
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ✅ Icône de notification avec badge
                            Surface(
                                onClick = {
                                    internalNavController.navigate("notifications") {
                                        launchSingleTop = true
                                    }
                                },
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
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifCount > 0) {
                                                Badge(
                                                    containerColor = Color(0xFFFF4444),
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = if (unreadNotifCount > 99) "99+" else "$unreadNotifCount",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = GreenAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            // Menu dropdown existant
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
                    GlassDropdownMenu(
                        onLogout = { showLogoutDialog = true },
                        onSavedClick = {
                            showOptions = false
                            internalNavController.navigate("saved") {
                                launchSingleTop = true
                            }
                        }
                    )
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
                        // Navigation simple et directe
                        val route = when (index) {
                            0 -> "home"
                            1 -> "messages"
                            2 -> "add"
                            3 -> "feed"
                            4 -> "profile"
                            else -> "home"
                        }

                        Log.d("TabBarView", "🔘 Tab clicked: index=$index, route=$route, currentRoute=$currentRoute")

                        // ✅ FIX: Always navigate, even if on same route (clears back stack)
                        Log.d("TabBarView", "➡️ Navigating to $route")
                        internalNavController.navigate(route) {
                            // Pop up to the start destination and save state
                            popUpTo(internalNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }

        // ✅ NEW: Eligible Sorties Rating Popup (shows on top of everything with higher z-index)
        if (showEligibleRatingPopup && eligibleSorties.isNotEmpty() && currentRoute == "home") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)) // Darker overlay
                    .clickable(enabled = false) {} // Prevent clicks through
            ) {
                EligibleSortiesRatingDialog(
                    eligibleSorties = eligibleSorties,
                    onDismiss = {
                        showEligibleRatingPopup = false
                    },
                    onRateSortie = { sortieId ->
                        selectedSortieForRating = sortieId
                        showIndividualRatingDialog = true
                    },
                    onRateLater = {
                        showEligibleRatingPopup = false
                    }
                )
            }
        }

        // ✅ NEW: Individual Rating Dialog (for rating a specific sortie)
        if (showIndividualRatingDialog && selectedSortieForRating != null) {
            RatingDialog(
                onDismiss = {
                    showIndividualRatingDialog = false
                    selectedSortieForRating = null
                    ratingViewModel.clearMessages()
                },
                onSubmit = { rating, comment ->
                    val token = UserPreferences.getToken(context) ?: ""
                    ratingViewModel.submitRating(
                        sortieId = selectedSortieForRating!!,
                        stars = rating,
                        comment = comment,
                        token = token,
                        onSuccess = {
                            // Success is handled in LaunchedEffect above
                        }
                    )
                },
                isLoading = ratingIsLoading,
                errorMessage = ratingError
            )
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
                            loginViewModel.logout(context, chatViewModel)
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

        // ✅ NEW: Show error snackbar if rating fails
        ratingError?.let { error ->
            LaunchedEffect(error) {
                Log.e("TabBarView", "⚠️ Rating error displayed: $error")
            }
        }
    }
}

@Composable
fun GlassDropdownMenu(
    onLogout: () -> Unit,
    onSavedClick: () -> Unit
) {
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
            GlassMenuItem(
                icon = Icons.Default.BookmarkBorder,
                label = "Saved",
                onClick = onSavedClick
            )
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
                        Box(modifier = Modifier.size(60.dp)) {
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

                            Surface(
                                onClick = { onTabSelected(index) },
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(4.dp, BackgroundDark, CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(GreenAccent, TealAccent)
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
                                        "Discussions" -> Icons.Default.ChatBubble
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