package com.example.dam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dam.utils.AudioPlayer
import com.example.dam.utils.AudioRecorder
import kotlinx.coroutines.delay

/**
 * Composant pour afficher un message audio avec lecteur intégré
 */
@Composable
fun AudioMessageBubble(
    audioUrl: String,
    durationSeconds: Int,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    // Mettre à jour l'état de lecture
    LaunchedEffect(audioUrl) {
        isPlaying = AudioPlayer.isPlayingUrl(audioUrl)
    }

    // Mettre à jour la progression pendant la lecture
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val position = AudioPlayer.getCurrentPosition()
            currentPosition = position / 1000 // Convertir en secondes

            // Vérifier si toujours en train de jouer
            if (!AudioPlayer.isPlayingUrl(audioUrl)) {
                isPlaying = false
                currentPosition = 0
            }

            delay(100) // Mise à jour toutes les 100ms
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton Play/Pause
        IconButton(
            onClick = {
                if (isPlaying && AudioPlayer.isPlayingUrl(audioUrl)) {
                    AudioPlayer.pause()
                    isPlaying = false
                } else {
                    AudioPlayer.play(
                        url = audioUrl,
                        context = context,
                        onCompletion = {
                            isPlaying = false
                            currentPosition = 0
                        },
                        onError = { error ->
                            showError = true
                            isPlaying = false
                        }
                    )
                    isPlaying = true
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isMe) Color(0xFF25D366) else Color(0xFF056162),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isPlaying && AudioPlayer.isPlayingUrl(audioUrl)) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Waveform simulée (barres verticales)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Générer 20 barres avec hauteurs aléatoires mais cohérentes
            val waveformHeights = remember {
                List(20) { (8..24).random().dp }
            }

            waveformHeights.forEachIndexed { index, height ->
                val progress = if (durationSeconds > 0) {
                    currentPosition.toFloat() / durationSeconds.toFloat()
                } else {
                    0f
                }
                val barProgress = index.toFloat() / waveformHeights.size
                val isActive = barProgress <= progress && isPlaying

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height)
                        .background(
                            color = if (isActive) {
                                Color(0xFF25D366)
                            } else {
                                Color.White.copy(alpha = 0.5f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        // Durée
        Text(
            text = if (isPlaying && AudioPlayer.isPlayingUrl(audioUrl)) {
                com.example.dam.utils.AudioRecorder.formatDuration(currentPosition)
            } else {
                com.example.dam.utils.AudioRecorder.formatDuration(durationSeconds)
            },
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }

    // Snackbar d'erreur si nécessaire
    if (showError) {
        LaunchedEffect(showError) {
            delay(3000)
            showError = false
        }
    }
}

/**
 * Composant pour afficher l'enregistrement en cours
 */
@Composable
fun RecordingIndicator(
    duration: Int,
    onCancel: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xFF1a3a2e),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Indicateur visuel d'enregistrement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Point rouge clignotant
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, CircleShape)
                )

                Text(
                    text = "Enregistrement...",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = com.example.dam.utils.AudioRecorder.formatDuration(duration),
                    color = Color(0xFF25D366),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Boutons d'action
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bouton Annuler
                TextButton(onClick = onCancel) {
                    Text("Annuler", color = Color.White.copy(alpha = 0.7f))
                }

                // Bouton Envoyer
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366)
                    )
                ) {
                    Text("Envoyer")
                }
            }
        }
    }
}

