// app/src/main/java/com/example/dam/Screens/MapWithRoute.kt
package com.example.dam.Screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.getValue  // ✅ AJOUTER
import androidx.compose.runtime.setValue  // ✅ AJOUTER

@Composable
fun MapWithRoute(
    start: LatLng?,
    end: LatLng?,
    polylinePoints: List<LatLng>,
    onMapClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultPosition = LatLng(36.8065, 10.1815)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            start ?: defaultPosition,
            if (start != null) 14f else 11f
        )
    }

    // ✅ Optimisation: Ne pas animer la caméra au premier chargement
    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(start, end) {
        if (start != null && end != null && !isFirstLoad) {
            try {
                val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                    .include(start)
                    .include(end)
                    .build()
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 100),
                    durationMs = 500  // ✅ Animation plus rapide
                )
            } catch (e: Exception) {
                Log.e("MapWithRoute", "Camera animation error: ${e.message}")
            }
        }
        isFirstLoad = false
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL,
            isBuildingEnabled = false,  // ✅ Désactiver les bâtiments 3D
            isTrafficEnabled = false,    // ✅ Désactiver le trafic
            isIndoorEnabled = false      // ✅ Désactiver les plans intérieurs
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = false,  // ✅ Désactiver la boussole
            myLocationButtonEnabled = false,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = false,  // ✅ Désactiver l'inclinaison
            rotationGesturesEnabled = false
        )
    ) {
        start?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Départ",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        end?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Arrivée",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        if (polylinePoints.isNotEmpty()) {
            Polyline(
                points = polylinePoints,
                color = Color(0xFF2196F3),
                width = 8f,  // ✅ Ligne moins épaisse = plus rapide
                geodesic = true
            )
        }
    }
}