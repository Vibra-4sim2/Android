
package com.example.dam.models

import com.google.gson.annotations.SerializedName

data class GoogleSignInRequest(
    val idToken: String
)

/**
 * Response from Google Sign-In endpoint
 * Works with existing backend - only returns access_token
 */
data class GoogleSignInResponse(
    @SerializedName("access_token")
    val access_token: String
)