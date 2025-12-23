package com.example.dam.utils

// utils/Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data class Failure(val message: Exception) : Result<Nothing>()

    object Loading : Result<Nothing>()
}