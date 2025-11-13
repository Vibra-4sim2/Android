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
import androidx.compose.material.icons.filled.ArrowBack
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

private val BackgroundColor = Color(0xFF0B0B0B)
val PrimaryText = Color.White
private val SecondaryText = Color(0xFFBDBDBD)
private val AccentGreen = Color(0xFF36C36A)
private val ButtonGradientStart = Color(0xFF39C06B)
private val ButtonGradientEnd = Color(0xFF2EA15A)

@Composable
fun EditProfile2Screen(navController: NavHostController) {  // ✅ Removed showDropdown parameter

    var cyclingLevel by remember { mutableStateOf("Beginner") }
    var physicalCondition by remember { mutableStateOf("Average") }
    var rideFrequency by remember { mutableStateOf("once a month") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .imePadding()  // ✅ Handle keyboard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp, bottom = 100.dp)
        ) {
            // --- Back button + Title ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.30f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 24.sp,
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

            // ✅ Submit Button (go to Profile or Home)
            Button(
                onClick = {
                    // Navigate to home with profile tab selected
                    navController.navigate("home") {
                        popUpTo("edit_profile") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp)),
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
                                listOf(ButtonGradientStart, ButtonGradientEnd)
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
        color = PrimaryText,
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
                    selectedColor = AccentGreen,
                    unselectedColor = SecondaryText
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = option,
                color = if (selected == option) PrimaryText else SecondaryText,
                fontSize = 15.sp,
                fontWeight = if (selected == option) FontWeight.Medium else FontWeight.Normal
            )
        }
    }

    Spacer(modifier = Modifier.height(22.dp))
}