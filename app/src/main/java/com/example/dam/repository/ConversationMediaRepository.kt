package com.example.dam.repository

import android.content.Context
import android.util.Log
import com.example.dam.utils.UserPreferences
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * ✅ Repository pour les uploads de médias dans les conversations privées
 * Endpoints:
 * - POST /conversations/upload/image
 * - POST /conversations/upload/audio
 * - POST /conversations/upload/video
 * - POST /conversations/upload/file
 */
class ConversationMediaRepository {

    companion object {
        private const val TAG = "ConversationMediaRepo"
        private const val BASE_URL = "https://dam-4sim2.onrender.com"

        // Limites de taille
        const val MAX_IMAGE_SIZE_MB = 10
        const val MAX_AUDIO_SIZE_MB = 25
        const val MAX_VIDEO_SIZE_MB = 50
        const val MAX_FILE_SIZE_MB = 25
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Résultat d'upload
     */
    data class UploadResult(
        val success: Boolean,
        val url: String?,
        val publicId: String?,
        val duration: Int? = null,
        val format: String?,
        val mimeType: String?,
        val size: Long?,
        val originalName: String?,
        val errorMessage: String? = null
    )

    /**
     * Upload d'image pour message privé
     */
    suspend fun uploadImage(context: Context, file: File): UploadResult {
        return uploadMedia(context, file, "image", "image/*")
    }

    /**
     * Upload d'audio pour message privé
     */
    suspend fun uploadAudio(context: Context, file: File): UploadResult {
        return uploadMedia(context, file, "audio", "audio/*")
    }

    /**
     * Upload de vidéo pour message privé
     */
    suspend fun uploadVideo(context: Context, file: File): UploadResult {
        return uploadMedia(context, file, "video", "video/*")
    }

    /**
     * Upload de fichier générique pour message privé
     */
    suspend fun uploadFile(context: Context, file: File): UploadResult {
        val mimeType = getMimeType(file)
        return uploadMedia(context, file, "file", mimeType)
    }

    /**
     * Méthode générique d'upload
     */
    private suspend fun uploadMedia(
        context: Context,
        file: File,
        type: String,
        mimeType: String
    ): UploadResult {
        try {
            // Validation
            if (!file.exists()) {
                return UploadResult(
                    success = false,
                    url = null,
                    publicId = null,
                    format = null,
                    mimeType = null,
                    size = null,
                    originalName = null,
                    errorMessage = "Le fichier n'existe pas"
                )
            }

            // Vérifier la taille selon le type
            val maxSizeMB = when (type) {
                "image" -> MAX_IMAGE_SIZE_MB
                "audio" -> MAX_AUDIO_SIZE_MB
                "video" -> MAX_VIDEO_SIZE_MB
                else -> MAX_FILE_SIZE_MB
            }

            val fileSizeMB = file.length() / (1024 * 1024)
            if (fileSizeMB > maxSizeMB) {
                return UploadResult(
                    success = false,
                    url = null,
                    publicId = null,
                    format = null,
                    mimeType = null,
                    size = null,
                    originalName = null,
                    errorMessage = "Fichier trop volumineux (max ${maxSizeMB}MB)"
                )
            }

            // Récupérer le token
            val token = UserPreferences.getToken(context)
            if (token.isNullOrEmpty()) {
                return UploadResult(
                    success = false,
                    url = null,
                    publicId = null,
                    format = null,
                    mimeType = null,
                    size = null,
                    originalName = null,
                    errorMessage = "Non authentifié"
                )
            }

            Log.d(TAG, "📤 Uploading $type: ${file.name} (${file.length()} bytes)")

            // Créer le body multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            // Créer la requête
            val endpoint = when (type) {
                "image" -> "/conversations/upload/image"
                "audio" -> "/conversations/upload/audio"
                "video" -> "/conversations/upload/video"
                else -> "/conversations/upload/file"
            }

            val request = Request.Builder()
                .url("$BASE_URL$endpoint")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            // Exécuter la requête
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "📥 Response code: ${response.code}")
            Log.d(TAG, "📥 Response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)

                if (json.optBoolean("success", false)) {
                    return UploadResult(
                        success = true,
                        url = json.optString("url"),
                        publicId = json.optString("publicId"),
                        duration = json.optInt("duration", -1).takeIf { it > 0 },
                        format = json.optString("format"),
                        mimeType = json.optString("mimeType"),
                        size = json.optLong("size"),
                        originalName = json.optString("originalName")
                    )
                } else {
                    return UploadResult(
                        success = false,
                        url = null,
                        publicId = null,
                        format = null,
                        mimeType = null,
                        size = null,
                        originalName = null,
                        errorMessage = json.optString("message", "Upload échoué")
                    )
                }
            } else {
                val errorMsg = try {
                    JSONObject(responseBody ?: "{}").optString("message", "Erreur serveur")
                } catch (e: Exception) {
                    "Erreur serveur (${response.code})"
                }

                return UploadResult(
                    success = false,
                    url = null,
                    publicId = null,
                    format = null,
                    mimeType = null,
                    size = null,
                    originalName = null,
                    errorMessage = errorMsg
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Upload error: ${e.message}", e)
            return UploadResult(
                success = false,
                url = null,
                publicId = null,
                format = null,
                mimeType = null,
                size = null,
                originalName = null,
                errorMessage = e.message ?: "Erreur d'upload"
            )
        }
    }

    /**
     * Obtenir le type MIME d'un fichier
     */
    private fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            // Audio
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            // Video
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            else -> "application/octet-stream"
        }
    }
}

