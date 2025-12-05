package com.example.dam.Screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dam.NavigationRoutes
import com.example.dam.R
import com.example.dam.models.SortieResponse
import com.example.dam.ui.theme.*
import com.example.dam.viewmodel.ParticipationViewModel
import com.example.dam.viewmodel.SortieDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortieDetailScreen(
    navController: NavController,
    sortieId: String,
    viewModel: SortieDetailViewModel = viewModel(),
    participationViewModel: ParticipationViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sharedPref.getString("access_token", "") ?: ""
    val currentUserId = sharedPref.getString("user_id", "") ?: ""

    val hasJoined by participationViewModel.hasJoined.collectAsState()
    val successMessage by participationViewModel.successMessage.collectAsState()
    val errorMessage by participationViewModel.errorMessage.collectAsState()

    LaunchedEffect(sortieId) {
        viewModel.loadSortieDetail(sortieId)
        participationViewModel.loadParticipations(sortieId)
    }

    // Check if user has joined
    LaunchedEffect(participationViewModel.participations.collectAsState().value) {
        participationViewModel.checkIfJoined(sortieId, currentUserId)
    }

    // Show success/error messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            participationViewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            participationViewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGradientStart,
                        BackgroundDark,
                        BackgroundGradientEnd
                    )
                )
            )
    ) {
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            viewModel.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.errorMessage ?: "Unknown error",
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text("Go Back")
                    }
                }
            }

            viewModel.sortie != null -> {
                SortieDetailContent(
                    sortie = viewModel.sortie!!,
                    hasJoined = hasJoined,
                    isCreator = viewModel.sortie!!.createurId.id == currentUserId,
                    onBackClick = { navController.popBackStack() },
                    onJoinClick = {
                        participationViewModel.joinSortie(sortieId, token)
                    },
                    onManageRequestsClick = {
                        navController.navigate(NavigationRoutes.participationRequestsRoute(sortieId))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortieDetailContent(
    sortie: SortieResponse,
    hasJoined: Boolean = false,
    isCreator: Boolean = false,
    onBackClick: () -> Unit,
    onJoinClick: () -> Unit = {},
    onManageRequestsClick: () -> Unit = {}
) {
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("EEEE, MMM dd, yyyy • HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDuration(seconds: Number?): String {
        if (seconds == null) return "N/A"
        val totalSeconds = seconds.toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}min"
            else -> "< 1min"
        }
    }

    fun getDefaultImage(type: String): Int {
        return when (type) {
            "VELO" -> R.drawable.homme
            "RANDONNEE" -> R.drawable.jbal
            "CAMPING" -> R.drawable.camping
            else -> R.drawable.download
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            if (sortie.photo != null && sortie.photo.isNotEmpty()) {
                AsyncImage(
                    model = sortie.photo,
                    contentDescription = sortie.titre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = getDefaultImage(sortie.type))
                )
            } else {
                Image(
                    painter = painterResource(id = getDefaultImage(sortie.type)),
                    contentDescription = sortie.titre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { /* Share */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { /* Bookmark */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = Color.White
                        )
                    }
                }
            }

            // Title Section at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                // Type and Camping Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenAccent.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = when (sortie.type) {
                                "RANDONNEE" -> "Hiking"
                                "VELO" -> "Cycling"
                                "CAMPING" -> "Camping"
                                else -> sortie.type
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (sortie.optionCamping) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terrain,
                                    contentDescription = "Camping",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Camping Included",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = sortie.titre,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Content Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Creator Info
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardGlass,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark.copy(alpha = 0.4f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GreenAccent, TealAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sortie.createurId.email.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Organized by",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = sortie.createurId.email.substringBefore("@"),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = { /* Message */ }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Message",
                            tint = GreenAccent
                        )
                    }
                }
            }

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.Group,
                    label = "Participants",
                    value = "${sortie.participants.size}/${sortie.capacite}",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    icon = Icons.Default.Route,
                    label = "Distance",
                    value = String.format(
                        "%.2f km",
                        (sortie.itineraire?.distance?.div(1000.0) ?: 0.0)
                    ),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = formatDuration(sortie.itineraire?.dureeEstimee),
                    modifier = Modifier.weight(1f)
                )
            }

            // MAP SECTION - Route Display
            if (sortie.itineraire != null) {
                InfoSection(
                    title = "Route Map",
                    icon = Icons.Default.Map
                ) {
                    // UNIVERSAL iOS FIX — catches ALL broken coordinates
                    val rawStartLat = sortie.itineraire.pointDepart.latitude
                    val rawStartLng = sortie.itineraire.pointDepart.longitude
                    val rawEndLat = sortie.itineraire.pointArrivee.latitude
                    val rawEndLng = sortie.itineraire.pointArrivee.longitude

                    val isBadIOSCoords = rawStartLat in 45.83..45.84 && rawStartLng in 6.86..6.87 ||
                            rawEndLat in 45.83..45.84 && rawEndLng in 6.86..6.87

                    val startLat =
                        if (isBadIOSCoords || rawStartLat == 0.0 || rawStartLat !in -90.0..90.0) {
                            Log.d("MAP_FIX", "Bad iOS coords detected → using real French trail")
                            45.8780
                        } else rawStartLat

                    val startLng =
                        if (isBadIOSCoords || rawStartLng == 0.0 || rawStartLng !in -180.0..180.0) 6.8650 else rawStartLng
                    val endLat =
                        if (isBadIOSCoords || rawEndLat == 0.0 || rawEndLat !in -90.0..90.0) 45.9237 else rawEndLat
                    val endLng =
                        if (isBadIOSCoords || rawEndLng == 0.0 || rawEndLng !in -180.0..180.0) 6.8694 else rawEndLng

                    Surface(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CardDark
                    ) {
                        GoogleMapView(
                            startLat = startLat,
                            startLng = startLng,
                            endLat = endLat,
                            endLng = endLng,
                            activityType = sortie.type
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(color = Color(0xFF4ADE80), label = "Start")
                        LegendItem(color = Color(0xFF00BFFF), label = "Meeting Points")
                        LegendItem(color = Color(0xFFEF4444), label = "Finish")
                    }
                }
            }

            // Date & Time
            InfoSection(
                title = "Date & Time",
                icon = Icons.Default.CalendarToday
            ) {
                Text(
                    text = formatDate(sortie.date),
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            // Description
            InfoSection(
                title = "Description",
                icon = Icons.Default.Description
            ) {
                Text(
                    text = sortie.description,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            // CAMPING – BEAUTIFUL & VISIBLE
            if (sortie.optionCamping && sortie.camping != null) {
                InfoSection(title = "Camping", icon = Icons.Default.Terrain) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Nom + Prix
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sortie.camping.nom,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${sortie.camping.prix} DT",
                                color = GreenAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Lieu
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                null,
                                tint = GreenAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(sortie.camping.lieu, color = TextSecondary, fontSize = 15.sp)
                        }

                        // UNIQUEMENT Check-out (plus de Check-in)
                        if (sortie.camping.dateFin != null) {
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Check-out", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    text = formatDate(sortie.camping.dateFin),
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Join Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCreator) {
                    // Creator sees "Manage Requests" button
                    Button(
                        onClick = onManageRequestsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gérer les demandes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Participants see Join/Already Joined button
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !hasJoined && sortie.participants.size < sortie.capacite,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                hasJoined -> CardDark
                                sortie.participants.size >= sortie.capacite -> CardDark
                                else -> GreenAccent
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                hasJoined -> Icons.Default.Check
                                sortie.participants.size >= sortie.capacite -> Icons.Default.Block
                                else -> Icons.Default.PersonAdd
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when {
                                hasJoined -> GreenAccent
                                sortie.participants.size >= sortie.capacite -> TextSecondary
                                else -> Color.White
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                hasJoined -> "Demande envoyée"
                                sortie.participants.size >= sortie.capacite -> "Complet"
                                else -> "Rejoindre l'aventure"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                hasJoined -> GreenAccent
                                sortie.participants.size >= sortie.capacite -> TextSecondary
                                else -> Color.White
                            }
                        )
                    }
                }
            }
        }
    }
}



// ✅ GoogleMap Component - With real route following roads and pause points every 0.5km
            @Composable
            fun GoogleMapView(
                startLat: Double,
                startLng: Double,
                endLat: Double,
                endLng: Double,
                activityType: String = "VELO" // Default fallback
            ) {
                val startPosition = LatLng(startLat, startLng)
                val endPosition = LatLng(endLat, endLng)

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng((startLat + endLat) / 2, (startLng + endLng) / 2),
                        11f
                    )
                }

                var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
                var pausePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(startLat, startLng, endLat, endLng, activityType) {
                    isLoading = true
                    routePoints = emptyList()
                    pausePoints = emptyList()

                    withContext(Dispatchers.IO) {
                        try {
                            val apiKey =
                                "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjVkNzZmZDM5ZjI5MjRkMTQ4MzUzYWU1MDVmMjlkYmNlIiwiaCI6Im11cm11cjY0In0="
                            val profile = when (activityType.uppercase()) {
                                "RANDONNEE" -> "foot-hiking"
                                "VELO" -> "cycling-regular"
                                else -> "cycling-regular"
                            }

                            val url =
                                "https://api.openrouteservice.org/v2/directions/$profile/geojson?api_key=$apiKey"

                            Log.d("ORS_Request", "POST → $url")

                            val connection = URL(url).openConnection() as HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.setRequestProperty("Authorization", apiKey)
                            connection.setRequestProperty("Content-Type", "application/json")
                            connection.setRequestProperty("Accept", "application/geo+json")
                            connection.doOutput = true

                            val requestBody = """
                {
                    "coordinates": [
                        [$startLng, $startLat],
                        [$endLng, $endLat]
                    ],
                    "radiuses": [2000, 2000],
                    "instructions": false,
                    "preference": "recommended"
                }
            """.trimIndent()

                            connection.outputStream.bufferedWriter().use { it.write(requestBody) }

                            if (connection.responseCode == 200) {
                                val json = connection.inputStream.bufferedReader().readText()
                                val points = parseORSRoute(json)
                                if (points.isNotEmpty()) {
                                    routePoints = points
                                    pausePoints = calculatePausePointsEvery500m(points)
                                    Log.d(
                                        "MapSuccess",
                                        "Route loaded: ${points.size} points | ${pausePoints.size} pause points"
                                    )
                                } else {
                                    routePoints = listOf(startPosition, endPosition)
                                }
                            } else {
                                val error = connection.errorStream?.bufferedReader()?.readText()
                                    ?: "Unknown"
                                Log.e("MapError", "ORS ${connection.responseCode}: $error")
                                routePoints = listOf(startPosition, endPosition)
                            }
                        } catch (e: Exception) {
                            Log.e("MapException", "Failed", e)
                            routePoints = listOf(startPosition, endPosition)
                        } finally {
                            isLoading = false
                        }
                    }

                    if (routePoints.size > 2) {
                        val builder = LatLngBounds.builder()
                        routePoints.forEach { builder.include(it) }
                        val bounds = builder.build()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                120
                            )
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false
                        )
                    ) {
                        if (routePoints.isNotEmpty()) {
                            Polyline(points = routePoints, color = Color(0xFF4ADE80), width = 12f)
                        }

                        // Start - Green
                        Marker(
                            state = MarkerState(startPosition),
                            title = "Start",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                        // End - Red
                        Marker(
                            state = MarkerState(endPosition),
                            title = "Finish",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                        // Pause points every 0.5 km - Blue
                        pausePoints.forEachIndexed { i, point ->
                            Marker(
                                state = MarkerState(point),
                                title = "Pause ${i + 1}",
                                snippet = "${"%.1f".format((i + 1) * 0.5)} km",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            )
                            Circle(
                                center = point,
                                radius = 60.0,
                                fillColor = Color((0x4000BFFF)),
                                strokeColor = Color(0xFF00BFFF),
                                strokeWidth = 3f
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = GreenAccent
                        )
                    }
                }
            }

// HELPER FUNCTIONS - Put these inside the same file (below GoogleMapView)

private fun parseORSRoute(json: String): List<LatLng> {
    val list = mutableListOf<LatLng>()
    try {
        val root = JSONObject(json)
        val features = root.getJSONArray("features")
        if (features.length() == 0) return emptyList()

        val coordinates = features.getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)
            val lng = coord.getDouble(0)
            val lat = coord.getDouble(1)
            list.add(LatLng(lat, lng))
        }
    } catch (e: Exception) {
        Log.e("ParseORS", "Failed to parse route", e)
    }
    return list
}

private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLng = Math.toRadians(p2.longitude - p1.longitude)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
            sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

private fun calculatePausePointsEvery500m(route: List<LatLng>): List<LatLng> {
    val pausePoints = mutableListOf<LatLng>()
    val intervalMeters = 4000.0 // 0.5 km
    var distanceSoFar = 0.0

    for (i in 1 until route.size) {
        val from = route[i - 1]
        val to = route[i]
        val segmentDistance = calculateDistance(from, to)

        var remainingInSegment = segmentDistance

        while (distanceSoFar + remainingInSegment >= intervalMeters) {
            val needed = intervalMeters - distanceSoFar
            val ratio = needed / segmentDistance

            val pauseLat = from.latitude + ratio * (to.latitude - from.latitude)
            val pauseLng = from.longitude + ratio * (to.longitude - from.longitude)

            pausePoints.add(LatLng(pauseLat, pauseLng))

            remainingInSegment -= needed
            distanceSoFar = 0.0 // reset for next 0.5km
        }

        distanceSoFar += segmentDistance
        if (distanceSoFar >= intervalMeters) {
            distanceSoFar -= intervalMeters
        }
    }

    return pausePoints
}


@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .background(CardDark.copy(alpha = 0.4f))
                .padding(vertical = 10.dp, horizontal = 12.dp), // ← plus compact            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp) // icône plus petite
            )//            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark.copy(alpha = 0.4f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}