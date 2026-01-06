package com.example.dam.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

/**
 * Utilitaire pour gérer la permission RECORD_AUDIO
 */
object PermissionHelper {

    /**
     * Vérifier si la permission RECORD_AUDIO est accordée
     */
    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Composable pour créer un lanceur de demande de permission RECORD_AUDIO
 * ✅ CORRIGÉ: Utilise rememberLauncherForActivityResult (API Compose)
 */
@Composable
fun rememberRecordAudioPermissionLauncher(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("PermissionHelper", "✅ Permission RECORD_AUDIO accordée")
            onPermissionGranted()
        } else {
            android.util.Log.e("PermissionHelper", "❌ Permission RECORD_AUDIO refusée")
            onPermissionDenied()
        }
    }
}

