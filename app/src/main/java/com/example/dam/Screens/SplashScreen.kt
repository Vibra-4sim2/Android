package com.example.dam.Screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dam.repository.AuthRepository
import com.example.dam.repository.Result
import com.example.dam.ui.theme.*
import com.example.dam.utils.JwtHelper
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController) {
    val greenColor = GreenAccent
    val backgroundColor = BackgroundDark
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }

    // State for showing token expiration dialog
    var showTokenExpiredDialog by remember { mutableStateOf(false) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val lineWidth by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1500),
        label = "fade"
    )

    LaunchedEffect(Unit) {
        delay(2000)

        val isFirstLaunch = UserPreferences.isFirstLaunch(context)
        val token = UserPreferences.getToken(context)
        val userId = UserPreferences.getUserId(context)

        Log.d("SplashScreen", "========== SPLASH NAVIGATION ==========")
        Log.d("SplashScreen", "🆕 isFirstLaunch: $isFirstLaunch")
        Log.d("SplashScreen", "🔑 token: ${token?.take(20)}")
        Log.d("SplashScreen", "👤 userId: $userId")

        val destination = when {
            // ✅ Case 1: First app launch → Onboarding
            isFirstLaunch -> {
                Log.d("SplashScreen", "🆕 First launch → Onboarding")
                "onboarding1"
            }

            // ✅ Case 2: Has token and userId → Validate and check preferences
            token != null && userId != null -> {
                // ✅ CHECK IF TOKEN IS EXPIRED
                val isExpired = JwtHelper.isTokenExpired(token)
                Log.d("SplashScreen", "🔐 Token expired: $isExpired")

                if (isExpired) {
                    // Token expired - show alert and go to login
                    Log.d("SplashScreen", "⚠️ Token expired → Clearing session and showing alert")
                    UserPreferences.clear(context)
                    showTokenExpiredDialog = true
                    "login"
                } else {
                    // Token valid - check preferences status
                    Log.d("SplashScreen", "🔍 User logged in, checking preferences status...")
                    Log.d("SplashScreen", "⏱️ Will timeout after 5 seconds if no response")

                    // ✅ CALL BACKEND WITH TIMEOUT HANDLING
                    val result = try {
                        withTimeout(5000L) { // 5 second timeout
                            authRepository.checkOnboardingStatus(userId, token)
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.w("SplashScreen", "⏱️ Timeout checking preferences - using local cache")
                        null
                    }

                    when (result) {
                        is Result.Success -> {
                            if (result.data) {
                                Log.d("SplashScreen", "✅ User has completed preferences → Home")
                                UserPreferences.setOnboardingComplete(context, true)
                                "home"
                            } else {
                                Log.d("SplashScreen", "⚠️ User needs to complete preferences → Preferences")
                                UserPreferences.setOnboardingComplete(context, false)
                                "preferences"
                            }
                        }
                        is Result.Error -> {
                            Log.e("SplashScreen", "❌ Error checking preferences: ${result.message}")

                            // Check if error is due to authentication (401/403)
                            if (result.message.contains("401") || result.message.contains("403") ||
                                result.message.contains("Unauthorized") || result.message.contains("authentication")) {
                                Log.d("SplashScreen", "🔐 Authentication error → Clearing session and showing alert")
                                UserPreferences.clear(context)
                                showTokenExpiredDialog = true
                                "login"
                            } else {
                                // Network error - use local cache as fallback
                                Log.d("SplashScreen", "🌐 Network error - using offline mode")
                                val localOnboardingComplete = UserPreferences.isOnboardingComplete(context)
                                if (localOnboardingComplete) {
                                    Log.d("SplashScreen", "✅ Using local cache → Home")
                                    "home"
                                } else {
                                    Log.d("SplashScreen", "⚠️ Using local cache → Preferences")
                                    "preferences"
                                }
                            }
                        }
                        null, is Result.Loading -> {
                            // Timeout or loading state - use local cache
                            Log.d("SplashScreen", "⏱️ Using local cache after timeout")
                            val localOnboardingComplete = UserPreferences.isOnboardingComplete(context)
                            if (localOnboardingComplete) {
                                Log.d("SplashScreen", "✅ Local cache → Home")
                                "home"
                            } else {
                                Log.d("SplashScreen", "⚠️ Local cache → Preferences")
                                "preferences"
                            }
                        }
                    }
                }
            }

            // ✅ Case 3: Has token but no userId (shouldn't happen)
            token != null -> {
                Log.d("SplashScreen", "⚠️ Token exists but no userId → Clear and go to Login")
                UserPreferences.clear(context)
                "login"
            }

            // ✅ Case 4: No token → Login
            else -> {
                Log.d("SplashScreen", "🔐 No token → Login")
                "login"
            }
        }

        Log.d("SplashScreen", "→ Navigating to: $destination")
        Log.d("SplashScreen", "=====================================")

        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ✅ Token Expired Alert Dialog
    if (showTokenExpiredDialog) {
        AlertDialog(
            onDismissRequest = {
                showTokenExpiredDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Session Expired", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Your session has expired. Please sign in again to continue.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTokenExpiredDialog = false
                    }
                ) {
                    Text("OK", color = GreenAccent)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        AnimatedParticles(color = greenColor)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(
                color = greenColor.copy(alpha = 0.1f),
                radius = 250f,
                center = center,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = greenColor.copy(alpha = 0.15f),
                radius = 200f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.offset(y = (-20).dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("V")
                        withStyle(style = SpanStyle(color = greenColor)) { append("!") }
                        append("BRA")
                    },
                    fontSize = (100 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = greenColor.copy(alpha = glowIntensity),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(GlowGreen, Offset(0f, 0f), 40f)
                    )
                )

                Text(
                    text = buildAnnotatedString {
                        append("V")
                        withStyle(style = SpanStyle(color = greenColor)) { append("!") }
                        append("BRA")
                    },
                    fontSize = (100 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = TextPrimary,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(GlowGreen, Offset(0f, 0f), 30f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Canvas(modifier = Modifier.width(lineWidth.dp).height(2.dp)) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, greenColor, Color.Transparent)
                    ),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "FEEL THE ENERGY",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 5.sp,
                color = greenColor.copy(alpha = alpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "RIDE • CONNECT • THRIVE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp,
                color = TextSecondary.copy(alpha = alpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AnimatedParticles(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val particles = remember {
        List(40) {
            ParticleData(
                x = Random.nextFloat(),
                initialY = Random.nextFloat(),
                delay = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = ((p.initialY - (progress + p.delay)) % 1f + 1f) % 1f
            drawCircle(
                color = color.copy(alpha = 0.20f),
                radius = 4f,
                center = Offset(p.x * size.width, y * size.height)
            )
        }
    }
}

data class ParticleData(
    val x: Float,
    val initialY: Float,
    val delay: Float
)