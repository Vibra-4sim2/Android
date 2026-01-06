package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.ui.theme.DamTheme

@Composable
fun ActivitiesSelectionScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selectedActivities = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Main Title - exact same text
        Text(
            text = "What activities do you enjoy?",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle - exact same text
        Text(
            text = "check all that apply.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Separator line - exactly like in the image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Activity buttons - exactly like in the image with 3 options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First button
            GlassySelectionButton(
                text = "Hiking / Randomée",
                isSelected = selectedActivities.contains("Hiking / Randomée"),
                onClick = {
                    if (selectedActivities.contains("Hiking / Randomée")) {
                        selectedActivities.remove("Hiking / Randomée")
                    } else {
                        selectedActivities.add("Hiking / Randomée")
                    }
                }
            )

            // Second button
            GlassySelectionButton(
                text = "Camping",
                isSelected = selectedActivities.contains("Camping"),
                onClick = {
                    if (selectedActivities.contains("Camping")) {
                        selectedActivities.remove("Camping")
                    } else {
                        selectedActivities.add("Camping")
                    }
                }
            )

            // Third button
            GlassySelectionButton(
                text = "Cycling",
                isSelected = selectedActivities.contains("Cycling"),
                onClick = {
                    if (selectedActivities.contains("Cycling")) {
                        selectedActivities.remove("Cycling")
                    } else {
                        selectedActivities.add("Cycling")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("activities") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun GlassySelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Glassy effect background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) {
                    Color(0xFF4CAF50).copy(alpha = 0.3f) // Green when selected
                } else {
                    Color.White.copy(alpha = 0.1f) // Glass effect when not selected
                }
            )
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    Color(0xFF4CAF50).copy(alpha = 0.5f)
                } else {
                    Color.White.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Activity text
            Text(
                text = text,
                color = if (isSelected) Color(0xFF4CAF50) else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            // Selection indicator - simple circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) Color(0xFF4CAF50) else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivitiesSelectionScreenPreview() {
    DamTheme {
        ActivitiesSelectionScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GlassyButtonPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassySelectionButton(
            text = "Hiking / Randomée",
            isSelected = false,
            onClick = {}
        )

        GlassySelectionButton(
            text = "Hiking / Randomée",
            isSelected = true,
            onClick = {}
        )

        GlassySelectionButton(
            text = "Camping",
            isSelected = false,
            onClick = {}
        )

        GlassySelectionButton(
            text = "Camping",
            isSelected = true,
            onClick = {}
        )

        GlassySelectionButton(
            text = "Cycling",
            isSelected = false,
            onClick = {}
        )

        GlassySelectionButton(
            text = "Cycling",
            isSelected = true,
            onClick = {}
        )
    }
}