package com.example.dam.models

import com.google.gson.annotations.SerializedName

// ========== EXISTING MODELS (DÉJÀ EXISTANTS) ==========

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String
)

data class RegisterRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("Gender") val Gender: String,
    @SerializedName("birthday") val birthDate: String,  // ✅ "birthday" for backend API

    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("gender") val Gender: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: UserData? = null
)

data class UserData(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("gender") val Gender: String? = null,
    @SerializedName("email") val email: String? = null
)

data class ErrorResponse(
    @SerializedName("message") val message: Any? = null,
    @SerializedName("statusCode") val statusCode: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errors") val errors: List<String>? = null
)

data class UserProfileResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("Gender") val gender: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = "",
    @SerializedName("password") val password: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int? = null
)

data class UpdateUserRequest(
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("Gender") val gender: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null
)

typealias UpdateUserResponse = UserProfileResponse

// ========== 🆕 NEW MODELS FOR PASSWORD RESET ==========

/**
 * Request pour POST /auth/forgot-password
 * Demande l'envoi d'un code de réinitialisation par email
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Response de POST /auth/forgot-password
 * Confirmation que le code a été envoyé
 */
data class ForgotPasswordResponse(
    @SerializedName("message")
    val message: String // "Reset code sent successfully"
)

/**
 * Request pour POST /auth/verify-reset-code
 * Vérifie que le code de réinitialisation est valide
 */
data class VerifyResetCodeRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String
)

/**
 * Response de POST /auth/verify-reset-code
 * Confirmation que le code est valide
 */
data class VerifyResetCodeResponse(

    @SerializedName("message")
    val message: String // "Code verified successfully"
)

/**
 * Request pour POST /auth/reset-password
 * Réinitialise le mot de passe avec le code vérifié
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("newPassword")
    val newPassword: String


)

/**
 * Response de POST /auth/reset-password
 * Confirmation que le mot de passe a été réinitialisé
 */
data class ResetPasswordResponse(
    @SerializedName("message")
    val message: String
)

/*
AJOUTEZ CE CODE À LA FIN DE VOTRE FICHIER AuthModels.kt EXISTANT
*/







data class OnboardingPreferencesRequest(
    @SerializedName("level") val level: String? = null,
    @SerializedName("cyclingType") val cyclingType: String? = null,
    @SerializedName("cyclingFrequency") val cyclingFrequency: String? = null,
    @SerializedName("cyclingDistance") val cyclingDistance: String? = null,
    @SerializedName("cyclingGroupInterest") val cyclingGroupInterest: Boolean? = null,
    @SerializedName("hikeType") val hikeType: String? = null,
    @SerializedName("hikeDuration") val hikeDuration: String? = null,
    @SerializedName("hikePreference") val hikePreference: String? = null,
    @SerializedName("campingPractice") val campingPractice: Boolean? = null,
    @SerializedName("campingType") val campingType: String? = null,
    @SerializedName("campingDuration") val campingDuration: String? = null
)

data class OnboardingPreferencesResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: String,
    @SerializedName("level") val level: String? = null,
    @SerializedName("cyclingType") val cyclingType: String? = null,
    @SerializedName("cyclingFrequency") val cyclingFrequency: String? = null,
    @SerializedName("cyclingDistance") val cyclingDistance: String? = null,
    @SerializedName("cyclingGroupInterest") val cyclingGroupInterest: Boolean? = null,
    @SerializedName("hikeType") val hikeType: String? = null,
    @SerializedName("hikeDuration") val hikeDuration: String? = null,
    @SerializedName("hikePreference") val hikePreference: String? = null,
    @SerializedName("campingPractice") val campingPractice: Boolean? = null,
    @SerializedName("campingType") val campingType: String? = null,
    @SerializedName("campingDuration") val campingDuration: String? = null,
    @SerializedName("onboardingComplete") val onboardingComplete: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val version: Int? = null
)




/// ========== FOLLOW/UNFOLLOW MODELS ==========

data class FollowResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("followersCount") val followersCount: Int? = null
)

data class IsFollowingResponse(
    @SerializedName("isFollowing") val isFollowing: Boolean

)

data class FollowStatsResponse(
    @SerializedName("followers") val followers: Int,
    @SerializedName("following") val following: Int
)

data class FollowUserItem(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = "",
    @SerializedName("followersCount") val followersCount: Int? = 0,
    @SerializedName("followingCount") val followingCount: Int? = 0
)

data class FollowersResponse(
    @SerializedName("followers") val followers: List<FollowUserItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class FollowingResponse(
    @SerializedName("following") val following: List<FollowUserItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class FollowSuggestionsResponse(
    @SerializedName("suggestions") val suggestions: List<FollowUserItem>,
    @SerializedName("count") val count: Int
)







