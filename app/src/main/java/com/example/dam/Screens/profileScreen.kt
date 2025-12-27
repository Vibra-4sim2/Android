package com.example.dam.Screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.dam.ui.theme.*
import com.example.dam.viewmodel.UserProfileViewModel
import com.example.dam.viewmodel.UserViewModel        // ← This is the one that has uploadAvatar
import androidx.lifecycle.ViewModelProvider

@Composable
fun ProfileScreen(
    navController: NavHostController,
    showDropdown: Boolean = false
) {
    val context = LocalContext.current

    // Token & userId
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("access_token", "") ?: ""
    val userId = sharedPref.getString("user_id", "") ?: ""

    // ViewModels
    val profileViewModel: UserProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(context.applicationContext) as T
            }
        }
    )
    val userViewModel: UserViewModel = viewModel()   // ← This one has uploadAvatar()

    // Data from backend (sorties & publications of the connected user)
    val user by profileViewModel.user.collectAsState()
    val sorties by profileViewModel.userSorties.collectAsState()
    val publications by profileViewModel.userPublications.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val followersCount by profileViewModel.followersCount.collectAsState()
    val followingCount by profileViewModel.followingCount.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // ---------- AVATAR UPLOAD (fixed) ----------
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            userViewModel.uploadAvatar(                 // ← This function exists!
                userId = userId,
                imageUri = it,
                context = context,
                onSuccess = {
                    // Force refresh profile picture
                    profileViewModel.loadUserProfile(userId, token)
                },
                onError = { msg ->
                    android.util.Log.e("ProfileScreen", "Avatar upload failed: $msg")
                }
            )
        }
    }

    // Load current user profile + sorties + publications
    LaunchedEffect(userId, token) {
        if (userId.isNotEmpty() && token.isNotEmpty()) {
            profileViewModel.loadUserProfile(userId, token)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            // YOUR ORIGINAL HEADER – 100% unchanged
            ProfileHeaderNew(
                userName = user?.let { "${it.firstName} ${it.lastName}" } ?: "Mon Profil",
                userBio = user?.email ?: "",
                avatarUrl = user?.avatar,
                adventureCount = sorties.size,
                followersCount = followersCount,
                followingCount = followingCount,
                location = "Tunis",
                isLoading = isLoading,
                onImageClick = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(navController)
            Spacer(modifier = Modifier.height(24.dp))

            // YOUR ORIGINAL TABS (bike + add)
            TabSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            Spacer(modifier = Modifier.height(16.dp))

            // REAL DATA FROM BACKEND
            if (selectedTab == 0) {
                if (sorties.isEmpty()) {
                    EmptyContentState(
                        icon = Icons.Default.DirectionsBike,
                        title = "Aucune sortie créée",
                        subtitle = "Créez votre première aventure !"
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        sorties.forEach { sortie ->
                            ModernEventCard(
                                sortie = sortie,
                                onClick = { navController.navigate("sortieDetail/${sortie.id}") }
                            )
                        }
                    }
                }
            } else {
                if (publications.isEmpty()) {
                    EmptyContentState(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Aucune publication",
                        subtitle = "Partagez vos moments ici"
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        publications.forEach { publication ->
                            PublicationCard(
                                publication = publication,
                                isLiked = profileViewModel.isLikedByCurrentUser(publication),
                                onLikeClick = { profileViewModel.toggleLike(publication.id) },
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ——— ALL YOUR ORIGINAL COMPOSABLES BELOW (unchanged) ———
@Composable
fun ProfileHeaderNew(
    userName: String,
    userBio: String,
    avatarUrl: String?,
    adventureCount: Int,
    followersCount: Int,
    followingCount: Int,
    location: String,
    isLoading: Boolean,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(100.dp).clickable { onImageClick() }) {
            Box(
                modifier = Modifier.fillMaxSize()
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
                        error = painterResource(R.drawable.homme),
                        placeholder = painterResource(R.drawable.homme)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.homme),
                        contentDescription = "Default",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Box(
                modifier = Modifier.size(32.dp).align(Alignment.BottomEnd)
                    .clip(CircleShape).background(GreenAccent).clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, "Change photo", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(adventureCount, "aventures")
            StatItem(followersCount, "followers")
            StatItem(followingCount, "following")
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(Modifier.size(24.dp), color = GreenAccent, strokeWidth = 2.dp)
        } else {
            Text(userName, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("bike", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("Passionné de vélo et nature", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("mountain", fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("mountain", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("Explorateur d'aventures | Tunisie", color = TextSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = GreenAccent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(location, color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("•", color = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Explore, null, tint = GreenAccent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("$adventureCount Adventures Created", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun ActionButtons(navController: NavHostController) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { navController.navigate("edit_profile") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = CardDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, null, tint = GreenAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Edit profil", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Button(
            onClick = { /* Share */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share profil", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        TabItem(Icons.Default.DirectionsBike, "My Adventures", selectedTab == 0, { onTabSelected(0) }, Modifier.weight(1f))
        TabItem(Icons.Default.PhotoLibrary, "MMy Publications", selectedTab == 1, { onTabSelected(1) }, Modifier.weight(1f))
    }
}

@Composable
fun TabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(50.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CardDark else Color.Transparent,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) GreenAccent else BorderColor)
    ) {
        Row(
            Modifier.fillMaxSize()
                .then(if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(GreenAccent.copy(0.15f), TealAccent.copy(0.15f)))) else Modifier)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, label, tint = if (isSelected) GreenAccent else TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = if (isSelected) GreenAccent else TextSecondary, fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
fun EmptyContentState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}