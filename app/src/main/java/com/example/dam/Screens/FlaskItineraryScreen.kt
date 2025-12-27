package com.example.dam.Screens


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dam.models.FlaskLocationPoint
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.FlaskAiViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

// ============================================================================
// FLASK AI ITINERARY GENERATION SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlaskItineraryScreen(
    navController: NavController,
    viewModel: FlaskAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val itinerary by viewModel.itinerary.collectAsState()
    val route by viewModel.itineraryRoute.collectAsState()
    val isLoading by viewModel.itineraryLoading.collectAsState()
    val error by viewModel.itineraryError.collectAsState()

    // Form state
    var startLat by remember { mutableStateOf("36.8065") }
    var startLon by remember { mutableStateOf("10.1815") }
    var startName by remember { mutableStateOf("Tunis") }

    var endLat by remember { mutableStateOf("36.8189") }
    var endLon by remember { mutableStateOf("10.1658") }
    var endName by remember { mutableStateOf("La Marsa") }

    var userContext by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("") }

    var showMap by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                )
            )
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Spacer(Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "🗺️ AI Itinerary",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Generate personalized routes",
                            fontSize = 13.sp,
                            color = GreenAccent
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardGlass,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF4A90E2).copy(0.2f), Color(0xFF357ABD).copy(0.2f))
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Gemini AI + OpenRouteService",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                "Personalized based on your preferences",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Start Location
            item {
                Text(
                    "📍 Start Location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startLat,
                        onValueChange = { startLat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            focusedLabelColor = GreenAccent,
                            cursorColor = GreenAccent
                        )
                    )
                    OutlinedTextField(
                        value = startLon,
                        onValueChange = { startLon = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            focusedLabelColor = GreenAccent,
                            cursorColor = GreenAccent
                        )
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = startName,
                    onValueChange = { startName = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        focusedLabelColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            }

            // End Location
            item {
                Text(
                    "🏁 End Location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endLat,
                        onValueChange = { endLat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            focusedLabelColor = GreenAccent,
                            cursorColor = GreenAccent
                        )
                    )
                    OutlinedTextField(
                        value = endLon,
                        onValueChange = { endLon = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            focusedLabelColor = GreenAccent,
                            cursorColor = GreenAccent
                        )
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = endName,
                    onValueChange = { endName = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        focusedLabelColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            }

            // Optional Fields
            item {
                OutlinedTextField(
                    value = userContext,
                    onValueChange = { userContext = it },
                    label = { Text("Context (optional)") },
                    placeholder = { Text("e.g., 'éviter les routes principales'") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        focusedLabelColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    label = { Text("Activity Type (optional)") },
                    placeholder = { Text("VELO, RANDONNEE, CAMPING") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        focusedLabelColor = GreenAccent,
                        cursorColor = GreenAccent
                    )
                )
            }

            // Generate Button
            item {
                Button(
                    onClick = {
                        val token = UserPreferences.getToken(context)
                        if (token != null) {
                            viewModel.generateItinerary(
                                token = token,
                                startLat = startLat.toDoubleOrNull() ?: 36.8065,
                                startLon = startLon.toDoubleOrNull() ?: 10.1815,
                                startName = startName.ifEmpty { null },
                                endLat = endLat.toDoubleOrNull() ?: 36.8189,
                                endLon = endLon.toDoubleOrNull() ?: 10.1658,
                                endName = endName.ifEmpty { null },
                                context = userContext.ifEmpty { null },
                                activityType = activityType.ifEmpty { null }
                            )
                            showMap = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate AI Itinerary", fontSize = 16.sp)
                    }
                }
            }

            // Error Display
            if (error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Red.copy(0.1f),
                        border = BorderStroke(1.dp, Color.Red.copy(0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                error ?: "",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Itinerary Result
            if (itinerary != null && showMap) {
                item {
                    ItineraryResultCard(
                        itinerary = itinerary!!,
                        route = route,
                        viewModel = viewModel
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ItineraryResultCard(
    itinerary: com.example.dam.models.FlaskItineraryResponse,
    route: List<com.google.android.gms.maps.model.LatLng>,
    viewModel: FlaskAiViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardGlass,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📊 Itinerary Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        icon = Icons.Default.Straighten,
                        label = "Distance",
                        value = viewModel.getFormattedDistance()
                    )
                    SummaryItem(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = viewModel.getFormattedDuration()
                    )
                    SummaryItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Difficulty",
                        value = "${(itinerary.personalization.difficultyScore * 10).toInt()}/10"
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    itinerary.personalization.difficultyAssessment,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Map Display
        if (route.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                SimpleMapView(route = route)
            }
        }

        // AI Recommendations
        if (itinerary.aiRecommendations.personalizedTips?.isNotEmpty() == true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardGlass,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI Recommendations",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    itinerary.aiRecommendations.personalizedTips?.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = GreenAccent, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                tip,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Equipment Suggestions
        if (itinerary.aiRecommendations.equipmentSuggestions?.isNotEmpty() == true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardGlass,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backpack,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Equipment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    itinerary.aiRecommendations.equipmentSuggestions?.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✓", color = Color(0xFFFF9800), fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                item,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Safety Tips
        if (itinerary.aiRecommendations.safetyTips?.isNotEmpty() == true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardGlass,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Safety Tips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    itinerary.aiRecommendations.safetyTips?.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("⚠", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                tip,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = GreenAccent,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun SimpleMapView(route: List<LatLng>) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                onResume()
                getMapAsync { googleMap ->
                    googleMap.uiSettings.isZoomControlsEnabled = true

                    if (route.isNotEmpty()) {
                        // Add polyline
                        googleMap.addPolyline(
                            PolylineOptions()
                                .addAll(route)
                                .color(android.graphics.Color.parseColor("#00C9A7"))
                                .width(10f)
                        )

                        // Add start marker
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(route.first())
                                .title("Start")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )

                        // Add end marker
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(route.last())
                                .title("End")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )

                        // Zoom to route
                        val bounds = LatLngBounds.builder().apply {
                            route.forEach { include(it) }
                        }.build()
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}