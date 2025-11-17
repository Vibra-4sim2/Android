package com.example.dam.models

import com.google.gson.annotations.SerializedName

// ========== REQUEST MODELS ==========

/**
 * Request pour créer une publication
 */
data class CreatePublicationRequest(
    @SerializedName("author") val author: String,
    @SerializedName("content") val content: String,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("mentions") val mentions: List<String>? = null,
    @SerializedName("location") val location: String? = null
)

// ========== RESPONSE MODELS ==========

/**
 * Response pour POST /publication (author = String)
 */
data class PublicationCreateResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("author") val author: String,  // ObjectId simple
    @SerializedName("content") val content: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("mentions") val mentions: List<String>? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("commentsCount") val commentsCount: Int = 0,
    @SerializedName("sharesCount") val sharesCount: Int = 0,
    @SerializedName("likedBy") val likedBy: List<String>? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

/**
 * Response pour GET /publication (author = objet complet)
 */
data class PublicationResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("author") val author: AuthorData?,  // Peut être null (voir dernier post du JSON)
    @SerializedName("content") val content: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("mentions") val mentions: List<MentionData>? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("commentsCount") val commentsCount: Int = 0,
    @SerializedName("sharesCount") val sharesCount: Int = 0,
    @SerializedName("likedBy") val likedBy: List<String>? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int = 0
)

/**
 * Données de l'auteur (populate)
 */
data class AuthorData(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("avatar") val avatar: String? = null
)

/**
 * Données d'une mention (populate)
 */
data class MentionData(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String
)

/**
 * Response d'erreur
 */
data class PublicationErrorResponse(
    @SerializedName("message") val message: Any? = null,
    @SerializedName("statusCode") val statusCode: Int? = null,
    @SerializedName("error") val error: String? = null
)

// ========== HELPERS ==========

/**
 * Extension pour obtenir le nom complet de l'auteur
 */
fun AuthorData.fullName(): String = "$firstName $lastName"

/**
 * Extension pour obtenir le nom complet d'une mention
 */
fun MentionData.fullName(): String = "$firstName $lastName"

/**
 * Extension pour vérifier si l'image existe
 */
fun PublicationResponse.hasImage(): Boolean = !image.isNullOrEmpty()

/**
 * Extension pour obtenir l'avatar de l'auteur ou un placeholder
 */
fun AuthorData?.avatarOrPlaceholder(): String =
    this?.avatar?.takeIf { it.isNotEmpty() } ?: ""