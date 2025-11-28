package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dam.R
import com.example.dam.models.PublicationResponse
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider


@Composable
fun UserProfileScreen(
    navController: NavHostController,
    userId: String
) {
    val context = LocalContext.current

    // ✅ Fixed: Create ViewModel with factory in composable context
    val viewModel: UserProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(context.applicationContext) as T
            }
        }
    )

    val token = UserPreferences.getToken(context) ?: ""

    // Observe states
    val user by viewModel.user.collectAsState()
    val sorties by viewModel.userSorties.collectAsState()
    val publications by viewModel.userPublications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Load user data
    LaunchedEffect(userId, token) {
        if (token.isNotEmpty()) {
            viewModel.loadUserProfile(userId, token)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            if (isLoading && user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GreenAccent,
                        strokeWidth = 3.dp
                    )
                }
            } else if (user != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    item {
                        UserProfileHeader(
                            userName = "${user!!.firstName} ${user!!.lastName}",
                            userBio = user!!.email,
                            avatarUrl = user!!.avatar,
                            followersCount = followersCount,
                            followingCount = followingCount,
                            adventureCount = sorties.size,
                            publicationCount = publications.size,
                            location = "Tunisia"
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
                            publicationsCount = publications.size
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (selectedTab == 0) {
                        if (sorties.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.DirectionsBike,
                                    title = "Aucune sortie",
                                    subtitle = "Cet utilisateur n'a pas encore créé de sorties"
                                )
                            }
                        } else {
                            items(sorties) { sortie ->
                                ModernEventCard(
                                    sortie = sortie,
                                    onClick = { navController.navigate("sortieDetail/${sortie.id}") }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        if (publications.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.PhotoLibrary,
                                    title = "Aucune publication",
                                    subtitle = "Cet utilisateur n'a pas encore publié"
                                )
                            }
                        } else {
                            items(publications) { publication ->
                                PublicationCard(
                                    publication = publication,
                                    isLiked = viewModel.isLikedByCurrentUser(publication),
                                    onLikeClick = { viewModel.toggleLike(publication.id) },
                                    onClick = { /* TODO: navigate to publication detail */ }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
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
    publicationCount: Int,  // ✅ NEW
    location: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(3.dp, GreenAccent, CircleShape)
                .background(CardDark)
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.homme),
                    placeholder = painterResource(id = R.drawable.homme)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.homme),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(count = adventureCount, label = "sorties")
            StatItem(count = publicationCount, label = "posts")  // ✅ NEW
            StatItem(count = followersCount, label = "followers")
            StatItem(count = followingCount, label = "following")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = userName,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bio
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = GreenAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = userBio, color = TextSecondary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = GreenAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = location, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun TabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    sortiesCount: Int,
    publicationsCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab: Créées (Sorties)
        Surface(
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = if (selectedTab == 0) GreenAccent.copy(alpha = 0.2f) else CardGlass,
            border = androidx.compose.foundation.BorderStroke(
                width = if (selectedTab == 0) 1.5.dp else 1.dp,
                color = if (selectedTab == 0) GreenAccent else BorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        if (selectedTab == 0)
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
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = "Sorties",
                    tint = if (selectedTab == 0) GreenAccent else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Créées",
                    color = if (selectedTab == 0) GreenAccent else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = "$sortiesCount",
                    color = if (selectedTab == 0) GreenAccent else TextTertiary,
                    fontSize = 12.sp
                )
            }
        }

        // Tab: Publications
        Surface(
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = if (selectedTab == 1) GreenAccent.copy(alpha = 0.2f) else CardGlass,
            border = androidx.compose.foundation.BorderStroke(
                width = if (selectedTab == 1) 1.5.dp else 1.dp,
                color = if (selectedTab == 1) GreenAccent else BorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        if (selectedTab == 1)
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
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Publications",
                    tint = if (selectedTab == 1) GreenAccent else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Publications",
                    color = if (selectedTab == 1) GreenAccent else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = "$publicationsCount",
                    color = if (selectedTab == 1) GreenAccent else TextTertiary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PublicationCard(
    publication: PublicationResponse,
    isLiked: Boolean = false,  // ✅ ADD THIS
    onLikeClick: () -> Unit = {},  // ✅ ADD THIS
    onClick: () -> Unit
) {
    var localIsLiked by remember { mutableStateOf(isLiked) }
    var localLikesCount by remember { mutableStateOf(publication.likesCount) }

    // ✅ UPDATE LOCAL STATE WHEN PROP CHANGES
    LaunchedEffect(isLiked) {
        localIsLiked = isLiked
    }

    LaunchedEffect(publication.likesCount) {
        localLikesCount = publication.likesCount
    }

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
                .padding(16.dp)
        ) {
            // Author info
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
                    if (publication.author?.avatar != null && publication.author.avatar.isNotEmpty()) {
                        AsyncImage(
                            model = publication.author.avatar,
                            contentDescription = "Author",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
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

            // Content
            Text(
                text = publication.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Image if exists
            if (!publication.image.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = publication.image,
                    contentDescription = "Publication image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ STATS ROW (UPDATED)
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

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ INTERACTION BUTTONS (UPDATED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like button
                TextButton(
                    onClick = {
                        localIsLiked = !localIsLiked
                        localLikesCount += if (localIsLiked) 1 else -1
                        onLikeClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (localIsLiked) ErrorRed else TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = if (localIsLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Like",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comment button
                TextButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share button
                TextButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
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
    } catch (e: Exception) {
        dateString
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
// StatItem is now defined in a common composables file
// Remove this duplicate definition

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