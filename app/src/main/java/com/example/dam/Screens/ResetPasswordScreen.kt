package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dam.R
import com.example.dam.viewmodel.ForgotPasswordViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel  // ✅ Shared ViewModel from MainActivity
) {
    // States
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordsMatch by remember { mutableStateOf(true) }

    // Observe ViewModel state
    val uiState by viewModel.uiState.collectAsState()

    // Colors
    val greenColor = Color(0xFF4CAF50)
    val backgroundColor = Color.Black.copy(alpha = 0.55f)

    // ✅ Navigate to login when password reset is successful
    LaunchedEffect(uiState.passwordReset) {
        if (uiState.passwordReset) {
            // Show success message briefly before navigating
            kotlinx.coroutines.delay(500)
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }  // Clear entire back stack
            }
        }
    }

    // ✅ Show success dialog when password is reset
    if (uiState.passwordReset) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = greenColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            text = {
                Text(
                    "Your password has been reset successfully!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(greenColor)
                ) {
                    Text("Go to Login")
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        )

        // Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = greenColor)
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header (Logo + Titles)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = greenColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "V!BRA",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Reset Password",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please enter your new password",
                    color = greenColor.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF5350).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF5350)
                        )
                        Column {
                            Text(
                                text = uiState.error!!,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (uiState.error!!.contains("expired", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please request a new code",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // New Password Field
            PasswordFieldWithVisibility(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    passwordsMatch = confirmPassword.isEmpty() || newPassword == confirmPassword
                },
                placeholder = "Enter new password",
                icon = Icons.Default.Lock,
                greenColor = greenColor,
                showPassword = showNewPassword,
                onToggleVisibility = { showNewPassword = !showNewPassword }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            PasswordFieldWithVisibility(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordsMatch = newPassword == confirmPassword
                },
                placeholder = "Confirm new password",
                icon = Icons.Default.LockReset,
                greenColor = greenColor,
                showPassword = showConfirmPassword,
                onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                isError = !passwordsMatch && confirmPassword.isNotEmpty()
            )

            // Password Mismatch Error
            if (!passwordsMatch && confirmPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Passwords do not match",
                    color = Color(0xFFEF5350),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Password Button
            Button(
                onClick = {
                    if (passwordsMatch && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                        // ✅ Call resetPassword with just the new password
                        // Email and code are already in the ViewModel!
                        viewModel.resetPassword(newPassword)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenColor,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = passwordsMatch &&
                        newPassword.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        !uiState.isLoading
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isLoading) "Resetting..." else "Reset Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!uiState.isLoading) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Back to login",
                    color = greenColor,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PasswordFieldWithVisibility(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    greenColor: Color,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isError) Color(0xFFEF5350) else greenColor,
            modifier = Modifier.size(24.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            cursorBrush = SolidColor(greenColor),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = greenColor.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (showPassword) "Hide password" else "Show password",
                tint = greenColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}