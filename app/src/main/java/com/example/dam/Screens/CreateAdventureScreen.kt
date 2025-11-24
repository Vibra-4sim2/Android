// screens/CreateAdventureScreen.kt
package com.example.dam.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.ui.theme.*
import com.example.dam.utils.Result
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.CreateAdventureViewModel
import com.example.dam.Screens.MapWithRoute
import com.example.dam.utils.JwtHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdventureScreen(
    navController: NavController,
    token: String,
    viewModel: CreateAdventureViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri.value = uri }

    // États des sections
    var currentSection by remember { mutableStateOf(0) } // 0=Info, 1=Itinéraire, 2=Camping
    var infoCompleted by remember { mutableStateOf(false) }
    var itineraryCompleted by remember { mutableStateOf(false) }

    // Vérifier si la section Info est complète
    LaunchedEffect(viewModel.title, viewModel.description, viewModel.date, viewModel.activityType, viewModel.capacity) {
        infoCompleted = viewModel.title.isNotEmpty() &&
                viewModel.description.isNotEmpty() &&
                viewModel.date.isNotEmpty() &&
                viewModel.activityType.isNotEmpty() &&
                viewModel.capacity.isNotEmpty()
    }

    // Vérifier si l'itinéraire est complet
    LaunchedEffect(viewModel.startLatLng, viewModel.endLatLng) {
        itineraryCompleted = viewModel.startLatLng != null && viewModel.endLatLng != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // En-tête avec stepper
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(30.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.Close, "Fermer", tint = Color.White)
                    }
                    Text(
                        "New Adventure",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Beautiful Stepper avec dots
                StepperDots(
                    currentStep = currentSection,
                    totalSteps = if (viewModel.includeCamping) 3 else 2,
                    completedSteps = listOf(
                        infoCompleted,
                        itineraryCompleted,
                        viewModel.includeCamping
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Label de la section actuelle
                Text(
                    text = when (currentSection) {
                        0 -> "Informations"
                        1 -> "Routes Organisation"
                        else -> "Camping Details"
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // SECTION 1: INFORMATIONS
            AnimatedVisibility(currentSection == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FormField("Title", viewModel.title) { viewModel.title = it }
                        FormField("Description", viewModel.description, minLines = 3) { viewModel.description = it }
                        DateField(viewModel.date) { viewModel.date = it }
                        ActivityTypeDropdown(viewModel.activityType) { viewModel.activityType = it }
                        FormField("Capacity", viewModel.capacity, keyboardType = KeyboardType.Number) { viewModel.capacity = it }

                        // PHOTO
                        Divider(color = BorderColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Picture (optional)", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                            ) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Choose", color = Color.White)
                            }
                        }
                        photoUri.value?.let {
                            Text("✓ Picture Added", color = GreenLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Bouton suivant
                        Button(
                            onClick = { currentSection = 1 },
                            enabled = infoCompleted,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                        }
                    }
                }
            }

            // SECTION 2: ITINÉRAIRE
            AnimatedVisibility(currentSection == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        MapSection(viewModel)

                        // Toggle Camping
                        CampingToggle(viewModel)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentSection = 0 },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GreenAccent)
                            ) {
                                Icon(Icons.Default.ArrowBack, null, tint = GreenAccent)
                                Spacer(Modifier.width(8.dp))
                                Text("Previous", color = GreenAccent, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (viewModel.includeCamping) {
                                        currentSection = 2
                                    } else {
                                        // Créer directement si pas de camping
                                        if (JwtHelper.isTokenExpired(token)) {
                                            viewModel.createResult = Result.Error("⚠️ Session expired")
                                            return@Button
                                        }
                                        val file = photoUri.value?.let { uri ->
                                            val input = context.contentResolver.openInputStream(uri)!!
                                            val temp = File.createTempFile("sortie_", ".jpg", context.cacheDir)
                                            input.copyTo(temp.outputStream())
                                            temp
                                        }
                                        viewModel.createAdventure(token, file)
                                    }
                                },
                                enabled = itineraryCompleted,
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (viewModel.includeCamping) "Next" else "Create",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    if (viewModel.includeCamping) Icons.Default.ArrowForward else Icons.Default.Check,
                                    null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: CAMPING (conditionnelle)
            AnimatedVisibility(currentSection == 2 && viewModel.includeCamping) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FormField("Name", viewModel.campingName) { viewModel.campingName = it }
                        FormField("Place", viewModel.campingLocation) { viewModel.campingLocation = it }
                        FormField("Price", viewModel.campingPrice, keyboardType = KeyboardType.Number) { viewModel.campingPrice = it }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Finish Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Spacer(Modifier.height(6.dp))
                            DateField(
                                value = viewModel.campingEnd,
                                onChange = { viewModel.campingEnd = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentSection = 1 },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GreenAccent)
                            ) {
                                Icon(Icons.Default.ArrowBack, null, tint = GreenAccent)
                                Spacer(Modifier.width(8.dp))
                                Text("Previous", color = GreenAccent, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (JwtHelper.isTokenExpired(token)) {
                                        viewModel.createResult = Result.Error("⚠️ Session expired")
                                        return@Button
                                    }
                                    if (viewModel.campingName.isEmpty() || viewModel.campingLocation.isEmpty() || viewModel.campingEnd.isEmpty()) {
                                        viewModel.createResult = Result.Error("⚠️ All camping fields are required")
                                        return@Button
                                    }
                                    // ✅ AUTO: campingStart = date de la sortie
                                    if (viewModel.campingStart.isEmpty()) {
                                        viewModel.campingStart = viewModel.date
                                    }
                                    val file = photoUri.value?.let { uri ->
                                        val input = context.contentResolver.openInputStream(uri)!!
                                        val temp = File.createTempFile("sortie_", ".jpg", context.cacheDir)
                                        input.copyTo(temp.outputStream())
                                        temp
                                    }
                                    viewModel.createAdventure(token, file)
                                },
                                enabled = viewModel.createResult !is Result.Loading,
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (viewModel.createResult is Result.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Create", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // MESSAGE RÉSULTAT
            viewModel.createResult?.let { result ->
                Spacer(Modifier.height(16.dp))
                when (result) {
                    is Result.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                                Spacer(Modifier.width(12.dp))
                                Text(result.data, color = SuccessGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    is Result.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = ErrorRed)
                                Spacer(Modifier.width(12.dp))
                                Text(result.message, color = ErrorRed, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    else -> Unit
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BEAUTIFUL STEPPER AVEC DOTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepperDots(
    currentStep: Int,
    totalSteps: Int,
    completedSteps: List<Boolean>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < completedSteps.size && completedSteps[index]
            val isCurrent = index == currentStep

            // Dot animé
            val dotSize by animateDpAsState(
                targetValue = if (isCurrent) 12.dp else 8.dp,
                label = "dotSize"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = when {
                            isCompleted -> GreenAccent
                            isCurrent -> Color.White
                            else -> Color.White.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    )
            )

            // Ligne de connexion
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(
                            color = if (isCompleted) GreenAccent else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSANTS RÉUTILISABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = modifier.fillMaxWidth(),
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = BorderColor,
                cursorColor = GreenAccent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun DateField(value: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val displayFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    OutlinedTextField(
        value = if (value.isEmpty()) "Choose date..." else {
            try {
                displayFormatter.format(isoFormatter.parse(value)!!)
            } catch (e: Exception) {
                "Invalid date"
            }
        },
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.CalendarToday, null, tint = GreenAccent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDateTimePicker(context, value, isoFormatter, onChange)
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenAccent,
            unfocusedBorderColor = BorderColor,
            disabledTextColor = Color.White,
            disabledBorderColor = BorderColor
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = false
    )
}

private fun showDateTimePicker(
    context: android.content.Context,
    currentValue: String,
    formatter: SimpleDateFormat,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    if (currentValue.isNotEmpty()) {
        try {
            calendar.time = formatter.parse(currentValue)!!
        } catch (e: Exception) {
            Log.e("DatePicker", "Parse error: ${e.message}")
        }
    }

    val datePicker = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            val timePicker = android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val utcDate = formatter.format(calendar.time)
                    onDateSelected(utcDate)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePicker.datePicker.minDate = System.currentTimeMillis()
    datePicker.show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTypeDropdown(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("VELO 🚴‍♂️", "RANDONNEE ⛰️")
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Type d'activité", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected.ifEmpty { "Choisir..." },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = { onSelect(type); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapSection(viewModel: CreateAdventureViewModel) {
    var showAddressInput by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Définir le trajet", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextSecondary)

            IconButton(onClick = { showAddressInput = !showAddressInput }) {
                Icon(
                    if (showAddressInput) Icons.Default.Map else Icons.Default.Edit,
                    contentDescription = if (showAddressInput) "Mode carte" else "Mode saisie",
                    tint = GreenAccent
                )
            }
        }

        if (!showAddressInput) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = viewModel.editingStart,
                    onClick = { viewModel.setEditingPoint(true) },
                    label = { Text("📍 Départ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !viewModel.editingStart,
                    onClick = { viewModel.setEditingPoint(false) },
                    label = { Text("🏁 Arrivée") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorRed,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = viewModel.startAddress,
            onValueChange = {
                viewModel.startAddress = it
                if (showAddressInput && it.length > 3) {
                    viewModel.searchAddress(it, true)
                }
            },
            label = { Text("Départ", color = TextSecondary) },
            readOnly = !showAddressInput,
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (viewModel.startLatLng != null) GreenAccent else TextSecondary
                )
            },
            trailingIcon = {
                if (viewModel.startLatLng != null) {
                    IconButton(onClick = { viewModel.clearStartPoint() }) {
                        Icon(Icons.Default.Clear, "Effacer", tint = ErrorRed)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (viewModel.editingStart && !showAddressInput) GreenAccent else BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = viewModel.endAddress,
            onValueChange = {
                viewModel.endAddress = it
                if (showAddressInput && it.length > 3) {
                    viewModel.searchAddress(it, false)
                }
            },
            label = { Text("Arrivée", color = TextSecondary) },
            readOnly = !showAddressInput,
            leadingIcon = {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = if (viewModel.endLatLng != null) ErrorRed else TextSecondary
                )
            },
            trailingIcon = {
                if (viewModel.endLatLng != null) {
                    IconButton(onClick = { viewModel.clearEndPoint() }) {
                        Icon(Icons.Default.Clear, "Effacer", tint = ErrorRed)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (!viewModel.editingStart && !showAddressInput) ErrorRed else BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (!showAddressInput) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.editingStart) GreenAccent.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (viewModel.editingStart)
                        "👆 Cliquez sur la carte pour placer le DÉPART"
                    else
                        "👆 Cliquez sur la carte pour placer l'ARRIVÉE",
                    fontSize = 13.sp,
                    color = if (viewModel.editingStart) GreenDark else ErrorRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(2.dp, BorderColor, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            MapWithRoute(
                start = viewModel.startLatLng,
                end = viewModel.endLatLng,
                polylinePoints = viewModel.polylinePoints,
                onMapClick = {
                    if (!showAddressInput) {
                        viewModel.onMapClick(it)
                    }
                }
            )
        }

        Button(
            onClick = { viewModel.calculateRoute() },
            enabled = viewModel.startLatLng != null && viewModel.endLatLng != null && !viewModel.calculating,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.calculating) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text("Calculer l'itinéraire", color = Color.White, fontWeight = FontWeight.Medium)
        }

        if (viewModel.distance.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Badge("📏 ${viewModel.distance}")
                Badge("🚶 ${viewModel.footTime}")
                Badge("🚴 ${viewModel.bikeTime}")
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GreenAccent.copy(alpha = 0.2f),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = text,
            color = GreenDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CampingToggle(viewModel: CreateAdventureViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.includeCamping = !viewModel.includeCamping }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = viewModel.includeCamping,
            onCheckedChange = { viewModel.includeCamping = it },
            colors = CheckboxDefaults.colors(checkedColor = GreenAccent)
        )
        Spacer(Modifier.width(12.dp))
        Icon(Icons.Outlined.Cottage, contentDescription = null, tint = Color(0xFFFFB74D))
        Spacer(Modifier.width(12.dp))
        Text("Inclure une option camping", fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 15.sp)
    }
}