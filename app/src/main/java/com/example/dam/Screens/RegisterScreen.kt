package com.example.dam.Screens

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.R
import com.example.dam.viewmodel.RegisterViewModel
import java.util.Locale

@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // ViewModel
    val viewModel: RegisterViewModel = viewModel()

    // States
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var Gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    // Observer l'état du ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Navigation en cas de succès
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("ActivitiesSelectionScreen")
        }
    }

    // Colors
    val greenColor = Color(0xFF4CAF50)
    val backgroundColor = Color.Black.copy(alpha = 0.55f)

    // AlertDialog pour les erreurs
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Text(
                    text = "❌ Registration Failed",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(uiState.error ?: "An error occurred")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = greenColor)
                }
            },
            containerColor = Color.Black.copy(alpha = 0.95f),
            titleContentColor = Color.White,
            textContentColor = Color.White
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

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Cycl-E",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Create your account",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Join the cycling community!",
                    color = greenColor.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // First Name Field
            RegisterTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "First Name",
                icon = Icons.Default.Person,
                greenColor = greenColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name Field
            RegisterTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "Last Name",
                icon = Icons.Default.Person,
                greenColor = greenColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Dropdown
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { genderExpanded = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = greenColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = if (Gender.isEmpty()) "Select Gender" else Gender.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        color = if (Gender.isEmpty())
                            greenColor.copy(alpha = 0.7f)
                        else
                            Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = greenColor
                    )
                }

                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color.Black.copy(alpha = 0.95f))
                ) {
                    listOf("MALE", "FEMALE").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    },
                                    color = Color.White
                                )
                            },
                            onClick = {
                                Gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            RegisterTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                icon = Icons.Default.Email,
                greenColor = greenColor,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            RegisterTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                icon = Icons.Default.Lock,
                greenColor = greenColor,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            RegisterTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm password",
                icon = Icons.Default.LockReset,
                greenColor = greenColor,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms & Privacy Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreeTerms = !agreeTerms }
            ) {
                Icon(
                    imageVector = if (agreeTerms) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = if (agreeTerms) greenColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I agree to the Terms & Privacy Policy",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = {

                    // LOGS DE DEBUG
                    Log.d("RegisterScreen", "=== REGISTRATION ATTEMPT ===")
                    Log.d("RegisterScreen", "firstName: '$firstName'")
                    Log.d("RegisterScreen", "lastName: '$lastName'")
                    Log.d("RegisterScreen", "gender: '$Gender'")
                    Log.d("RegisterScreen", "email: '$email'")
                    Log.d("RegisterScreen", "password: '$password'")
                    Log.d("RegisterScreen", "confirmPassword: '$confirmPassword'")



                    Log.d("RegisterScreen", "=== BUTTON CLICKED ===")
                    Log.d("RegisterScreen", "gender: '$Gender'")
                    Log.d("RegisterScreen", "gender.isEmpty(): ${Gender.isEmpty()}")

                    if (Gender.isEmpty()) {
                        // Afficher une erreur
                        Log.e("RegisterScreen", "GENDER IS EMPTY!")
                    }

                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        Gender = Gender,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agreeTerms && !uiState.isLoading) greenColor else Color.Gray,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = agreeTerms && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sign up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Already Have Account Link
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Already have an account?",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Log in",
                    color = greenColor,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    greenColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = greenColor,
            modifier = Modifier.size(24.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = greenColor.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }

            if (isPassword) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = greenColor
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
                )
            } else {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = greenColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
                )
            }
        }
    }
}