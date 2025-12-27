package com.example.dam.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import com.example.dam.models.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface qui définit les endpoints de l'API d'authentification
 */
interface AuthApiService {

    /**
     * Endpoint pour se connecter
     * POST http://localhost:3000/auth/login
     *
     * @param loginRequest contient email et password
     * @return Response avec LoginResponse (access_token)
     */
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>





    /**
     * Endpoint pour créer un compte utilisateur
     * POST http://localhost:3000/user
     *
     * @param registerRequest contient firstName, lastName, Gender, email, password
     * @return Response avec RegisterResponse (données utilisateur créé)
     */
    @POST("user")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    // Vous pouvez ajouter d'autres endpoints ici plus tard :
    // @POST("auth/register")
    // suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    // NEW: Get user by ID
    @GET("user/{id}")
    suspend fun getUserById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    // NEW: Update user
    @PATCH("user/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body updateUserRequest: UpdateUserRequest,
        @Header("Authorization") token: String
    ): Response<UpdateUserResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

    /**
     * POST /auth/verify-reset-code
     * Vérifier que le code de réinitialisation est valide
     *
     * @param request contient l'email et le code à 6 chiffres
     * @return Response avec message de confirmation
     *
     * Exemple de requête:
     * { "email": "user@example.com", "code": "123456" }
     *
     * Exemple de réponse:
     * { "message": "Code verified successfully" }
     */
    @POST("auth/verify-reset-code")
    suspend fun verifyResetCode(
        @Body request: VerifyResetCodeRequest
    ): Response<VerifyResetCodeResponse>

    /**
     * POST /auth/reset-password
     * Réinitialiser le mot de passe avec le code vérifié
     *
     * @param request contient l'email, le code et le nouveau mot de passe
     * @return Response avec message de confirmation
     *
     * Exemple de requête:
     * { "email": "user@example.com", "code": "123456", "newPassword": "newpass123" }
     *
     * Exemple de réponse:
     * { "message": "Password reset successful" }
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>




    /**
     * ✅ NOUVEAU: Google Sign-In
     * POST /auth/google
     *
     * @param request contient le Google ID Token
     * @return Response avec access_token JWT
     */
    @POST("auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequest
    ): Response<GoogleSignInResponse>








/////for preferences
    @POST("preferences/{userId}/onboarding")
    suspend fun submitOnboardingPreferences(
        @Path("userId") userId: String,
        @Body preferences: OnboardingPreferencesRequest,
        @Header("Authorization") token: String
    ): Response<OnboardingPreferencesResponse>



    // ✅ AJOUTEZ CETTE FONCTION ICI
    /**
     * Get user preferences
     * GET /preferences/{userId}
     */
    @GET("preferences/{userId}")
    suspend fun getPreferences(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<OnboardingPreferencesResponse>




    @Multipart
    @POST("user/{id}/upload")
    suspend fun uploadAvatar(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UserProfileResponse>

    // ========== NEW: FOLLOW/UNFOLLOW ENDPOINTS ==========

    /**
     * Follow a user
     * POST /user/{userId}/follow
     */
    @POST("user/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String  // Should be "Bearer {token}"
    ): Response<FollowResponse>

    /**
     * Unfollow a user
     * DELETE /user/{userId}/follow
     */
    @DELETE("user/{userId}/follow")
    suspend fun unfollowUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<FollowResponse>

    /**
     * Check if current user is following another user
     * GET /user/{userId}/is-following
     */
    @GET("user/{userId}/is-following")
    suspend fun checkIsFollowing(
        @Path("userId") userId: String,
        @Header("Authorization") token: String  // Should be "Bearer {token}"
    ): Response<IsFollowingResponse>

    /**
     * Get follow statistics for a user
     * GET /user/{userId}/follow-stats
     */
    @GET("user/{userId}/follow-stats")
    suspend fun getFollowStats(
        @Path("userId") userId: String,
        @Header("Authorization") token: String  // Should be "Bearer {token}"

    ): Response<FollowStatsResponse>

    /**
     * Get followers of a user
     * GET /user/{userId}/followers
     */
    @GET("user/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Header("Authorization") token: String  // Should be "Bearer {token}"

    ): Response<FollowersResponse>

    /**
     * Get users that a user is following
     * GET /user/{userId}/following
     */
    @GET("user/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Header("Authorization") token: String  // Should be "Bearer {token}"

    ): Response<FollowingResponse>

    /**
     * Get follow suggestions
     * GET /user/suggestions/follow
     */
    @GET("user/suggestions/follow")
    suspend fun getFollowSuggestions(
        @Query("limit") limit: Int = 5,
        @Header("Authorization") token: String
    ): Response<FollowSuggestionsResponse>







    data class FollowResponse(
        @SerializedName("message") val message: String,
        @SerializedName("followersCount") val followersCount: Int? = null
    )

    data class IsFollowingResponse(
        @SerializedName("isFollowing") val isFollowing: Boolean
    )

    data class FollowStatsResponse(
        @SerializedName("followersCount") val followers: Int,  // ✅ CHANGED from "followers"
        @SerializedName("followingCount") val following: Int
    )
}

