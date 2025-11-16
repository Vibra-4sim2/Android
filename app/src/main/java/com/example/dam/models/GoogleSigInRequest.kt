
package com.example.dam.models


data class GoogleSignInRequest(
    val idToken: String
)

data class GoogleSignInResponse(
    val access_token: String
)