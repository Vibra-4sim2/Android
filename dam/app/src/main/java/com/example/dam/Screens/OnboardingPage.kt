package com.example.dam.Screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.ui.theme.DamTheme
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val highlightedTitle: String,
    val description: String,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            title = "Enjoy Outdoor",
            highlightedTitle = "Activities",
            description = "The hardest thing to deal with is love. Desert Ulamco is painful, gives, and is.",
            imageRes = R.drawable.logo
        ),
        OnboardingPage(
            title = "Track Your",
            highlightedTitle = "Progress",
            description = "Monitor your cycling stats, distance, speed and calories burned in real-time.",
            imageRes = R.drawable.homme
        ),
        OnboardingPage(
            title = "Join the",
            highlightedTitle = "Community",
            description = "Connect with cyclists worldwide, share your rides and compete with friends.",
            imageRes = R.drawable.camping
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val greenColor = Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Status Bar + Back Button + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (visible sauf sur la première page)
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = greenColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Spacer pour maintenir l'alignement
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Skip Button
                Text(
                    text = "Skip",
                    color = greenColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            // HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page], greenColor)
            }

            // Bottom Section: Indicators + Next Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                                    height = 8.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pagerState.currentPage == index) greenColor
                                    else greenColor.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Next/Get Started Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(greenColor)
                        .clickable {
                            scope.launch {
                                if (pagerState.currentPage < pages.size - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            }
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
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage, greenColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Title
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = page.title,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp
            )
            Text(
                text = page.highlightedTitle,
                color = greenColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp
            )
        }

        // Description
        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Image with circular mask
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Decorative circle background
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                greenColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Main image
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    DamTheme {
        val navController = rememberNavController()
        OnboardingScreen(navController = navController)
    }
}