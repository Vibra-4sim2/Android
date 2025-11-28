package com.example.dam.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Utilitaire pour enregistrer des messages audio
 * Format : AAC (M4A) avec compression automatique
 */
class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

    companion object {
        private const val TAG = "AudioRecorder"
        private const val MAX_DURATION_MS = 120000 // 2 minutes max
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000 // 128 kbps

        /**
         * Valider un fichier audio avant l'upload
         */
        fun validateAudioFile(file: File): kotlin.Result<Boolean> {
            return when {
                !file.exists() -> {
                    kotlin.Result.failure(Exception("Le fichier audio n'existe pas"))
                }
                file.length() == 0L -> {
                    kotlin.Result.failure(Exception("Le fichier audio est vide"))
                }
                file.length() > 10 * 1024 * 1024 -> { // 10 MB max
                    kotlin.Result.failure(Exception("Le fichier audio est trop grand (max 10 MB)"))
                }
                !file.extension.lowercase().matches(Regex("m4a|aac|mp3|wav")) -> {
                    kotlin.Result.failure(Exception("Format audio non supporté"))
                }
                else -> {
                    kotlin.Result.success(true)
                }
            }
        }

        /**
         * Formater la durée en format MM:SS
         */
        fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, secs)
        }
    }

    /**
     * Démarrer l'enregistrement
     * @return Résultat contenant le fichier de sortie ou une erreur
     */
    fun startRecording(): kotlin.Result<File> {
        return try {
            if (isRecording) {
                return kotlin.Result.failure(Exception("Enregistrement déjà en cours"))
            }

            // Créer un fichier temporaire pour l'enregistrement
            outputFile = File(
                context.cacheDir,
                "audio_${System.currentTimeMillis()}.m4a"
            )

            Log.d(TAG, "📁 Fichier de sortie: ${outputFile?.absolutePath}")

            // Configurer MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(SAMPLE_RATE)
                setAudioEncodingBitRate(BIT_RATE)
                setMaxDuration(MAX_DURATION_MS)
                setOutputFile(outputFile?.absolutePath)

                // Listener pour durée maximale atteinte
                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        Log.w(TAG, "⏱️ Durée maximale atteinte (2 minutes)")
                        stopRecording()
                    }
                }

                try {
                    prepare()
                    start()
                    isRecording = true
                    startTime = System.currentTimeMillis()
                    Log.d(TAG, "🎤 Enregistrement démarré")
                } catch (e: IOException) {
                    Log.e(TAG, "❌ Erreur préparation MediaRecorder: ${e.message}", e)
                    release()
                    return kotlin.Result.failure(Exception("Erreur de préparation: ${e.message}"))
                }
            }

            kotlin.Result.success(outputFile!!)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur démarrage enregistrement: ${e.message}", e)
            cleanup()
            kotlin.Result.failure(Exception("Impossible de démarrer l'enregistrement: ${e.message}"))
        }
    }

    /**
     * Arrêter l'enregistrement
     * @return Résultat contenant le fichier audio et sa durée
     */
    fun stopRecording(): kotlin.Result<AudioResult> {
        return try {
            if (!isRecording) {
                return kotlin.Result.failure(Exception("Aucun enregistrement en cours"))
            }

            val duration = (System.currentTimeMillis() - startTime) / 1000 // en secondes
            Log.d(TAG, "⏹️ Arrêt enregistrement (durée: ${duration}s)")

            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    Log.e(TAG, "⚠️ Erreur lors de l'arrêt: ${e.message}")
                    // Continue quand même pour cleanup
                }
                release()
            }

            mediaRecorder = null
            isRecording = false

            val file = outputFile
            if (file != null && file.exists() && file.length() > 0) {
                Log.d(TAG, "✅ Enregistrement terminé: ${file.name} (${file.length()} bytes, ${duration}s)")
                kotlin.Result.success(AudioResult(file, duration.toInt()))
            } else {
                Log.e(TAG, "❌ Fichier audio invalide ou vide")
                cleanup()
                kotlin.Result.failure(Exception("Fichier audio invalide"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur arrêt enregistrement: ${e.message}", e)
            cleanup()
            kotlin.Result.failure(Exception("Erreur lors de l'arrêt: ${e.message}"))
        }
    }

    /**
     * Annuler l'enregistrement en cours
     */
    fun cancelRecording() {
        Log.d(TAG, "🚫 Annulation enregistrement")
        cleanup()
    }

    /**
     * Vérifier si un enregistrement est en cours
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Obtenir la durée actuelle de l'enregistrement (en secondes)
     */
    fun getCurrentDuration(): Int {
        return if (isRecording) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }
    }

    /**
     * Nettoyer les ressources
     */
    private fun cleanup() {
        try {
            mediaRecorder?.apply {
                if (isRecording) {
                    try {
                        stop()
                    } catch (e: RuntimeException) {
                        Log.w(TAG, "Erreur stop pendant cleanup: ${e.message}")
                    }
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur cleanup: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false

            // Supprimer le fichier temporaire si annulé
            outputFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "🗑️ Fichier temporaire supprimé")
                }
            }
            outputFile = null
        }
    }

    /**
     * Classe de résultat contenant le fichier audio et sa durée
     */
    data class AudioResult(
        val file: File,
        val durationSeconds: Int
    )
}

