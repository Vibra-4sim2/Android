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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dam.ui.theme.*

@Composable
fun EditProfile2Screen(navController: NavHostController) {
    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    var cyclingLevel by remember { mutableStateOf("Beginner") }
    var physicalCondition by remember { mutableStateOf("Average") }
    var rideFrequency by remember { mutableStateOf("once a month") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    } else {
                        listOf(BackgroundLightGradientStart, BackgroundLight, BackgroundLightGradientEnd)
                    }
                )
            )
            .imePadding()  // ✅ Handle keyboard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp) // ✅ Space for top bar
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 120.dp) // ✅ Space for bottom nav bar
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Back button + Title ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = CardGlass,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = BorderColor,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Edit Profile",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question 1
            QuestionBlock(
                title = "How would you describe your cycling level?",
                options = listOf("Beginner", "Intermediate", "Advanced"),
                selected = cyclingLevel,
                onSelect = { cyclingLevel = it }
            )

            // Question 2
            QuestionBlock(
                title = "How would you describe your physical condition?",
                options = listOf("Low", "Average", "High", "Prefer not to say"),
                selected = physicalCondition,
                onSelect = { physicalCondition = it }
            )

            // Question 3
            QuestionBlock(
                title = "How often do you usually ride a bike?",
                options = listOf(
                    "Once a week",
                    "2-3 times a week",
                    "Almost every day",
                    "Once a month"
                ),
                selected = rideFrequency,
                onSelect = { rideFrequency = it }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ✅ Submit Button with modern gradient design
            Button(
                onClick = {
                    // Navigate to profile screen
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
                    .height(52.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = GreenAccent.copy(alpha = 0.4f),
                        spotColor = GreenAccent.copy(alpha = 0.6f)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(GreenLight, GreenAccent)
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Submit",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun QuestionBlock(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    options.forEach { option ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onSelect(option) }
        ) {
            RadioButton(
                selected = selected == option,
                onClick = { onSelect(option) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = GreenAccent,
                    unselectedColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = option,
                color = if (selected == option) TextPrimary else TextSecondary,
                fontSize = 15.sp,
                fontWeight = if (selected == option) FontWeight.Medium else FontWeight.Normal
            )
        }
    }

    Spacer(modifier = Modifier.height(22.dp))
}