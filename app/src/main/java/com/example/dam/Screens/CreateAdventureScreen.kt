// screens/CreateAdventureScreen.kt
package com.example.dam.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        Text("Créer une Sortie", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Planifiez votre prochaine aventure", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

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
                FormField("Titre", viewModel.title) { viewModel.title = it }
                FormField("Description", viewModel.description, minLines = 3) { viewModel.description = it }
                DateField(viewModel.date) { viewModel.date = it }
                ActivityTypeDropdown(viewModel.activityType) { viewModel.activityType = it }
                FormField("Capacité", viewModel.capacity, keyboardType = KeyboardType.Number) { viewModel.capacity = it }

                MapSection(viewModel)
                CampingToggle(viewModel)

                // PHOTO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Photo (optionnel)", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Choisir", color = Color.White)
                    }
                }
                photoUri.value?.let {
                    Text("Photo sélectionnée", color = GreenLight, fontSize = 12.sp)
                }

                Spacer(Modifier.height(12.dp))

                // BOUTON CRÉER
                Button(
                    onClick = {
                        // ✅ Vérifier le token
                        if (JwtHelper.isTokenExpired(token)) {
                            viewModel.createResult = Result.Error("⚠️ Session expirée. Veuillez vous reconnecter.")
                            return@Button
                        }

                        // ✅ Validation du camping si activé
                        if (viewModel.includeCamping) {
                            when {
                                viewModel.campingName.isEmpty() -> {
                                    viewModel.createResult = Result.Error("⚠️ Le nom du camping est requis")
                                    return@Button
                                }
                                viewModel.campingLocation.isEmpty() -> {
                                    viewModel.createResult = Result.Error("⚠️ Le lieu du camping est requis")
                                    return@Button
                                }
                                viewModel.campingStart.isEmpty() || viewModel.campingEnd.isEmpty() -> {
                                    viewModel.createResult = Result.Error("⚠️ Les dates de camping sont requises")
                                    return@Button
                                }
                            }
                        }
                        val file = photoUri.value?.let { uri ->
                            val input = context.contentResolver.openInputStream(uri)!!
                            val temp = File.createTempFile("sortie_", ".jpg", context.cacheDir)
                            input.copyTo(temp.outputStream())
                            temp
                        }
                        Log.d("CREATE_SORTIE", "Token: ${UserPreferences.getToken(context)}")
                        viewModel.createAdventure(token, file)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = viewModel.startLatLng != null && viewModel.endLatLng != null,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    if (viewModel.createResult is Result.Loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Créer la Sortie", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // MESSAGE RÉSULTAT
                viewModel.createResult?.let { result ->
                    when (result) {
                        is Result.Success -> Text(result.data, color = SuccessGreen)
                        is Result.Error -> Text(result.message, color = ErrorRed)
                        else -> Unit
                    }
                }
            }
        }
        Spacer(Modifier.height(120.dp))
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
                cursorColor = GreenAccent
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun DateField(value: String, onChange: (String) -> Unit) {
    val context = LocalContext.current

    // ✅ Format ISO 8601 avec UTC
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val displayFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Date et heure", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)

        OutlinedTextField(
            value = if (value.isEmpty()) "Choisir une date..." else {
                try {
                    displayFormatter.format(isoFormatter.parse(value)!!)
                } catch (e: Exception) {
                    "Date invalide"
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
                    // ✅ CORRECTION: Appeler directement le DatePicker au clic
                    showDateTimePicker(context, value, isoFormatter, onChange)
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = BorderColor,
                disabledTextColor = TextPrimary,
                disabledBorderColor = BorderColor
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = false  // ✅ Pour forcer l'utilisation du clickable
        )
    }
}

// ✅ NOUVELLE FONCTION: Afficher le DatePicker
private fun showDateTimePicker(
    context: android.content.Context,
    currentValue: String,
    formatter: SimpleDateFormat,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    // Parser la date actuelle si elle existe
    if (currentValue.isNotEmpty()) {
        try {
            calendar.time = formatter.parse(currentValue)!!
        } catch (e: Exception) {
            Log.e("DatePicker", "Parse error: ${e.message}")
        }
    }

    // ✅ DatePicker d'abord
    val datePicker = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            // ✅ Ensuite TimePicker
            val timePicker = android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val utcDate = formatter.format(calendar.time)
                    Log.d("DatePicker", "Date sélectionnée (UTC): $utcDate")
                    onDateSelected(utcDate)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true  // Format 24h
            )
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // ✅ Date minimum = aujourd'hui
    datePicker.datePicker.minDate = System.currentTimeMillis()
    datePicker.show()
}@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTypeDropdown(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("VELO", "RANDONNEE", "CAMPING", "ESCALADE", "KAYAK", "COURSE", "AUTRE")
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
                    unfocusedBorderColor = BorderColor
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
    var mapLoaded by remember { mutableStateOf(true) }  // ✅ CORRECTION: true par défaut

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = GreenAccent)
                Spacer(Modifier.width(8.dp))
                Text("Planifier l'itinéraire", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            }

            IconButton(onClick = { showAddressInput = !showAddressInput }) {
                Icon(
                    if (showAddressInput) Icons.Default.Map else Icons.Default.Edit,
                    contentDescription = if (showAddressInput) "Mode carte" else "Mode saisie",
                    tint = GreenAccent
                )
            }
        }

        // ✅ MODE SÉLECTION: Boutons radio pour choisir départ ou arrivée
        if (!showAddressInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bouton DÉPART
                FilterChip(
                    selected = viewModel.editingStart,
                    onClick = { viewModel.setEditingPoint(true) },
                    label = { Text("📍 Placer le départ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Bouton ARRIVÉE
                FilterChip(
                    selected = !viewModel.editingStart,
                    onClick = { viewModel.setEditingPoint(false) },
                    label = { Text("🏁 Placer l'arrivée") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorRed,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CHAMPS DÉPART
        OutlinedTextField(
            value = viewModel.startAddress,
            onValueChange = {
                viewModel.startAddress = it
                if (showAddressInput && it.length > 3) {
                    viewModel.searchAddress(it, true)
                }
            },
            label = { Text("Départ") },
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
                unfocusedBorderColor = BorderColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // CHAMPS ARRIVÉE
        OutlinedTextField(
            value = viewModel.endAddress,
            onValueChange = {
                viewModel.endAddress = it
                if (showAddressInput && it.length > 3) {
                    viewModel.searchAddress(it, false)
                }
            },
            label = { Text("Arrivée") },
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
                unfocusedBorderColor = BorderColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // INDICATEUR VISUEL
        if (!showAddressInput) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.editingStart) GreenAccent.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (viewModel.editingStart)
                        "👆 Cliquez sur la carte pour placer le DÉPART (vert)"
                    else
                        "👆 Cliquez sur la carte pour placer l'ARRIVÉE (rouge)",
                    fontSize = 13.sp,
                    color = if (viewModel.editingStart) GreenDark else ErrorRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // CARTE
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
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
        ) {
            if (viewModel.calculating) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text("Calculer l'itinéraire", color = Color.White)
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
}@Composable
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
        Icon(Icons.Outlined.Cottage, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(12.dp))
        Text("Inclure une option camping", fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 15.sp)
    }

    AnimatedVisibility(viewModel.includeCamping) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardGlass),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = TealAccent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Détails du camping", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                }

                FormField("Nom du camping", viewModel.campingName) { viewModel.campingName = it }
                FormField("Lieu", viewModel.campingLocation) { viewModel.campingLocation = it }
                FormField("Prix (€)", viewModel.campingPrice, keyboardType = KeyboardType.Number) { viewModel.campingPrice = it }

                // LES DEUX DATES SONT MAINTENANT PARFAITES
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Date début", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        DateField(
                            value = viewModel.campingStart,
                            onChange = { viewModel.campingStart = it }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Date fin", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        DateField(
                            value = viewModel.campingEnd,
                            onChange = { viewModel.campingEnd = it }
                        )
                    }
                }
}}}}
//private fun CampingToggle(viewModel: CreateAdventureViewModel) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { viewModel.includeCamping = !viewModel.includeCamping }
//            .padding(vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Checkbox(
//            checked = viewModel.includeCamping,
//            onCheckedChange = { viewModel.includeCamping = it },
//            colors = CheckboxDefaults.colors(checkedColor = GreenAccent)
//        )
//        Spacer(Modifier.width(8.dp))
//        Icon(Icons.Outlined.Cottage, contentDescription = null, tint = Color.White)
//        Spacer(Modifier.width(8.dp))
//        Text("Inclure une option camping", fontWeight = FontWeight.Medium, color = TextPrimary)
//    }
//
//    AnimatedVisibility(viewModel.includeCamping) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            colors = CardDefaults.cardColors(containerColor = CardGlass),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Default.Home, contentDescription = null, tint = TealAccent)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Détails du camping", fontWeight = FontWeight.Bold, color = TextPrimary)
//                }
//
//                FormField(
//                    label = "Nom du camping",
//                    value = viewModel.campingName,
//                    modifier = Modifier.fillMaxWidth()
//                ) { viewModel.campingName = it }
//
//                FormField(
//                    label = "Lieu",
//                    value = viewModel.campingLocation,
//                    modifier = Modifier.fillMaxWidth()
//                ) { viewModel.campingLocation = it }
//
//                FormField(
//                    label = "Prix (€)",
//                    value = viewModel.campingPrice,
//                    keyboardType = KeyboardType.Number,
//                    modifier = Modifier.fillMaxWidth()
//                ) { viewModel.campingPrice = it }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    FormField(
//                        label = "Date début",
//                        value = viewModel.campingStart,
//                        modifier = Modifier.weight(1f)
//                    ) { viewModel.campingStart = it }
//
//                    FormField(
//                        label = "Date fin",
//                        value = viewModel.campingEnd,
//                        modifier = Modifier.weight(1f)
//                    ) { viewModel.campingEnd = it }
//                }
//            }
//        }
//    }
//
//}