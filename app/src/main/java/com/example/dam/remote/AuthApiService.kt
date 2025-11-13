package com.example.dam.remote

// Fichier: app/src/main/java/com/example/dam/data/remote/AuthApiService.kt


import com.example.dam.models.ForgotPasswordRequest
import com.example.dam.models.ForgotPasswordResponse
import com.example.dam.models.LoginRequest
import com.example.dam.models.LoginResponse
import com.example.dam.models.RegisterRequest
import com.example.dam.models.RegisterResponse
import com.example.dam.models.ResetPasswordRequest
import com.example.dam.models.ResetPasswordResponse
import com.example.dam.models.UpdateUserRequest
import com.example.dam.models.UpdateUserResponse
import com.example.dam.models.UserProfileResponse
import com.example.dam.models.VerifyResetCodeRequest
import com.example.dam.models.VerifyResetCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
}

