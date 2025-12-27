package com.example.dam.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dam.MainActivity
import com.example.dam.R
import com.example.dam.models.Notification
import com.example.dam.models.NotificationType

/**
 * Gestionnaire des notifications locales Android
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "app_notifications"
    private const val CHANNEL_NAME = "Notifications de l'application"
    private const val CHANNEL_DESCRIPTION = "Notifications pour les messages, publications et sorties"

    /**
     * Crée le canal de notification (requis pour Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ Notification channel created")
        }
    }

    /**
     * Vérifie si les permissions de notification sont accordées (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pas de permission requise avant Android 13
        }
    }

    /**
     * Affiche une notification locale Android
     * @param context Context de l'application
     * @param notification Objet notification reçu du backend
     */
    fun showNotification(context: Context, notification: Notification) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "⚠️ Notification permission not granted, skipping notification")
            return
        }

        try {
            // Créer l'intent pour ouvrir l'app avec deep link
            val intent = createDeepLinkIntent(context, notification)
            val pendingIntent = PendingIntent.getActivity(
                context,
                notification.id.hashCode(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Construire la notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(getNotificationIcon(notification.type))
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Ferme la notification au clic
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))

            // Afficher la notification
            with(NotificationManagerCompat.from(context)) {
                notify(notification.id.hashCode(), builder.build())
            }

            Log.d(TAG, "✅ Notification displayed: ${notification.title}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing notification", e)
        }
    }

    /**
     * Crée un intent avec les données pour le deep linking
     */
    private fun createDeepLinkIntent(context: Context, notification: Notification): Intent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Ajouter les données de la notification
            putExtra("notification_id", notification.id)
            putExtra("notification_type", notification.type.name)

            // Ajouter les données spécifiques selon le type
            when (notification.type) {
                NotificationType.NEW_PUBLICATION -> {
                    putExtra("publicationId", notification.data.publicationId)
                    putExtra("authorId", notification.data.authorId)
                }
                NotificationType.CHAT_MESSAGE -> {
                    putExtra("chatId", notification.data.chatId)
                    putExtra("sortieId", notification.data.sortieId)
                    putExtra("senderId", notification.data.senderId)
                }
                NotificationType.NEW_SORTIE -> {
                    putExtra("sortieId", notification.data.sortieId)
                    putExtra("creatorId", notification.data.creatorId)
                }
                NotificationType.PARTICIPATION_ACCEPTED,
                NotificationType.PARTICIPATION_REJECTED -> {
                    putExtra("sortieId", notification.data.sortieId)
                    putExtra("participationId", notification.data.participationId)
                }
                NotificationType.TEST -> {
                    // Pas de deep link pour les notifications de test
                }
            }
        }

        Log.d(TAG, "📲 Created deep link intent for type: ${notification.type}")
        return intent
    }

    /**
     * Retourne l'icône appropriée selon le type de notification
     */
    private fun getNotificationIcon(type: NotificationType): Int {
        return when (type) {
            NotificationType.NEW_PUBLICATION -> android.R.drawable.ic_menu_gallery
            NotificationType.CHAT_MESSAGE -> android.R.drawable.ic_dialog_email
            NotificationType.NEW_SORTIE -> android.R.drawable.ic_menu_compass
            NotificationType.PARTICIPATION_ACCEPTED -> android.R.drawable.ic_menu_add
            NotificationType.PARTICIPATION_REJECTED -> android.R.drawable.ic_menu_close_clear_cancel
            NotificationType.TEST -> android.R.drawable.ic_dialog_info
        }
    }

    /**
     * Annule toutes les notifications affichées
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        Log.d(TAG, "🗑️ All notifications cancelled")
    }
}

