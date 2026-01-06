package com.example.dam.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.io.IOException

/**
 * Utilitaire pour lire des messages audio dans le chat
 * Gère un seul MediaPlayer global pour éviter les conflits
 */
object AudioPlayer {

    private const val TAG = "AudioPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    // États publics pour l'UI
    private val _isPlaying = androidx.compose.runtime.mutableStateOf(false)
    val isPlaying: androidx.compose.runtime.State<Boolean> = _isPlaying

    private val _currentPosition = androidx.compose.runtime.mutableStateOf(0)
    val currentPosition: androidx.compose.runtime.State<Int> = _currentPosition

    private val _duration = androidx.compose.runtime.mutableStateOf(0)
    val duration: androidx.compose.runtime.State<Int> = _duration

    /**
     * Lire un fichier audio depuis une URL
     * @param url URL Cloudinary du fichier audio
     * @param onCompletion Callback appelé quand la lecture se termine
     * @param onError Callback appelé en cas d'erreur
     */
    fun play(
        url: String,
        context: Context,
        onCompletion: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            // Si on joue déjà le même audio, on met en pause
            if (currentUrl == url && _isPlaying.value) {
                pause()
                return
            }

            // Si on joue un autre audio, on arrête le précédent
            if (currentUrl != null && currentUrl != url) {
                stop()
            }

            this.onCompletionListener = onCompletion
            this.onErrorListener = onError

            // Si c'est la même URL et qu'on est en pause, on reprend
            if (currentUrl == url && mediaPlayer != null) {
                resume()
                return
            }

            Log.d(TAG, "▶️ Lecture audio: $url")

            // Créer un nouveau MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    Log.d(TAG, "✅ MediaPlayer prêt")
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    currentUrl = url
                    startProgressUpdater()
                }

                setOnCompletionListener {
                    Log.d(TAG, "✅ Lecture terminée")
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    onCompletionListener?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "❌ Erreur MediaPlayer: what=$what, extra=$extra")
                    val errorMsg = "Erreur de lecture audio (code: $what)"
                    _isPlaying.value = false
                    onErrorListener?.invoke(errorMsg)
                    true
                }

                prepareAsync()
            }

        } catch (e: IOException) {
            Log.e(TAG, "❌ Erreur IOException: ${e.message}", e)
            onError?.invoke("Impossible de lire l'audio: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lecture audio: ${e.message}", e)
            onError?.invoke("Erreur: ${e.message}")
        }
    }

    /**
     * Mettre en pause
     */
    fun pause() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                    Log.d(TAG, "⏸️ Pause")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur pause: ${e.message}")
        }
    }

    /**
     * Reprendre la lecture
     */
    fun resume() {
        try {
            mediaPlayer?.let { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                    _isPlaying.value = true
                    startProgressUpdater()
                    Log.d(TAG, "▶️ Reprise")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur resume: ${e.message}")
        }
    }

    /**
     * Arrêter complètement la lecture
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            currentUrl = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
            Log.d(TAG, "⏹️ Arrêt")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur stop: ${e.message}")
        }
    }

    /**
     * Se déplacer à une position spécifique (en millisecondes)
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            Log.e(TAG, "Erreur seekTo: ${e.message}")
        }
    }

    /**
     * Obtenir la position actuelle
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Obtenir la durée totale
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Vérifier si un audio est en cours de lecture
     */
    fun isPlayingUrl(url: String): Boolean {
        return currentUrl == url && _isPlaying.value
    }

    /**
     * Mettre à jour la progression régulièrement
     */
    private fun startProgressUpdater() {
        // Cette fonction sera appelée par un coroutine dans le ViewModel
        // ou directement dans le Composable avec LaunchedEffect
    }

    /**
     * Nettoyer les ressources (à appeler dans onCleared du ViewModel)
     */
    fun release() {
        stop()
    }
}

