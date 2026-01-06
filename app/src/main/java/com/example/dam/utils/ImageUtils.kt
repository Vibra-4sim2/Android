package com.example.dam.utils

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dam.R

/**
 * Safe avatar URL extraction
 * Returns null if avatar is null, empty, or invalid
 */
fun String?.toSafeAvatarUrl(): String? {
    return this?.takeIf { it.isNotBlank() && it.isNotEmpty() }
}

/**
 * Composable for displaying user avatar with automatic fallback
 *
 * @param avatarUrl URL of the avatar image (can be null or empty)
 * @param modifier Modifier for the image
 * @param contentDescription Description for accessibility
 * @param contentScale How to scale the image (default: Crop)
 * @param fallbackDrawable Drawable resource ID for fallback (default: R.drawable.homme)
 */
@Suppress("unused")
@Composable
fun UserAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "User Avatar",
    contentScale: ContentScale = ContentScale.Crop,
    fallbackDrawable: Int = R.drawable.homme
) {
    val safeUrl = avatarUrl.toSafeAvatarUrl()

    // Debug logging
    Log.d("UserAvatar", "┌─────────────────────────────────────")
    Log.d("UserAvatar", "│ Avatar URL received: $avatarUrl")
    Log.d("UserAvatar", "│ Safe URL: $safeUrl")
    Log.d("UserAvatar", "│ Will show: ${if (safeUrl != null) "AsyncImage" else "Fallback"}")
    Log.d("UserAvatar", "└─────────────────────────────────────")

    if (safeUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(safeUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = fallbackDrawable),
            placeholder = painterResource(id = fallbackDrawable),
            fallback = painterResource(id = fallbackDrawable)
        )
    } else {
        // Fallback to default image
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

/**
 * Composable for displaying user avatar with initials fallback
 * Shows initials if no avatar is available
 *
 * @param avatarUrl URL of the avatar image (can be null or empty)
 * @param firstName User's first name
 * @param lastName User's last name
 * @param modifier Modifier for the container
 * @param contentDescription Description for accessibility
 * @param backgroundColor Background color for initials display
 * @param textColor Text color for initials
 */
@Suppress("unused")
@Composable
fun UserAvatarWithInitials(
    avatarUrl: String?,
    firstName: String?,
    lastName: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "User Avatar",
    backgroundColor: Color = Color(0xFF374151),
    textColor: Color = Color(0xFF4ADE80)
) {
    val safeUrl = avatarUrl.toSafeAvatarUrl()

    if (safeUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(safeUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.homme),
            placeholder = painterResource(id = R.drawable.homme)
        )
    } else {
        // Show initials if no avatar URL
        Box(modifier = modifier) {
            InitialsAvatar(
                firstName = firstName,
                lastName = lastName,
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }
    }
}

/**
 * Displays user initials in a circular background
 */
@Composable
private fun InitialsAvatar(
    firstName: String?,
    lastName: String?,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val initials = buildString {
        firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
        lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
    }.takeIf { it.isNotEmpty() } ?: "?"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

