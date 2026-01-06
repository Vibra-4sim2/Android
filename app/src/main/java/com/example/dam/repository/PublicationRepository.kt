package com.example.dam.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dam.models.PublicationCreateResponse
import com.example.dam.models.PublicationErrorResponse
import com.example.dam.models.PublicationResponse
import com.example.dam.remote.RetrofitInstance
import com.example.dam.utils.UserPreferences
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.Result

/**
 * Repository pour gérer les opérations liées aux publications
 */
class PublicationRepository(private val context: Context) {

    private val TAG = "PublicationRepository"
    private val api = RetrofitInstance.publicationApi

    /**
     * Créer une publication (avec ou sans image, toujours multipart)
     *
     * @param content Texte de la publication
     * @param imageUri URI de l'image (optionnel)
     * @param tags Liste de tags (optionnel)
     * @param mentions Liste de mentions (optionnel)
     * @param location Location (optionnel)
     * @return Result avec PublicationResponse ou message d'erreur
     */
    suspend fun createPublication(
        content: String,
        imageUri: Uri? = null,
        tags: List<String>? = null,
        mentions: List<String>? = null,
        location: String? = null
    ): Result<PublicationResponse> {
        return try {
            val userId = UserPreferences.getUserId(context)
            if (userId.isNullOrEmpty()) {
                Log.e(TAG, "❌ User ID not found in preferences")
                return Result.failure(Exception("User not logged in"))
            }

            Log.d(TAG, "📤 Creating publication for user: $userId")
            Log.d(TAG, "   Content: $content")
            Log.d(TAG, "   Image: ${imageUri != null}")
            Log.d(TAG, "   Tags: $tags")
            Log.d(TAG, "   Mentions: $mentions")
            Log.d(TAG, "   Location: $location")

            // ✅ Préparer les champs (author et content sont OBLIGATOIRES)
            val authorBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())

            // ✅ CORRECTION : Conversion List<String> → String séparée par virgules
            val tagsBody = tags
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(",")  // Ex: ["cycling", "fitness"] → "cycling,fitness"
                ?.toRequestBody("text/plain".toMediaTypeOrNull())

            val mentionsBody = mentions
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(",")  // Ex: ["userId1", "userId2"] → "userId1,userId2"
                ?.toRequestBody("text/plain".toMediaTypeOrNull())

            val locationBody = location
                ?.takeIf { it.isNotBlank() }
                ?.toRequestBody("text/plain".toMediaTypeOrNull())

            // ✅ Préparer le fichier si présent
            val filePart: MultipartBody.Part? = if (imageUri != null) {
                try {
                    val file = uriToFile(imageUri)
                    Log.d(TAG, "   File prepared: ${file.name} (${file.length()} bytes)")
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error preparing file", e)
                    null
                }
            } else {
                null
            }

            // ✅ LOGS DE DEBUG DÉTAILLÉS
            Log.d(TAG, "═══════════════════════════════════")
            Log.d(TAG, "🔥 CALLING API createPublication")
            Log.d(TAG, "   author: $userId")
            Log.d(TAG, "   content: $content")
            Log.d(TAG, "   file: ${if (filePart != null) "YES" else "NO"}")
            Log.d(TAG, "   tags: ${tags?.joinToString(",") ?: "null"}")
            Log.d(TAG, "   mentions: ${mentions?.joinToString(",") ?: "null"}")
            Log.d(TAG, "   location: ${location ?: "null"}")
            Log.d(TAG, "═══════════════════════════════════")

            // ✅ Appel API - Returns PublicationCreateResponse (author is String)
            val response: Response<PublicationCreateResponse> = api.createPublication(
                author = authorBody,
                content = contentBody,
                file = filePart,
                tags = tagsBody,
                mentions = mentionsBody,
                location = locationBody
            )

            // ✅ LOG DE LA RÉPONSE
            Log.d(TAG, "📥 Response code: ${response.code()}")
            Log.d(TAG, "📥 Response URL: ${response.raw().request.url}")
            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Error body: ${response.errorBody()?.string()}")
            }

            if (response.isSuccessful && response.body() != null) {
                val createResponse = response.body()!!
                Log.d(TAG, "✅ Publication created: ${createResponse.id}")

                // Convert PublicationCreateResponse to PublicationResponse for compatibility
                val publicationResponse = PublicationResponse(
                    id = createResponse.id,
                    author = null, // Will be populated when fetching from feed
                    content = createResponse.content,
                    image = createResponse.image,
                    tags = createResponse.tags,
                    mentions = null, // Will be populated when fetching from feed
                    location = createResponse.location,
                    likesCount = createResponse.likesCount,
                    commentsCount = createResponse.commentsCount,
                    sharesCount = createResponse.sharesCount,
                    likedBy = createResponse.likedBy,
                    isActive = createResponse.isActive,
                    createdAt = createResponse.createdAt,
                    updatedAt = createResponse.updatedAt
                )

                Result.success(publicationResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                Log.e(TAG, "❌ Error creating publication (${response.code()}): $errorMessage")
                Log.e(TAG, "   Raw error: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception creating publication", e)
            Result.failure(e)
        }
    }

    /**
     * Récupérer toutes les publications
     */
    suspend fun getAllPublications(): Result<List<PublicationResponse>> {
        return try {
            Log.d(TAG, "📥 Fetching all publications")
            val response: Response<List<PublicationResponse>> = api.getAllPublications()

            if (response.isSuccessful && response.body() != null) {
                val publications = response.body()!!
                Log.d(TAG, "✅ Fetched ${publications.size} publications")

                // Log détaillé pour debug
                publications.take(2).forEach { pub ->
                    Log.d(TAG, "   📄 Post: ${pub.content.take(50)}...")
                    //Log.d(TAG, "      Author: ${pub.author?.fullName() ?: "Unknown"}")
                    Log.d(TAG, "      Likes: ${pub.likesCount}, Comments: ${pub.commentsCount}")
                }

                Result.success(publications)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Log.e(TAG, "❌ Error fetching publications (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching publications", e)
            Result.failure(e)
        }
    }

    /**
     * Liker/Unliker une publication
     */
    suspend fun likePublication(publicationId: String): Result<PublicationCreateResponse> {
        return try {
            val userId = UserPreferences.getUserId(context)
            if (userId.isNullOrEmpty()) {
                return Result.failure(Exception("User not logged in"))
            }

            Log.d(TAG, "❤️ Toggling like on publication: $publicationId")
            val response: Response<PublicationCreateResponse> = api.likePublication(
                publicationId,
                mapOf("userId" to userId)
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✅ Like toggled successfully")
                Result.success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Log.e(TAG, "❌ Error toggling like: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception toggling like", e)
            Result.failure(e)
        }
    }

    // ========== HELPERS ==========

    /**
     * Convertir URI en File
     */
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream for URI")

        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        Log.d(TAG, "✅ File created: ${file.absolutePath}")
        return file
    }

    /**
     * Parser le message d'erreur depuis le backend
     */
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrEmpty()) return "Unknown error"

            val error = Gson().fromJson(errorBody, PublicationErrorResponse::class.java)
            when (val msg = error.message) {
                is String -> msg
                is List<*> -> msg.joinToString(", ")
                else -> error.error ?: "Unknown error"
            }
        } catch (_: Exception) {
            errorBody ?: "Unknown error"
        }
    }
}