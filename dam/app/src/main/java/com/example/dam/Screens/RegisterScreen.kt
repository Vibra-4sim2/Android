package com.example.dam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.ui.theme.DamTheme

@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // States
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val greenColor = Color(0xFF4CAF50)

    Box(modifier = modifier.fillMaxSize()) {

        // Top Half Background Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(R.drawable.download),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                        )
                    )
            )
        }

        // Bottom Half Solid Black
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Color.Black)
        )

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header Title & Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "V!BRA", // Changed to match your app name
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Create Account",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Join the adventuring community!",
                color = Color.White.copy(0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // CARD (fields)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Full Name",
                        icon = Icons.Default.Person,
                        greenColor = greenColor
                    )

                    CompactTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        icon = Icons.Default.Email,
                        greenColor = greenColor,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    CompactTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        icon = Icons.Default.Lock,
                        greenColor = greenColor,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
                    )

                    CompactTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        icon = Icons.Default.LockReset,
                        greenColor = greenColor,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                    )

                    Button(
                        onClick = {
                            // Add registration logic here
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = greenColor)
                    ) {
                        Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Already have account?
            Row {
                Text("Already have an account?", color = Color.White.copy(0.7f), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log in",
                    color = greenColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    greenColor: Color,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = greenColor, modifier = Modifier.size(24.dp))

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = greenColor,
                focusedLabelColor = greenColor,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
            placeholder = {
                Text(
                    placeholder,
                    color = greenColor.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            },
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = greenColor
                        )
                    }
                }
            },
            singleLine = true
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    DamTheme {
        RegisterScreen(navController = rememberNavController())
    }
}