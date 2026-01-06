package com.example.dam.Screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.R
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.dam.viewmodel.ForgotPasswordViewModel
import com.example.dam.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Show error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Error", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(uiState.error ?: "Unknown error") },
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

    // Show verification dialog when code is sent
    LaunchedEffect(uiState.codeSent) {
        if (uiState.codeSent) {
            showVerificationDialog = true
        }
    }

    // Navigate when code is verified
    LaunchedEffect(uiState.codeVerified) {
        if (uiState.codeVerified) {
            showVerificationDialog = false
            navController.navigate("reset_password")
        }
    }

    // Verification dialog
    if (showVerificationDialog) {
        VerificationCodeDialog(
            email = uiState.email,
            onDismiss = {
                showVerificationDialog = false
                viewModel.resetState()
            },
            onResend = {
                viewModel.resendCode()
            },
            isLoading = uiState.isLoading,
            viewModel = viewModel
        )
    }

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
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.7f), BackgroundDark),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
            )
        }

        // Main Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo and Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(GreenAccent.copy(alpha = 0.3f), Color.Transparent)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text("V!BRA", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("Forgot Password?", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Enter your email to receive a verification code",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
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
                            Text("Email", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            ForgotPasswordGlassTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "your@email.com",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email
                            )
                        }

                        // Send Button
                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    viewModel.sendResetCode(email)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape = RoundedCornerShape(16.dp),
                            enabled = email.isNotEmpty() && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = BackgroundDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Send Verification Code",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BackgroundDark
                                    )
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = BackgroundDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back to Login
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Back to login",
                    color = GreenAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate("login") {
                            popUpTo("forgot_password") { inclusive = true }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VerificationCodeDialog(
    email: String,
    onDismiss: () -> Unit,
    onResend: () -> Unit,
    isLoading: Boolean = false,
    viewModel: ForgotPasswordViewModel
) {
    var code by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter Verification Code", fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "We sent a 6-character code to $email",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isLetterOrDigit() }) {
                            code = it.uppercase()
                        }
                    },
                    label = { Text("Code") },
                    placeholder = { Text("ABC123") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        focusedLabelColor = GreenAccent,
                        cursorColor = GreenAccent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onResend,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = GreenAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resend Code", color = GreenAccent)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.length == 6) {
                        viewModel.verifyResetCode(code)
                    }
                },
                colors = ButtonDefaults.buttonColors(GreenAccent),
                enabled = code.length == 6 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BackgroundDark,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Verify", color = BackgroundDark)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = TextTertiary)
            }
        },
        containerColor = CardDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(20.dp)
    )
}

// GlassTextField (same as Login)
@Composable
fun ForgotPasswordGlassTextField(
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
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(GreenAccent),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) Text(placeholder, color = TextSecondary)
                    innerTextField()
                }
            )
        }
    }
}