// Screens/EligibleRatingPopup.kt
package com.example.dam.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dam.models.EligibleSortieForRating
import com.example.dam.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EligibleSortiesRatingDialog(
    eligibleSorties: List<EligibleSortieForRating>,
    onDismiss: () -> Unit,
    onRateSortie: (String) -> Unit,
    onRateLater: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentSortie = eligibleSorties.getOrNull(currentIndex)

    AnimatedVisibility(
        visible = currentSortie != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Dialog(
            onDismissRequest = { /* Prevent dismissing by clicking outside */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), // Add padding from edges
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = CardDark,
                    shadowElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenAccent.copy(0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CardDark,
                                        CardDark.copy(alpha = 0.95f),
                                        BackgroundDark
                                    )
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ✅ Header with icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            GreenAccent.copy(0.3f),
                                            GreenAccent.copy(0.1f)
                                        )
                                    )
                                )
                                .border(2.dp, GreenAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RateReview,
                                contentDescription = "Rate",
                                tint = GreenAccent,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Rate Your Experience! ⭐",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You have ${eligibleSorties.size} completed ${if (eligibleSorties.size == 1) "adventure" else "adventures"} waiting for your feedback",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // ✅ Current sortie card
                        currentSortie?.let { sortie ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CardGlass,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                shadowElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Type badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (sortie.camping) SuccessGreen.copy(0.2f) else TealAccent.copy(0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (sortie.camping) Icons.Default.Terrain else Icons.Default.Hiking,
                                                contentDescription = "Type",
                                                tint = if (sortie.camping) SuccessGreen else TealAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (sortie.camping) "Camping" else "Adventure",
                                                color = if (sortie.camping) SuccessGreen else TealAccent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Title
                                    Text(
                                        text = sortie.title,
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 24.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Date info
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = "Date",
                                            tint = GreenAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Completed: ${formatEligibleDate(sortie.eligibleDate)}",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ✅ Progress indicator
                        if (eligibleSorties.size > 1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                eligibleSorties.forEachIndexed { index, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == currentIndex) 10.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == currentIndex) GreenAccent
                                                else TextTertiary.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // ✅ Action buttons (Icon-only for better spacing)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous button (only show if not first sortie)
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentIndex-- },
                                    modifier = Modifier.size(56.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = GreenAccent
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenAccent),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Previous",
                                        modifier = Modifier.size(24.dp),
                                        tint = GreenAccent
                                    )
                                }
                            }

                            // Skip/Later button
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex < eligibleSorties.size - 1) {
                                        currentIndex++
                                    } else {
                                        onRateLater()
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderColor),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentIndex < eligibleSorties.size - 1)
                                        Icons.Default.ArrowForward
                                    else
                                        Icons.Default.Close,
                                    contentDescription = if (currentIndex < eligibleSorties.size - 1) "Skip" else "Later",
                                    modifier = Modifier.size(24.dp),
                                    tint = TextSecondary
                                )
                            }

                            // Rate Now button (larger, primary action)
                            Button(
                                onClick = {
                                    currentSortie?.let {
                                        onRateSortie(it.sortieId)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Rate",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Rate Now",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ✅ Labels row below buttons (for clarity)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentIndex > 0) {
                                Text(
                                    text = "Previous",
                                    color = GreenAccent.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(56.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                text = if (currentIndex < eligibleSorties.size - 1) "Skip" else "Later",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier.width(56.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // ✅ Close button (small, top-right style)
                        if (currentIndex == eligibleSorties.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = onRateLater,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "I'll rate them later",
                                    color = TextTertiary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to format the eligible date
 */
private fun formatEligibleDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}