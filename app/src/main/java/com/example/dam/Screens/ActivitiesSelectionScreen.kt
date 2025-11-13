package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.ui.theme.DamTheme

@Composable
fun ActivitiesSelectionScreen(navController: NavController) {
    var selectedActivities by remember { mutableStateOf(setOf<String>()) }
    val greenColor = Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Back Button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "What activities\ndo you enjoy?",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "check all that apply.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Activities Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ActivityOption(
                    text = "Cycling 🚴",
                    isSelected = selectedActivities.contains("cycling"),
                    onClick = {
                        selectedActivities = if (selectedActivities.contains("cycling")) {
                            selectedActivities - "cycling"
                        } else {
                            selectedActivities + "cycling"
                        }
                    },
                    greenColor = greenColor
                )

                ActivityOption(
                    text = "Hiking / Randonnée ⛰️",
                    isSelected = selectedActivities.contains("hiking"),
                    onClick = {
                        selectedActivities = if (selectedActivities.contains("hiking")) {
                            selectedActivities - "hiking"
                        } else {
                            selectedActivities + "hiking"
                        }
                    },
                    greenColor = greenColor
                )

                ActivityOption(
                    text = "Camping 🏕️",
                    isSelected = selectedActivities.contains("camping"),
                    onClick = {
                        selectedActivities = if (selectedActivities.contains("camping")) {
                            selectedActivities - "camping"
                        } else {
                            selectedActivities + "camping"
                        }
                    },
                    greenColor = greenColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section with Cyclist Image and Next Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Cyclist Image Left
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )

                // Next Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable {
                            navController.navigate("terrain_selection")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Cyclist Image Right (placeholder)
                Spacer(modifier = Modifier.size(80.dp))
            }
        }
    }
}

@Composable
fun ActivityOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    greenColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (isSelected) greenColor.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) greenColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) greenColor else Color.White,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivitiesSelectionPreview() {
    DamTheme {
        val navController = rememberNavController()
        ActivitiesSelectionScreen(navController = navController)
    }
}