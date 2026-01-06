package com.example.dam.Screens

import android.util.Log
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.R
import com.example.dam.viewmodel.RegisterViewModel
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.LoginViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)  // ✅ ADD THIS LINE
@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ViewModel

    // Observer l'état du ViewModel
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()  // ✅ AJOUTER



    // States
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var Gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var birthDateDisplay by remember { mutableStateOf("") }  // ✅ For display only
    var showDatePicker by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }


    // ✅ Observer les états (CORRIGER ICI)
    val loginUiState by loginViewModel.uiState.collectAsState()
    val registerUiState by registerViewModel.uiState.collectAsState()


    // ✅ 1. Après inscription réussie → Login automatique
    LaunchedEffect(registerUiState.isSuccess) {
        if (registerUiState.isSuccess) {
            Log.d("RegisterScreen", "✅ Register réussi, login automatique...")
            loginViewModel.login(email, password)
        }
    }
    // ✅ 2. Observer le login après register → Sauvegarder et naviguer
    LaunchedEffect(loginUiState.isSuccess) {
        if (loginUiState.isSuccess) {
            val token = loginViewModel.getAccessToken()
            if (token.isNotEmpty()) {
                Log.d("RegisterScreen", "✅ Login réussi après register")
                Log.d("RegisterScreen", "🔑 Token: ${token.take(30)}...")

                // Sauvegarder le token (décode automatiquement le userId)
                UserPreferences.saveToken(context, token)

                Log.d("RegisterScreen", "📍 Navigation vers preferences...")

                // Naviguer vers preferences
                navController.navigate("preferences") {
                    popUpTo("register") { inclusive = true }
                }
            }
        }
    }

    // ✅ AlertDialog pour les erreurs (REGISTER)
    if (registerUiState.error != null) {
        AlertDialog(
            onDismissRequest = { registerViewModel.clearError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registration Failed", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(registerUiState.error ?: "An error occurred") },
            confirmButton = {
                TextButton(onClick = { registerViewModel.clearError() }) {
                    Text("OK", color = GreenAccent)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ✅ AlertDialog pour les erreurs (LOGIN après register)
    if (loginUiState.error != null) {
        AlertDialog(
            onDismissRequest = { loginViewModel.clearError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Login Failed", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Registration successful but auto-login failed. Please login manually.") },
            confirmButton = {
                TextButton(onClick = {
                    loginViewModel.clearError()
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }) {
                    Text("Go to Login", color = GreenAccent)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }


    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert to ISO 8601 format for backend
                            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                            isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            birthDate = isoFormat.format(java.util.Date(millis))

                            // Format for display (DD/MM/YYYY)
                            val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                            birthDateDisplay = displayFormat.format(java.util.Date(millis))

                            Log.d("RegisterScreen", "✅ Selected birthDate (ISO 8601): $birthDate")
                            Log.d("RegisterScreen", "✅ Display format: $birthDateDisplay")
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = GreenAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CardDark
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = CardDark,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = GreenAccent,
                    selectedYearContentColor = BackgroundDark,
                    selectedYearContainerColor = GreenAccent,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = BackgroundDark,
                    selectedDayContainerColor = GreenAccent,
                    todayContentColor = GreenAccent,
                    todayDateBorderColor = GreenAccent
                )
            )
        }
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

        // Background Image (Same as Login)
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

        // Main Content
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
                    Image(
                        painter = painterResource(id = R.drawable.vibra),
                        contentDescription = "Bike icon",
                        modifier = Modifier.size(60.dp),
                        colorFilter = ColorFilter.tint(GreenAccent)
                    )
                }

                Text("V!BRA", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("Create Account", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("Join the cycling community today", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
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

                        // First Name
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("First Name", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            RegisterGlassTextField(firstName, { firstName = it }, "John", Icons.Default.Person)
                        }

                        // Last Name
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Last Name", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            RegisterGlassTextField(lastName, { lastName = it }, "Doe", Icons.Default.Person)
                        }

                        // Gender Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Gender", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = CardGlass,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { genderExpanded = true }
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Wc,
                                            contentDescription = null,
                                            tint = GreenAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (Gender.isEmpty()) "Select your gender" else Gender.replaceFirstChar { char ->
                                                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                                            },
                                            color = if (Gender.isEmpty()) TextSecondary else TextPrimary,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = genderExpanded,
                                    onDismissRequest = { genderExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.88f)
                                        .background(CardDark, RoundedCornerShape(16.dp))
                                ) {
                                    listOf("MALE", "FEMALE").forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = option.replaceFirstChar { char ->
                                                        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                                                    },
                                                    color = TextPrimary,
                                                    fontSize = 16.sp
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
                        }

                        // Birth Date
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Birth Date", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CardGlass,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.clickable { showDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (birthDateDisplay.isEmpty()) "DD/MM/YYYY" else birthDateDisplay,
                                        color = if (birthDateDisplay.isEmpty()) TextSecondary else TextPrimary,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Email
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Email", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            RegisterGlassTextField(email, { email = it }, "your@email.com", Icons.Default.Email, KeyboardType.Email)
                        }

                        // Password
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Password", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            RegisterGlassTextField(password, { password = it }, "••••••••", Icons.Default.Lock, KeyboardType.Password, true)
                        }

                        // Confirm Password
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Confirm Password", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            RegisterGlassTextField(confirmPassword, { confirmPassword = it }, "••••••••", Icons.Default.LockReset, KeyboardType.Password, true)
                        }

                        // Terms & Privacy Checkbox
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (agreeTerms) GreenAccent.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.clickable { agreeTerms = !agreeTerms }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (agreeTerms) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (agreeTerms) GreenAccent else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "I agree to Terms & Privacy Policy",
                                    color = if (agreeTerms) GreenAccent else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Sign Up Button
                        Button(
                            onClick = {
                                // LOGS DE DEBUG
                                Log.d("RegisterScreen", "=== REGISTRATION ATTEMPT ===")
                                Log.d("RegisterScreen", "firstName: '$firstName'")
                                Log.d("RegisterScreen", "lastName: '$lastName'")
                                Log.d("RegisterScreen", "gender: '$Gender'")
                                Log.d("RegisterScreen", "birthDate: '$birthDate'")  // ✅ NEW LOG
                                Log.d("RegisterScreen", "email: '$email'")
                                Log.d("RegisterScreen", "password: '$password'")
                                Log.d("RegisterScreen", "confirmPassword: '$confirmPassword'")

                                if (Gender.isEmpty()) {
                                    Log.e("RegisterScreen", "GENDER IS EMPTY!")
                                }

                                if (birthDate.isEmpty()) {
                                    Log.e("RegisterScreen", "BIRTH DATE IS EMPTY!")
                                }

                                registerViewModel.register(  // ✅ CHANGEZ viewModel en registerViewModel
                                    firstName = firstName,
                                    lastName = lastName,
                                    Gender = Gender,
                                    birthDate = birthDate,  // ✅ NEW PARAMETER
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape = RoundedCornerShape(16.dp),
                            enabled = agreeTerms && !registerUiState.isLoading  // ✅ CHANGEZ uiState en registerUiState
                        ) {
                            if (registerUiState.isLoading) {  // ✅ CHANGEZ uiState en registerUiState
                                CircularProgressIndicator(
                                    color = BackgroundDark,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Already Have Account
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "Log in",
                    color = GreenAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RegisterGlassTextField(
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
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                modifier = Modifier.weight(1f),
                cursorBrush = SolidColor(GreenAccent),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(placeholder, color = TextSecondary, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}