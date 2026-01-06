// screens/LoginScreen.kt
package com.example.dam.Screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.NavigationRoutes
import com.example.dam.R
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject

private const val RC_SIGN_IN = 9001

@Composable
fun LoginScreen(
    navController: NavController,
    googleSignInClient: GoogleSignInClient,
    activity: Activity,
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Launcher pour Google Sign-In
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In result received: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("LoginScreen", "Google Account: ${account?.email}")
                Log.d("LoginScreen", "ID Token: ${idToken?.take(30)}...")
                if (idToken != null) {
                    Log.d("LoginScreen", "Calling viewModel.googleSignIn()")
                    viewModel.googleSignIn(idToken)
                } else {
                    Log.e("LoginScreen", "ID Token is null!")
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google sign in failed: ${e.statusCode} - ${e.message}", e)
            }
        } else {
            Log.e("LoginScreen", "Result code: ${result.resultCode}")
        }
    }

    // Charger les credentials sauvegardés
    LaunchedEffect(Unit) {
        googleSignInClient.signOut()
        val savedCredentials = loadSavedCredentials(context)
        if (savedCredentials != null) {
            email = savedCredentials.first
            password = savedCredentials.second
            rememberMe = true
        }
    }

    // ✅ SAVE TOKEN IMMEDIATELY on login success (don't wait for preferences)
    LaunchedEffect(uiState.isSuccess, uiState.accessToken) {
        if (uiState.isSuccess && uiState.accessToken != null) {
            val token: String = uiState.accessToken!!

            Log.d("LoginScreen", "========== LOGIN SUCCESS ==========")
            Log.d("LoginScreen", "✅ Login successful")
            Log.d("LoginScreen", "🔑 Token: ${token.take(30)}...")

            // ✅ SAVE TOKEN IMMEDIATELY - Critical for session persistence!
            UserPreferences.saveToken(context, token)
            Log.d("LoginScreen", "💾 Token saved to preferences")

            // Save remember me credentials
            if (rememberMe) {
                saveCredentials(context, email, password)
            } else {
                clearSavedCredentials(context)
            }

            // ✅ START NOTIFICATION POLLING
            com.example.dam.services.NotificationPollingService.startPolling(
                context = context,
                intervalSeconds = 15
            )
            Log.d("LoginScreen", "🔔 Notification polling started")

            // Now check preferences only if not already checked
            if (uiState.needsPreferences == null) {
                val userId: String? = com.example.dam.utils.JwtHelper.getUserIdFromToken(token)

                Log.d("LoginScreen", "🔍 Checking preferences...")
                Log.d("LoginScreen", "👤 UserId from JWT: $userId")

                if (userId != null) {
                    viewModel.checkPreferencesStatus(userId, token)
                } else {
                    Log.e("LoginScreen", "⚠️ Cannot check preferences: userId is null")
                    Log.e("LoginScreen", "⚠️ Token: ${token.take(30)}")
                }
            }

            Log.d("LoginScreen", "===================================")
        }
    }

    // ✅ Navigation after login with preference check complete
    LaunchedEffect(uiState.isSuccess, uiState.needsPreferences) {
        if (uiState.isSuccess && uiState.needsPreferences != null) {
            val token = viewModel.getAccessToken()
            if (token.isNotEmpty()) {
                Log.d("LoginScreen", "========== LOGIN SUCCESS ==========")
                Log.d("LoginScreen", "✅ Login successful")
                Log.d("LoginScreen", "🔑 Token: ${token.take(30)}...")

                // Save authentication data
                saveAuthData(context, token)
                UserPreferences.saveToken(context, token)

                if (rememberMe) {
                    saveCredentials(context, email, password)
                } else {
                    clearSavedCredentials(context)
                }

                // ✅ START NOTIFICATION POLLING
                com.example.dam.services.NotificationPollingService.startPolling(
                    context = context,
                    intervalSeconds = 15
                )
                Log.d("LoginScreen", "🔔 Notification polling started")

                // ✅ DETERMINE NAVIGATION based on preferences check
                val destination = if (uiState.needsPreferences == true) {
                    Log.d("LoginScreen", "→ User needs preferences: Navigate to PREFERENCES")
                    UserPreferences.setOnboardingComplete(context, false)
                    NavigationRoutes.PREFERENCES
                } else {
                    Log.d("LoginScreen", "→ User has preferences: Navigate to HOME")
                    UserPreferences.setOnboardingComplete(context, true)
                    NavigationRoutes.HOME
                }

                Log.d("LoginScreen", "📍 Navigating to: $destination")
                Log.d("LoginScreen", "===================================")

                navController.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    // Dialog d'erreur
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Login Failed", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(uiState.error ?: "An error occurred") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = GreenAccent)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Interface principale
    Box(modifier = modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    )
                )
        )

        // Background Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.download),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(1.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.7f), BackgroundDark),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        // Colonne principale
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo et titre
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(GreenAccent.copy(alpha = 0.3f), Color.Transparent)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vibra),
                        contentDescription = "Bike icon",
                        modifier = Modifier.size(60.dp),
                        colorFilter = ColorFilter.tint(GreenAccent)
                    )
                }
                Text("V!BRA", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("Welcome Back", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("Log in to your adventuring account", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Glass Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardGlass,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(CardDark.copy(alpha = 0.6f), CardDark.copy(alpha = 0.8f))))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Email
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Email", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            GlassTextField(email, { email = it }, "your@email.com", Icons.Default.Email, KeyboardType.Email, false)
                        }

                        // Password
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Password", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            GlassTextField(password, { password = it }, "••••••••", Icons.Default.Lock, KeyboardType.Text, true)
                        }

                        // Remember Me + Forgot Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (rememberMe) GreenAccent.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Icon(
                                        imageVector = if (rememberMe) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (rememberMe) GreenAccent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Remember me", color = if (rememberMe) GreenAccent else TextSecondary, fontSize = 13.sp)
                                }
                            }
                            Text(
                                text = "Forgot password?",
                                color = GreenAccent,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { navController.navigate("forgot_password") }
                            )
                        }

                        // Login Button
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "Create account",
                    color = GreenAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                SocialLoginButton(
                    iconPainter = painterResource(id = R.drawable.google_logo),
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// GlassTextField
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(22.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                modifier = Modifier.weight(1f),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(GreenAccent),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) Text(placeholder, color = TextSecondary)
                    innerTextField()
                }
            )
        }
    }
}

// SocialLoginButton
@Composable
fun SocialLoginButton(
    iconVector: ImageVector? = null,
    iconPainter: Painter? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.size(54.dp),
        shape = CircleShape,
        color = CardGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardDark.copy(alpha = 0.6f))
                .clickable { onClick?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            when {
                iconVector != null -> Icon(iconVector, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(24.dp))
                iconPainter != null -> Image(painter = iconPainter, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// Save, Load, Clear credentials
private fun saveCredentials(context: Context, email: String, password: String) {
    val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("saved_email", email)
        putString("saved_password", password)
        putBoolean("remember_me", true)
        apply()
    }
}

private fun loadSavedCredentials(context: Context): Pair<String, String>? {
    val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val rememberMe = sharedPref.getBoolean("remember_me", false)
    return if (rememberMe) {
        val email = sharedPref.getString("saved_email", "") ?: ""
        val password = sharedPref.getString("saved_password", "") ?: ""
        if (email.isNotEmpty() && password.isNotEmpty()) Pair(email, password) else null
    } else null
}

private fun clearSavedCredentials(context: Context) {
    val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        remove("saved_email")
        remove("saved_password")
        putBoolean("remember_me", false)
        apply()
    }
}

// Save Auth Token
private fun saveAuthData(context: Context, accessToken: String) {
    // Save token using UserPreferences
    UserPreferences.saveToken(context, accessToken)

    // Extract and log userId for debugging
    val userId = com.example.dam.utils.JwtHelper.getUserIdFromToken(accessToken)
    Log.d("LoginScreen", "💾 Saving auth data - UserId: $userId")
    Log.d("LoginScreen", "💾 Token saved: ${accessToken.take(30)}...")
}