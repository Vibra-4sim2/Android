package com.example.dam.Screens

import android.util.Log
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dam.R
import com.example.dam.models.PublicationResponse
import com.example.dam.models.SortieRatingData
import com.example.dam.remote.RetrofitInstance
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.HomeExploreViewModel
import com.example.dam.viewmodel.RatingViewModel
import com.example.dam.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserProfileScreen(
    navController: NavHostController,
    userId: String
) {
    val context = LocalContext.current
    val viewModel: UserProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(context) as T
            }
        }
    )

    // ✅ Rating ViewModel instance
    val ratingViewModel: RatingViewModel = viewModel()
    val homeViewModel: HomeExploreViewModel = viewModel()

    val token = UserPreferences.getToken(context) ?: ""
    val currentUserId = UserPreferences.getUserId(context) ?: ""

    // User profile states
    val user by viewModel.user.collectAsState()
    val sorties by viewModel.userSorties.collectAsState()
    val participations by viewModel.userParticipations.collectAsState()
    val publications by viewModel.userPublications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    // ✅ Rating states from RatingViewModel
    val creatorRating by ratingViewModel.creatorRating.collectAsState()
    val ratingIsLoading by ratingViewModel.isLoading.collectAsState()
    val ratingError by ratingViewModel.errorMessage.collectAsState()
    val ratingSuccess by ratingViewModel.successMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedSortieForRating by remember { mutableStateOf<String?>(null) }
    var ratingRefreshTrigger by remember { mutableStateOf(0) }

    // ✅ FIXED: Load both profile and rating
    LaunchedEffect(userId, token) {
        Log.d("UserProfileScreen", "🔍 Loading data for userId: $userId")
        if (token.isNotEmpty() && userId.isNotEmpty()) {
            try {
                // Load user profile
                viewModel.loadUserProfile(userId, token)

                // ✅ Load creator rating (this was missing!)
                ratingViewModel.loadCreatorRating(userId, token)

                Log.d("UserProfileScreen", "✅ Started loading profile and rating")
            } catch (e: Exception) {
                Log.e("UserProfileScreen", "❌ Error: ${e.message}")
            }
        }
    }

    // Show success/error messages
    LaunchedEffect(ratingSuccess) {
        ratingSuccess?.let {
            Log.d("UserProfileScreen", "✅ Rating submitted successfully")
            ratingRefreshTrigger++

            // ✅ Refresh the creator rating after submitting a new rating
            ratingViewModel.loadCreatorRating(userId, token)
            ratingViewModel.clearMessages()
        }
    }

    LaunchedEffect(ratingError) {
        ratingError?.let {
            Log.e("UserProfileScreen", "❌ Rating error: $it")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            if (isLoading && user == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent, strokeWidth = 3.dp)
                }
            } else if (user != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    item {
                        // ✅ FIXED: Pass creatorRating from RatingViewModel
                        UserProfileHeader(
                            userName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}",
                            userBio = user?.email ?: "",
                            avatarUrl = user?.avatar,
                            followersCount = followersCount,
                            followingCount = followingCount,
                            adventureCount = sorties.size,
                            publicationCount = publications.size,
                            participationCount = participations.size,
                            location = "Tunisia",
                            rating = creatorRating  // ✅ Use from RatingViewModel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FollowActionButtons(
                            isFollowing = isFollowing,
                            onFollowClick = { viewModel.followUser(userId, token) },
                            onUnfollowClick = { viewModel.unfollowUser(userId, token) },
                            onMessageClick = { /* TODO */ }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TabSection(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            sortiesCount = sorties.size,
                            participationsCount = participations.size,
                            publicationsCount = publications.size
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when (selectedTab) {
                        0 -> {
                            if (sorties.isEmpty()) {
                                item {
                                    EmptyState(
                                        icon = Icons.Default.DirectionsBike,
                                        title = "Aucune sortie",
                                        subtitle = "Cet utilisateur n'a pas encore créé de sorties"
                                    )
                                }
                            } else {
                                items(sorties.filterNotNull()) { sortie ->
                                    ModernEventCard(
                                        sortie = sortie,
                                        onClick = { navController.navigate("sortieDetail/${sortie.id}") }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                        1 -> {
                            if (participations.isEmpty()) {
                                item {
                                    EmptyState(
                                        icon = Icons.Default.EmojiPeople,
                                        title = "Aucune participation",
                                        subtitle = "Cet utilisateur ne participe à aucune sortie"
                                    )
                                }
                            } else {
                                items(participations.filterNotNull()) { participation ->
                                    participation.sortieId?.let { sortie ->
                                        ParticipationCardWithRating(
                                            participation = participation,
                                            sortie = sortie,
                                            isCurrentUser = userId == currentUserId,
                                            refreshTrigger = ratingRefreshTrigger,
                                            onClick = {
                                                sortie._id?.let { id ->
                                                    navController.navigate("sortieDetail/$id")
                                                }
                                            },
                                            onRatingClick = {
                                                sortie._id?.let { id ->
                                                    selectedSortieForRating = id
                                                    showRatingDialog = true
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (publications.isEmpty()) {
                                item {
                                    EmptyState(
                                        icon = Icons.Default.PhotoLibrary,
                                        title = "Aucune publication",
                                        subtitle = "Cet utilisateur n'a pas encore publié"
                                    )
                                }
                            } else {
                                items(publications.filterNotNull()) { publication ->
                                    PublicationCard(
                                        publication = publication,
                                        isLiked = viewModel.isLikedByCurrentUser(publication),
                                        onLikeClick = { viewModel.toggleLike(publication.id) },
                                        onClick = { /* TODO */ }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Impossible de charger le profil", color = TextPrimary)
                }
            }
        }

        // ✅ REMOVED: Duplicate loading overlay
        // The main loading indicator at line 149 already handles initial page load
        // Only show loading overlay when submitting a new rating (not on page load)
        if (ratingIsLoading && user != null && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog && selectedSortieForRating != null) {
        RatingDialog(
            onDismiss = {
                showRatingDialog = false
                selectedSortieForRating = null
                ratingViewModel.clearMessages()
            },
            onSubmit = { rating, comment ->
                ratingViewModel.submitRating(
                    sortieId = selectedSortieForRating!!,
                    stars = rating,
                    comment = comment,
                    token = token,
                    onSuccess = {
                        showRatingDialog = false
                        selectedSortieForRating = null
                    }
                )
            },
            isLoading = ratingIsLoading,
            errorMessage = ratingError
        )
    }

    // Error snackbar
    ratingError?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            ratingViewModel.clearMessages()
        }
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            color = GreenAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}



@Composable
fun UserProfileHeader(
    userName: String,
    userBio: String,
    avatarUrl: String?,
    followersCount: Int,
    followingCount: Int,
    adventureCount: Int,
    publicationCount: Int,
    participationCount: Int,
    location: String,
    rating: com.example.dam.models.CreatorRatingResponse?
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Rating badge on top (if exists)
        if (rating != null && rating.count > 0) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        val fullStars = rating.average.toInt()
                        val hasHalfStar = (rating.average - fullStars) >= 0.5

                        for (i in 1..5) {
                            Icon(
                                imageVector = when {
                                    i <= fullStars -> Icons.Filled.Star
                                    i == fullStars + 1 && hasHalfStar -> Icons.Filled.StarHalf
                                    else -> Icons.Outlined.StarOutline
                                },
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = String.format("%.1f", rating.average),
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${rating.count} reviews)",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ✅ Avatar in the middle
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(3.dp, GreenAccent, CircleShape)
                .background(CardDark)
        ) {
            AsyncImage(
                model = avatarUrl ?: "",
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.homme),
                placeholder = painterResource(id = R.drawable.homme)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Name under avatar
        Text(
            text = userName,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Email,
                contentDescription = "Email",
                tint = GreenAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = userBio, color = TextSecondary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = GreenAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = location, color = TextSecondary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem(count = adventureCount, label = "Créées")
            ProfileStatItem(count = participationCount, label = "Participations")
            ProfileStatItem(count = followersCount, label = "Followers")
            ProfileStatItem(count = followingCount, label = "Following")
        }
    }
}

@Composable
fun FollowActionButtons(
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { if (isFollowing) onUnfollowClick() else onFollowClick() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing) CardDark else GreenAccent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                contentDescription = if (isFollowing) "Unfollow" else "Follow",
                tint = if (isFollowing) GreenAccent else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFollowing) "Ne plus suivre" else "Suivre",
                color = if (isFollowing) GreenAccent else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onMessageClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = CardDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Message,
                contentDescription = "Message",
                tint = GreenAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Message",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    sortiesCount: Int,
    participationsCount: Int,
    publicationsCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabItem(
            selected = selectedTab == 0,
            icon = Icons.Default.DirectionsBike,
            label = "Créées",
            count = sortiesCount,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        TabItem(
            selected = selectedTab == 1,
            icon = Icons.Default.EmojiPeople,
            label = "Participations",
            count = participationsCount,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )

        TabItem(
            selected = selectedTab == 2,
            icon = Icons.Default.PhotoLibrary,
            label = "Posts",
            count = publicationsCount,
            onClick = { onTabSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) GreenAccent.copy(alpha = 0.2f) else CardGlass,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) GreenAccent else BorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (selected)
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
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) GreenAccent else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (selected) GreenAccent else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = "$count",
                color = if (selected) GreenAccent else TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate this Adventure",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Share your experience",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Star Rating Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star $i",
                            tint = if (i <= selectedRating) Color(0xFFFFD700) else TextTertiary,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(enabled = !isLoading) { selectedRating = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (selectedRating) {
                        1 -> "Poor"
                        2 -> "Fair"
                        3 -> "Good"
                        4 -> "Very Good"
                        5 -> "Excellent"
                        else -> "Tap to rate"
                    },
                    color = if (selectedRating > 0) GreenAccent else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Comment TextField
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (!isLoading) comment = it },
                    placeholder = { Text("Add a comment (optional)", color = TextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = GreenAccent,
                        disabledTextColor = TextSecondary,
                        disabledBorderColor = BorderColor.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // ✅ Show error message if any
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(0.3f))
                    ) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (selectedRating > 0) {
                                onSubmit(selectedRating, comment)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedRating > 0 && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent,
                            disabledContainerColor = CardDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicationCard(
    publication: PublicationResponse,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onClick: () -> Unit
) {
    var localIsLiked by remember { mutableStateOf(isLiked) }
    var localLikesCount by remember { mutableStateOf(publication.likesCount) }

    LaunchedEffect(isLiked) { localIsLiked = isLiked }
    LaunchedEffect(publication.likesCount) { localLikesCount = publication.likesCount }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardGlass,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = publication.author?.avatar
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Author",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.homme),
                            placeholder = painterResource(id = R.drawable.homme)
                        )
                    } else {
                        Text(
                            text = publication.author?.firstName?.take(1)?.uppercase() ?: "?",
                            color = GreenAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = publication.author?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatPublicationDate(publication.createdAt),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = publication.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            val pubImage = publication.image
            if (!pubImage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = pubImage,
                    contentDescription = "Publication image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.homme),
                    placeholder = painterResource(id = R.drawable.homme)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (localIsLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Likes",
                        tint = if (localIsLiked) ErrorRed else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$localLikesCount",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "${publication.commentsCount} comments • ${publication.sharesCount} shares",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        localIsLiked = !localIsLiked
                        localLikesCount += if (localIsLiked) 1 else -1
                        onLikeClick()
                    }
                ) {
                    Icon(
                        imageVector = if (localIsLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(20.dp),
                        tint = if (localIsLiked) ErrorRed else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Like", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                TextButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comment", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                TextButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

fun formatPublicationDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) { dateString }
}


@Composable
fun ParticipationCardWithRating(
    participation: com.example.dam.models.UserParticipationResponse,
    sortie: com.example.dam.models.UserParticipationSortieInfo,
    isCurrentUser: Boolean,
    refreshTrigger: Int = 0,
    onClick: () -> Unit,
    onRatingClick: () -> Unit
) {
    val context = LocalContext.current
    val token = UserPreferences.getToken(context) ?: ""

    var sortieRating by remember { mutableStateOf<SortieRatingData?>(null) }
    var userHasRated by remember { mutableStateOf(false) }
    var userRatingStars by remember { mutableStateOf(0) }

    LaunchedEffect(sortie._id, token, refreshTrigger) {
        if (token.isEmpty()) return@LaunchedEffect

        try {
            val ratingsResponse = RetrofitInstance.adventureApi.getSortieRatings(
                sortieId = sortie._id,
                token = "Bearer $token",
                page = 1,
                limit = 100
            )

            if (ratingsResponse.isSuccessful && ratingsResponse.body() != null) {
                val ratingsData = ratingsResponse.body()!!
                if (ratingsData.ratings.isNotEmpty()) {
                    val avgRating = ratingsData.ratings.map { it.stars }.average()
                    sortieRating = SortieRatingData(average = avgRating, count = ratingsData.total)
                }

                val currentUserId = UserPreferences.getUserId(context)
                val userRating = ratingsData.ratings.find { it.userId == currentUserId }
                if (userRating != null) {
                    userHasRated = true
                    userRatingStars = userRating.stars
                }
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                Log.e("ParticipationCard", "Error: ${e.message}")
            }
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardGlass,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardDark.copy(alpha = 0.5f),
                            CardDark.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Image header with gradient overlay
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val photoUrl = sortie.photo
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = sortie.titre,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.randonne),
                            placeholder = painterResource(id = R.drawable.randonne)
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = when (sortie.type) {
                                    "RANDONNEE" -> R.drawable.randonne
                                    "VELO" -> R.drawable.logo
                                    "CAMPING" -> R.drawable.camping
                                    else -> R.drawable.randonne
                                }
                            ),
                            contentDescription = sortie.titre,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // Status badge (top-right)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = when (participation.status) {
                            "ACCEPTEE" -> SuccessGreen.copy(0.9f)
                            "EN_ATTENTE" -> WarningOrange.copy(0.9f)
                            "REFUSEE" -> ErrorRed.copy(0.9f)
                            else -> CardDark.copy(0.9f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (participation.status) {
                                    "ACCEPTEE" -> Icons.Default.CheckCircle
                                    "EN_ATTENTE" -> Icons.Default.Schedule
                                    "REFUSEE" -> Icons.Default.Cancel
                                    else -> Icons.Default.Info
                                },
                                contentDescription = participation.status,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when (participation.status) {
                                    "ACCEPTEE" -> "Acceptée"
                                    "EN_ATTENTE" -> "En attente"
                                    "REFUSEE" -> "Refusée"
                                    else -> participation.status
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Type badge (top-left)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = when (sortie.type) {
                            "RANDONNEE" -> WarningOrange.copy(0.9f)
                            "VELO" -> TealAccent.copy(0.9f)
                            "CAMPING" -> SuccessGreen.copy(0.9f)
                            else -> CardDark.copy(0.9f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (sortie.type) {
                                    "RANDONNEE" -> Icons.Default.Hiking
                                    "VELO" -> Icons.Default.DirectionsBike
                                    "CAMPING" -> Icons.Default.Terrain
                                    else -> Icons.Default.Explore
                                },
                                contentDescription = sortie.type,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = sortie.type,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Content section
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title
                    Text(
                        text = sortie.titre,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date (if available)
                    sortie.date?.let { date ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                tint = TealAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatDate(date),
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Description (if available)
                    sortie.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 2,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Type chip (alternative display if no description)
                    if (sortie.description.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (sortie.type) {
                                "RANDONNEE" -> WarningOrange.copy(0.2f)
                                "VELO" -> TealAccent.copy(0.2f)
                                "CAMPING" -> SuccessGreen.copy(0.2f)
                                else -> CardDark
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (sortie.type) {
                                        "RANDONNEE" -> Icons.Default.Hiking
                                        "VELO" -> Icons.Default.DirectionsBike
                                        "CAMPING" -> Icons.Default.Terrain
                                        else -> Icons.Default.Explore
                                    },
                                    contentDescription = sortie.type,
                                    tint = when (sortie.type) {
                                        "RANDONNEE" -> WarningOrange
                                        "VELO" -> TealAccent
                                        "CAMPING" -> SuccessGreen
                                        else -> TextSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when (sortie.type) {
                                        "RANDONNEE" -> "Randonnée"
                                        "VELO" -> "Vélo"
                                        "CAMPING" -> "Camping"
                                        else -> sortie.type
                                    },
                                    color = when (sortie.type) {
                                        "RANDONNEE" -> WarningOrange
                                        "VELO" -> TealAccent
                                        "CAMPING" -> SuccessGreen
                                        else -> TextSecondary
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Rating section (only for ACCEPTED participations)
                    if (participation.status == "ACCEPTEE") {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderColor, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Average rating display
                            if (sortieRating != null && sortieRating!!.count > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFFFFD700).copy(0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = String.format("%.1f", sortieRating!!.average),
                                            color = Color(0xFFFFD700),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "(${sortieRating!!.count})",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.StarOutline,
                                        contentDescription = "No rating",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "No ratings yet",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Rate button (only for current user)
                            if (isCurrentUser) {
                                Button(
                                    onClick = onRatingClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (userHasRated)
                                            Color(0xFFFFD700).copy(alpha = 0.25f)
                                        else
                                            GreenAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 2.dp,
                                        pressedElevation = 4.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (userHasRated) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Rate",
                                        tint = if (userHasRated) Color(0xFFFFD700) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (userHasRated) "$userRatingStars ★" else "Rate",
                                        color = if (userHasRated) Color(0xFFFFD700) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to format date
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