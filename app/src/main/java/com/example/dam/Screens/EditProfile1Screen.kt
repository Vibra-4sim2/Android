package com.example.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile1Screen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel()

    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    // ✅ Get stored auth data from UserPreferences (SINGLE SOURCE OF TRUTH)
    val token = UserPreferences.getToken(context) ?: ""
    val userId = UserPreferences.getUserId(context) ?: ""

    // Form states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Scroll state
    val scrollState = rememberScrollState()

    // Observe ViewModel states
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        if (userId.isNotEmpty() && token.isNotEmpty()) {
            viewModel.loadUserProfile(userId, token)
        }
    }

    // Populate form fields when user data is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firstName = user.firstName
            lastName = user.lastName
            email = user.email
            gender = user.gender
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            println("Error loading user data: $error")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    } else {
                        listOf(BackgroundLightGradientStart, BackgroundLight, BackgroundLightGradientEnd)
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp) // ✅ Space for top bar
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp)
                .padding(bottom = 120.dp) // ✅ Space for bottom nav bar
                .imePadding() // ✅ Adjusts for keyboard
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Back Button with modern design
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = CardGlass,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = BorderColor,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Edit Profile",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            // --- Form Fields ---
            Label("First Name")
            RoundedInputField(
                value = firstName,
                onValueChange = { firstName = it },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Label("Last Name")
            RoundedInputField(
                value = lastName,
                onValueChange = { lastName = it },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Label("Email address")
            RoundedInputField(
                value = email,
                onValueChange = { email = it },
                hint = "syrine@gmail.com",
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Label("Gender")
            // Gender Field
            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                placeholder = { Text(text = "Select gender", color = TextSecondary) },
                textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = GreenAccent,
                    disabledTextColor = TextSecondary,
                    disabledBorderColor = BorderColor.copy(alpha = 0.5f),
                    focusedContainerColor = CardGlass.copy(alpha = 0.3f),
                    unfocusedContainerColor = CardGlass.copy(alpha = 0.2f)
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Save Button (Arrow) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = GreenAccent.copy(alpha = 0.4f),
                            spotColor = GreenAccent.copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GreenLight, GreenAccent)
                            ),
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = !isLoading
                        ) {
                            // Update user profile with new data
                            viewModel.updateUserProfile(
                                userId = userId,
                                token = token,
                                firstName = firstName,
                                lastName = lastName,
                                gender = gender,
                                email = email,
                                onSuccess = {
                                    navController.navigate("profile") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    println("Update failed: $error")
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Save Changes",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun RoundedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = hint, color = TextSecondary) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = TextPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = GreenAccent,
            unfocusedBorderColor = BorderColor,
            cursorColor = GreenAccent,
            disabledTextColor = TextSecondary,
            disabledBorderColor = BorderColor.copy(alpha = 0.5f),
            focusedContainerColor = CardGlass.copy(alpha = 0.3f),
            unfocusedContainerColor = CardGlass.copy(alpha = 0.2f)
        ),
        enabled = enabled
    )
}