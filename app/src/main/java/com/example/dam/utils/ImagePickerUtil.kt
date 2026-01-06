package com.example.dam.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.io.File
import java.io.FileOutputStream

/**
 * Utilitaire pour la sélection et la gestion des images
 */
object ImagePickerUtil {

    private const val TAG = "ImagePickerUtil"

    /**
     * Convertir un Uri en File temporaire pour l'upload
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        try {
            // Obtenir le nom du fichier
            val fileName = getFileName(context, uri) ?: "temp_image_${System.currentTimeMillis()}.jpg"

            // Créer un fichier temporaire
            val tempFile = File(context.cacheDir, fileName)

            // Copier le contenu de l'Uri vers le fichier
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "✅ File created: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
            return tempFile

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error converting Uri to File: ${e.message}", e)
            return null
        }
    }

    /**
     * Obtenir le nom du fichier depuis un Uri
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name: ${e.message}")
        }

        return fileName
    }

    /**
     * Vérifier si un fichier est une image valide
     */
    fun isValidImage(file: File): Boolean {
        val validExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
        val extension = file.extension.lowercase()
        return validExtensions.contains(extension)
    }

    /**
     * Vérifier la taille du fichier (limite à 10 MB)
     */
    fun isValidSize(file: File, maxSizeMB: Int = 10): Boolean {
        val maxSizeBytes = maxSizeMB * 1024 * 1024
        return file.length() <= maxSizeBytes
    }

    /**
     * Valider une image avant l'upload
     */
    fun validateImage(file: File): kotlin.Result<Boolean> {
        return when {
            !file.exists() -> {
                kotlin.Result.failure(Exception("Le fichier n'existe pas"))
            }
            !isValidImage(file) -> {
                kotlin.Result.failure(Exception("Format d'image non supporté. Utilisez JPG, PNG ou GIF"))
            }
            !isValidSize(file, 10) -> {
                kotlin.Result.failure(Exception("L'image est trop grande. Taille maximale: 10 MB"))
            }
            else -> {
                kotlin.Result.success(true)
            }
        }
    }
}

/**
 * Composable pour créer un lanceur de sélection d'image
 */
@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (Uri) -> Unit,
    onError: (String) -> Unit
): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Log.d("ImagePicker", "✅ Image sélectionnée: $uri")
            onImageSelected(uri)
        } else {
            Log.d("ImagePicker", "❌ Aucune image sélectionnée")
            onError("Aucune image sélectionnée")
        }
    }
}

