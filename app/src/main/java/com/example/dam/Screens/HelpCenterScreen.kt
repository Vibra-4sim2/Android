package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
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

@Composable
fun HelpCenterScreen(navController: NavHostController) {
    val context = LocalContext.current

    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    // Dynamic colors based on theme
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
                    text = "Help Center",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Welcome message
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = greenAccent,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "We're here to help!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Find answers to common questions",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // FAQ Section
            Text(
                text = "Frequently Asked Questions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // FAQ Items
            FAQItem(
                icon = Icons.Default.AccountCircle,
                question = "How do I update my profile?",
                answer = "Go to your Profile screen, tap the 'Edit Profile' button, make your changes, and tap the save button (arrow icon) to confirm.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.AutoMirrored.Filled.DirectionsBike,
                question = "How do I create a new adventure?",
                answer = "Tap the '+' button in the bottom navigation bar, fill in your adventure details including location, date, difficulty, and description, then tap 'Create Adventure'.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.Default.LocationOn,
                question = "How does location sharing work?",
                answer = "When creating an adventure, you can set a meeting point using the map. Other users can see this location and navigate to it. Your real-time location is only shared if you enable it during an active adventure.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.Default.Notifications,
                question = "How do notifications work?",
                answer = "You'll receive notifications for new messages, adventure invitations, and updates from adventures you've joined. You can manage notification preferences in Settings.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.Default.Group,
                question = "How do I join an adventure?",
                answer = "Browse adventures on the Home screen or Map view. Tap on any adventure to see details, then tap 'Join Adventure' to participate. The creator will be notified of your interest.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.Default.Security,
                question = "Is my data secure?",
                answer = "Yes! We use industry-standard encryption to protect your data. Your personal information is never shared with third parties without your explicit consent.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            FAQItem(
                icon = Icons.Default.Lock,
                question = "How do I reset my password?",
                answer = "On the login screen, tap 'Forgot Password', enter your email, and follow the instructions sent to your email to reset your password.",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor,
                borderColor = borderColor,
                greenAccent = greenAccent
            )

            // Contact Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Still need help?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = greenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Email Support",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "support@vibra-adventures.com",
                                fontSize = 14.sp,
                                color = greenAccent
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = borderColor
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = greenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Documentation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Visit our online help center",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FAQItem(
    icon: ImageVector,
    question: String,
    answer: String,
    textPrimary: Color,
    textSecondary: Color,
    cardColor: Color,
    borderColor: Color,
    greenAccent: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = greenAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = textSecondary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    color = textSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

