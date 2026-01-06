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
import androidx.compose.material.icons.outlined.AutoAwesome // ← Nouvelle icône magique
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dam.viewmodel.CreateAdventureViewModel
import com.example.dam.viewmodel.FlaskAiViewModel // ← Ajouté
import com.example.dam.utils.JwtHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdventureScreen(
    navController: NavController,
    token: String,
    viewModel: CreateAdventureViewModel = viewModel(),
    flaskAiViewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri.value = uri
    }

    // Écoute propre des StateFlows (sans .value !)
    val aiRoute by flaskAiViewModel.itineraryRoute.collectAsStateWithLifecycle(emptyList())
    val aiLoading by flaskAiViewModel.itineraryLoading.collectAsStateWithLifecycle()
    val itineraryResponse by flaskAiViewModel.itinerary.collectAsStateWithLifecycle()

    val aiDistance = itineraryResponse?.itinerary?.summary?.distance?.let {
        if (it < 1000) "${it.toInt()} m" else "%.1f km".format(it / 1000)
    } ?: "N/A"

    val aiDuration = itineraryResponse?.itinerary?.summary?.duration?.let {
        val minutes = (it / 60).toInt()
        if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}min"
    } ?: "N/A"

    var currentSection by remember { mutableStateOf(0) }
    var infoCompleted by remember { mutableStateOf(false) }
    var itineraryCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.title, viewModel.description, viewModel.date, viewModel.activityType, viewModel.capacity) {
        infoCompleted = viewModel.title.isNotEmpty() &&
                viewModel.description.isNotEmpty() &&
                viewModel.date.isNotEmpty() &&
                viewModel.activityType.isNotEmpty() &&
                viewModel.capacity.isNotEmpty()
    }

    LaunchedEffect(viewModel.startLatLng, viewModel.endLatLng) {
        itineraryCompleted = viewModel.startLatLng != null && viewModel.endLatLng != null
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Header
        Surface(
            color = BackgroundDark,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Box(Modifier.fillMaxWidth()) {
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
                Spacer(Modifier.height(20.dp))
                StepperDots(
                    currentStep = currentSection,
                    totalSteps = if (viewModel.includeCamping) 3 else 2,
                    completedSteps = listOf(infoCompleted, itineraryCompleted, viewModel.includeCamping)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    when (currentSection) {
                        0 -> "Informations"
                        1 -> "Routes Organisation"
                        else -> "Camping Details"
                    },
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp)) {
            Spacer(Modifier.height(16.dp))

            // SECTION 1: Informations
            AnimatedVisibility(currentSection == 0) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
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

                        HorizontalDivider(color = BorderColor)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Picture (optional)", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Choose", color = Color.White)
                            }
                        }
                        photoUri.value?.let {
                            Text("Picture Added", color = GreenLight, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { currentSection = 1 },
                            enabled = infoCompleted,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(GreenAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                        }
                    }
                }
            }

            // SECTION 2: Itinéraire
            AnimatedVisibility(currentSection == 1) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        MapSection(viewModel, flaskAiViewModel, token, aiRoute, aiDistance, aiDuration, aiLoading)

                        CampingToggle(viewModel)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { currentSection = 0 },
                                modifier = Modifier.weight(1f).height(52.dp),
                                border = BorderStroke(1.dp, GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, null, tint = GreenAccent)
                                Spacer(Modifier.width(8.dp))
                                Text("Previous", color = GreenAccent, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (viewModel.includeCamping) currentSection = 2
                                    else {
                                        if (JwtHelper.isTokenExpired(token)) {
                                            viewModel.createResult = Result.Error("Session expired")
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
                                colors = ButtonDefaults.buttonColors(GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (viewModel.includeCamping) "Next" else "Create",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
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

            // SECTION 3: Camping
            AnimatedVisibility(currentSection == 2 && viewModel.includeCamping) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FormField("Name", viewModel.campingName) { viewModel.campingName = it }
                        FormField("Place", viewModel.campingLocation) { viewModel.campingLocation = it }
                        FormField("Price", viewModel.campingPrice, keyboardType = KeyboardType.Number) { viewModel.campingPrice = it }
                        Column {
                            Text("Finish Date", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(Modifier.height(6.dp))
                            DateField(viewModel.campingEnd) { viewModel.campingEnd = it }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { currentSection = 1 },
                                modifier = Modifier.weight(1f).height(52.dp),
                                border = BorderStroke(1.dp, GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, null, tint = GreenAccent)
                                Spacer(Modifier.width(8.dp))
                                Text("Previous", color = GreenAccent, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (JwtHelper.isTokenExpired(token)) { viewModel.createResult = Result.Error("Session expired"); return@Button }
                                    if (viewModel.campingName.isEmpty() || viewModel.campingLocation.isEmpty() || viewModel.campingEnd.isEmpty()) {
                                        viewModel.createResult = Result.Error("Tous les champs camping requis")
                                        return@Button
                                    }
                                    if (viewModel.campingStart.isEmpty()) viewModel.campingStart = viewModel.date
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
                                colors = ButtonDefaults.buttonColors(GreenAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (viewModel.createResult is Result.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            viewModel.createResult?.let { result ->
                Spacer(Modifier.height(16.dp))
                when (result) {
                    is Result.Success -> Card(colors = CardDefaults.cardColors(SuccessGreen.copy(0.2f)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                            Spacer(Modifier.width(12.dp))
                            Text(result.data, color = SuccessGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                    is Result.Error -> Card(colors = CardDefaults.cardColors(ErrorRed.copy(0.2f)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = ErrorRed)
                            Spacer(Modifier.width(12.dp))
                            Text(result.message, color = ErrorRed, fontWeight = FontWeight.Medium)
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
// MAP SECTION AVEC BOUTON IA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MapSection(
    viewModel: CreateAdventureViewModel,
    flaskAiViewModel: FlaskAiViewModel,
    token: String,
    aiRoute: List<com.google.android.gms.maps.model.LatLng>,
    aiDistance: String,
    aiDuration: String,
    aiLoading: Boolean
) {
    var showAddressInput by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Définir le trajet",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSecondary
            )
            IconButton(onClick = { showAddressInput = !showAddressInput }) {
                Icon(
                    if (showAddressInput) Icons.Default.Map else Icons.Default.Edit,
                    null,
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
                    label = { Text("Départ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !viewModel.editingStart,
                    onClick = { viewModel.setEditingPoint(false) },
                    label = { Text("Arrivée") },
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
            onValueChange = { viewModel.startAddress = it },
            label = { Text("Départ") },
            readOnly = !showAddressInput,
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = if (viewModel.startLatLng != null) GreenAccent else TextSecondary
                )
            },
            trailingIcon = {
                if (viewModel.startLatLng != null) {
                    IconButton(onClick = { viewModel.clearStartPoint() }) {
                        Icon(Icons.Default.Clear, null, tint = ErrorRed)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (viewModel.editingStart && !showAddressInput) GreenAccent else BorderColor,
                unfocusedBorderColor = BorderColor
            )
        )

        OutlinedTextField(
            value = viewModel.endAddress,
            onValueChange = { viewModel.endAddress = it },
            label = { Text("Arrivée") },
            readOnly = !showAddressInput,
            leadingIcon = {
                Icon(
                    Icons.Default.Flag,
                    null,
                    tint = if (viewModel.endLatLng != null) ErrorRed else TextSecondary
                )
            },
            trailingIcon = {
                if (viewModel.endLatLng != null) {
                    IconButton(onClick = { viewModel.clearEndPoint() }) {
                        Icon(Icons.Default.Clear, null, tint = ErrorRed)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (!viewModel.editingStart && !showAddressInput) ErrorRed else BorderColor,
                unfocusedBorderColor = BorderColor
            )
        )

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
                // Afficher la route AI si disponible, sinon la route normale
                polylinePoints = if (aiRoute.isNotEmpty()) aiRoute else viewModel.polylinePoints,
                onMapClick = { if (!showAddressInput) viewModel.onMapClick(it) }
            )
        }

        Button(
            onClick = { viewModel.calculateRoute() },
            enabled = viewModel.startLatLng != null && viewModel.endLatLng != null && !viewModel.calculating,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(TealAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.calculating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Calcul en cours...", color = Color.White, fontWeight = FontWeight.Medium)
            } else {
                Text("Calculer l'itinéraire", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }

        // Afficher les infos de la route normale (OpenRouteService)
        if (viewModel.distance != "N/A" && aiRoute.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Badge("Route calculée")
                    Badge(viewModel.distance)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (viewModel.footTime != "N/A") Badge("🚶 ${viewModel.footTime}")
                    if (viewModel.bikeTime != "N/A") Badge("🚴 ${viewModel.bikeTime}")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val start = viewModel.startLatLng ?: return@Button
                val end = viewModel.endLatLng ?: return@Button
                flaskAiViewModel.generateItinerary(
                    token = token,
                    startLat = start.latitude, startLon = start.longitude, startName = viewModel.startAddress,
                    endLat = end.latitude, endLon = end.longitude, endName = viewModel.endAddress,
                    context = "Je préfère les routes ombragées, sécurisées, évitant les grandes avenues et les montées raides. Activité vélo.",
                    activityType = "VELO"
                )
            },
            enabled = viewModel.startLatLng != null && viewModel.endLatLng != null && !aiLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (aiLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Génération IA...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Calculer avec IA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (aiDistance != "N/A") {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Badge("✨ IA Route générée !")
                    Badge(aiDistance)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Badge(aiDuration)
                    Badge("🌳 Ombragée & sécurisée")
                }
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// RESTE DU CODE INCHANGÉ (Stepper, FormField, etc.)
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
    val options = listOf("VELO", "RANDONNEE")
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
private fun Badge(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GreenAccent.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.3f)),
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = text,
            color = GreenAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
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
        Icon(Icons.Outlined.Cottage, null, tint = Color(0xFFFFB74D))
        Spacer(Modifier.width(12.dp))
        Text(
            "Inclure une option camping",
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            fontSize = 15.sp
        )
    }
}