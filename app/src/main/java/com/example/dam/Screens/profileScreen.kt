package com.example.dam.Screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dam.R
import com.example.dam.viewmodel.UserViewModel

// Colors - Make sure these match your theme
private val BackgroundDark = Color(0xFF0F0F0F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)
private val GreenAccent = Color(0xFF4ADE80)

@Composable
fun ProfileScreen(
    navController: NavHostController,
    showDropdown: Boolean = false,
    viewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current

    // Get stored auth data
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("access_token", "") ?: ""
    val userId = sharedPref.getString("user_id", "") ?: ""

    // Observe user data from ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        if (userId.isNotEmpty() && token.isNotEmpty()) {
            viewModel.loadUserProfile(userId, token)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp) // ✅ Space for top bar
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp) // ✅ Space for bottom nav bar
        ) {
            // Pass user data to ProfileHeader
            ProfileHeader(
                navController = navController,
                userName = currentUser?.let { "${it.firstName} ${it.lastName}" } ?: "Loading...",
                userEmail = currentUser?.email ?: "",
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Statistiques
            StatisticsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    navController: NavHostController,
    userName: String,
    userEmail: String,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Photo de profil
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.homme),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display actual user name with loading state
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = GreenAccent,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = userName,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display user email
        if (!isLoading && userEmail.isNotEmpty()) {
            Text(
                text = userEmail,
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Localisation et followers
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                contentDescription = "Location",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "San Francisco, CA",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                contentDescription = "Followers",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "245 followers",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date d'inscription
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                contentDescription = "Calendar",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Member since 2018",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bouton Edit Profile
        Button(
            onClick = {
                navController.navigate("edit_profile")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(44.dp),
            enabled = !isLoading
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatisticsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Your Statistics",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Conteneur parent avec effet Glass 26
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A1A).copy(alpha = 0.26f),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF2A2A2A).copy(alpha = 0.26f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                // Grille de statistiques 2x2 avec effet Glassy
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = "📊",
                            label = "Distance",
                            value = "2,547",
                            unit = "km",
                            subtitle = "+123 km this month",
                            iconColor = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            icon = "⏱",
                            label = "Time",
                            value = "187",
                            unit = "hours",
                            subtitle = "+8 hours this month",
                            iconColor = GreenAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = "⛰",
                            label = "Elevation",
                            value = "28,650",
                            unit = "m",
                            subtitle = "+1,200 m this month",
                            iconColor = GreenAccent,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            icon = "🔥",
                            label = "Calories",
                            value = "78,345",
                            unit = "kcal",
                            subtitle = "+3,450 kcal this month",
                            iconColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: String,
    label: String,
    value: String,
    unit: String,
    subtitle: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1F1F1F).copy(alpha = 0.5f),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFF252525).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header avec icône et label
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = icon,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Valeur principale
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Sous-titre
                Text(
                    text = subtitle,
                    color = GreenAccent,
                    fontSize = 11.sp
                )
            }
        }
    }
}