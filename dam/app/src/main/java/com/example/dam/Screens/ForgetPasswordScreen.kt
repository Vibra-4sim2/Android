package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.ui.theme.DamTheme

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }

    val greenColor = Color(0xFF4CAF50)

    Box(modifier = modifier.fillMaxSize()) {

        // Background Top (même design que login)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.download),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )
            )
        }

        // Background Bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Black)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Header (même design que login)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = greenColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "V!BRA", // Changé pour correspondre à votre app
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icône de réinitialisation
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEmailSent)
                        "Check your email for reset instructions"
                    else
                        "Enter your email to reset your password",
                    color = Color.White.copy(0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEmailSent) {
                // Card (même design que login)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.Black.copy(0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        Text("Email", color = Color.White.copy(0.7f), fontSize = 12.sp)

                        // Utilisez le même CompactTextField que dans login/register
                        CompactTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "your@email.com",
                            icon = Icons.Default.Email,
                            greenColor = greenColor,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    isEmailSent = true
                                    // TODO: Implémenter l'envoi d'email
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(greenColor),
                            shape = RoundedCornerShape(10.dp),
                            enabled = email.isNotEmpty()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.Send, contentDescription = null)
                            }
                        }
                    }
                }
            } else {
                // Success Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.Black.copy(0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = greenColor,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Email Sent!",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "We've sent a password reset link to:",
                            color = Color.White.copy(0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = email,
                            color = greenColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Resend Button
                        TextButton(
                            onClick = {
                                isEmailSent = false
                                // TODO: Implémenter le renvoi d'email
                            }
                        ) {
                            Text("Didn't receive the email? Resend", color = greenColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back to Login
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Back to login",
                    color = greenColor,
                    fontSize = 13.sp,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordPreview() {
    DamTheme {
        val navController = rememberNavController()
        ForgotPasswordScreen(navController = navController)
    }
}