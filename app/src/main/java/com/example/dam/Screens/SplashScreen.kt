package com.example.dam.Screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.NavigationRoutes
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController) {
    // Use theme colors
    val greenColor = GreenAccent
    val backgroundColor = BackgroundDark
    val context = LocalContext.current


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

        // Vérifier l'état de l'utilisateur
        val isFirstLaunch = UserPreferences.isFirstLaunch(context)
        val token = UserPreferences.getToken(context)
        val isOnboardingComplete = UserPreferences.isOnboardingComplete(context)

        Log.d("SplashScreen", "========== SPLASH NAVIGATION ==========")
        Log.d("SplashScreen", "isFirstLaunch: $isFirstLaunch")
        Log.d("SplashScreen", "token: ${token?.take(20)}")
        Log.d("SplashScreen", "isOnboardingComplete: $isOnboardingComplete")

        val destination = when {
            // Cas 1 : Première utilisation → Onboarding
            isFirstLaunch -> {
                Log.d("SplashScreen", "🆕 Première utilisation → Onboarding")
                "onboarding1"
            }

            // Cas 2 : Token existe ET préférences complétées → Home directement
            token != null && isOnboardingComplete -> {
                Log.d("SplashScreen", "✅ Utilisateur connecté → Home")
                "home"
            }

            // Cas 3 : Token existe MAIS préférences pas complétées → Preferences
            token != null && !isOnboardingComplete -> {
                Log.d("SplashScreen", "⚠️ Token existe mais préférences incomplètes → Preferences")
                "preferences"
            }

            // Cas 4 : Pas de token (logout ou jamais connecté) → Login
            else -> {
                Log.d("SplashScreen", "🔐 Pas de token → Login")
                "login"
            }
        }

        Log.d("SplashScreen", "→ Destination: $destination")
        Log.d("SplashScreen", "=====================================")

        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Animated particles
        AnimatedParticles(color = greenColor)

        // Background circles
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
                // Background shadow text with glow animation
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

                // Foreground text with scale animation
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

            // Animated horizontal line
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashPreview() {
    DamTheme {
        SplashScreen(navController = rememberNavController())
    }
}