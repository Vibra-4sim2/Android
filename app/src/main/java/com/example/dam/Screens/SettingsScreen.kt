package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dam.ui.theme.*
import com.example.dam.utils.ThemePreferences
import com.example.dam.utils.UserPreferences

@Composable
fun SettingsScreen(navController: NavHostController, onThemeChanged: () -> Unit = {}) {
    val context = LocalContext.current

    // ✅ Use global theme state instead of local state
    val themeState = LocalThemeState.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Dynamic colors based on theme
    val isDarkMode = themeState.isDarkMode
    val backgroundColor = if (isDarkMode) BackgroundDark else BackgroundLight
    val gradientStart = if (isDarkMode) BackgroundGradientStart else BackgroundLightGradientStart
    val gradientEnd = if (isDarkMode) BackgroundGradientEnd else BackgroundLightGradientEnd
    val textPrimary = if (isDarkMode) TextPrimary else TextPrimaryLight
    val textSecondary = if (isDarkMode) TextSecondary else TextSecondaryLight
    val cardColor = if (isDarkMode) CardDark else CardLight
    val cardGlass = if (isDarkMode) CardGlass else CardLightGlass
    val borderColor = if (isDarkMode) BorderColor else BorderColorLight
    val greenAccent = if (isDarkMode) GreenAccent else GreenAccentLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientStart, backgroundColor, gradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back Button + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(color = cardGlass, shape = CircleShape)
                        .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Settings",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Appearance Section
            SectionHeader(
                title = "Appearance",
                textPrimary = textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Theme Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = greenAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Dark Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = if (isDarkMode) "Enabled" else "Disabled",
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isChecked ->
                            // ✅ Update global theme state (triggers app-wide recomposition)
                            themeState.isDarkMode = isChecked
                            // Save preference
                            ThemePreferences.setDarkMode(context, isChecked)
                            onThemeChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = greenAccent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = textSecondary
                        )
                    )
                }
            }

            // Account Section
            SectionHeader(
                title = "Account",
                textPrimary = textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update your personal information",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent,
                onClick = {
                    navController.navigate("edit_profile")
                }
            )

            // Notifications Section
            SectionHeader(
                title = "Notifications",
                textPrimary = textPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Manage notification preferences",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent,
                onClick = {
                    navController.navigate("notifications")
                }
            )

            // Help & Support Section
            SectionHeader(
                title = "Help & Support",
                textPrimary = textPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )


            SettingsItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Help Center",
                subtitle = "Get help and support",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent,
                onClick = {
                    navController.navigate("help_center")
                }
            )

            // Logout Section
            SectionHeader(
                title = "Account Actions",
                textPrimary = textPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                        Text(
                            text = "Sign out of your account",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Version
            Text(
                text = "Version 1.0.0",
                fontSize = 12.sp,
                color = textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed)
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        UserPreferences.clear(context)
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
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = cardColor,
            titleContentColor = textPrimary,
            textContentColor = textSecondary
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    textPrimary: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = textPrimary.copy(alpha = 0.7f),
        modifier = modifier
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    cardColor: Color,
    borderColor: Color,
    greenAccent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = greenAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondary
            )
        }
    }
}

