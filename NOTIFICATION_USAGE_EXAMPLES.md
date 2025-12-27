# 🎓 Exemples d'Utilisation du Système de Notification

## 📱 Ajouter un Bouton Notifications dans la TopBar

### Exemple avec Badge (Compteur)

```kotlin
// Dans votre HomeScreen ou tout écran avec TopBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val context = LocalContext.current
    
    // Charger le compteur au démarrage
    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accueil") },
                actions = {
                    // Bouton Notifications avec badge
                    IconButton(onClick = {
                        navController.navigate(NavigationRoutes.NOTIFICATIONS)
                    }) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Votre contenu
    }
}
```

---

## 🔔 Test Manuel : Envoyer une Notification depuis Postman

### Prérequis
1. Backend en cours d'exécution : `https://dam-4sim2.onrender.com/`
2. Token JWT récupéré après login

### Endpoint de Test (si disponible)
```
POST https://dam-4sim2.onrender.com/notifications/test
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN
  Content-Type: application/json

Body:
{
  "title": "Test Notification",
  "body": "Ceci est une notification de test",
  "type": "test"
}
```

### Vérification
1. Dans votre app Android, attendez 15 secondes maximum
2. Une notification doit apparaître
3. Vérifiez Logcat :
   ```
   📡 Polling notifications...
   📬 1 new notification(s)
   ✅ Notification displayed: Test Notification
   ```

---

## 🛠️ Personnaliser l'Intervalle de Polling

### Changer l'Intervalle dans LoginScreen

```kotlin
// Dans LoginScreen.kt, ligne ~135

// Option 1 : Polling rapide (10 secondes) - Plus de batterie
NotificationPollingService.startPolling(
    context = context,
    intervalSeconds = 10
)

// Option 2 : Polling équilibré (15 secondes) - Recommandé ⭐
NotificationPollingService.startPolling(
    context = context,
    intervalSeconds = 15
)

// Option 3 : Polling économique (30 secondes) - Moins de batterie
NotificationPollingService.startPolling(
    context = context,
    intervalSeconds = 30
)

// Option 4 : Polling lent (60 secondes) - Très économique
NotificationPollingService.startPolling(
    context = context,
    intervalSeconds = 60
)
```

---

## 🎨 Personnaliser les Icônes de Notification

### Dans NotificationHelper.kt

```kotlin
private fun getNotificationIcon(type: NotificationType): Int {
    return when (type) {
        NotificationType.NEW_PUBLICATION -> R.drawable.ic_publication  // Votre icône
        NotificationType.CHAT_MESSAGE -> R.drawable.ic_message
        NotificationType.NEW_SORTIE -> R.drawable.ic_adventure
        NotificationType.PARTICIPATION_ACCEPTED -> R.drawable.ic_check
        NotificationType.PARTICIPATION_REJECTED -> R.drawable.ic_cancel
        NotificationType.TEST -> R.drawable.ic_test
    }
}
```

**Note :** Ajoutez vos propres icônes dans `res/drawable/`

---

## 🔊 Ajouter Son et Vibration

### Dans NotificationHelper.kt, méthode showNotification()

```kotlin
val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(getNotificationIcon(notification.type))
    .setContentTitle(notification.title)
    .setContentText(notification.body)
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .setContentIntent(pendingIntent)
    .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
    
    // ✅ AJOUTER SON ET VIBRATION
    .setDefaults(NotificationCompat.DEFAULT_ALL)  // Son + Vibration + Lumière
    
    // OU personnaliser :
    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
    .setVibrate(longArrayOf(0, 500, 200, 500))  // Pattern de vibration
    .setLights(Color.BLUE, 1000, 500)  // Lumière bleue
```

---

## 🎭 Ajouter des Actions Rapides

### Exemple : Bouton "Voir" et "Marquer comme lu"

```kotlin
// Dans NotificationHelper.kt, méthode showNotification()

// Intent pour marquer comme lu
val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
    action = "MARK_READ"
    putExtra("notification_id", notification.id)
}
val markReadPendingIntent = PendingIntent.getBroadcast(
    context,
    notification.id.hashCode() + 1,
    markReadIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    // ... configuration existante ...
    
    // ✅ AJOUTER ACTIONS
    .addAction(
        R.drawable.ic_check,
        "Marquer lu",
        markReadPendingIntent
    )
    .addAction(
        R.drawable.ic_open,
        "Voir",
        pendingIntent
    )
```

**Note :** Créez `NotificationActionReceiver` pour gérer les actions

---

## 🔄 Refresh Manuel des Notifications

### Exemple avec Pull-to-Refresh

```kotlin
@Composable
fun MyNotificationsScreen(navController: NavController) {
    val viewModel: NotificationViewModel = viewModel()
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Swipe to refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refreshNotifications(context)
            delay(500)
            isRefreshing = false
        }
    }
    
    // Votre UI avec SwipeRefresh ou équivalent
}
```

---

## 📊 Afficher le Compteur dans le TabBar

### Exemple

```kotlin
@Composable
fun MyTabBar(navController: NavController) {
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount(context)
    }
    
    BottomNavigation {
        BottomNavigationItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text("$unreadCount") }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, "Notifications")
                }
            },
            label = { Text("Notifications") },
            selected = false,
            onClick = { navController.navigate(NavigationRoutes.NOTIFICATIONS) }
        )
    }
}
```

---

## 🧹 Nettoyer les Notifications au Logout

### Dans votre LogoutHandler

```kotlin
fun logout(context: Context, navController: NavController) {
    // Arrêter le polling
    NotificationPollingService.stopPolling()
    
    // Effacer toutes les notifications affichées
    NotificationHelper.cancelAllNotifications(context)
    
    // Effacer le token
    UserPreferences.clear(context)
    
    // Naviguer vers login
    navController.navigate(NavigationRoutes.LOGIN) {
        popUpTo(0) { inclusive = true }
    }
}
```

---

## 🎯 Créer une Route Manquante (Exemple : Publication Detail)

### Si vous n'avez pas encore d'écran PublicationDetailScreen

```kotlin
// Dans MainActivity.kt, NavigationGraph

composable(
    route = "publicationDetail/{publicationId}",
    arguments = listOf(
        navArgument("publicationId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val publicationId = backStackEntry.arguments?.getString("publicationId") ?: ""
    PublicationDetailScreen(
        navController = navController,
        publicationId = publicationId
    )
}
```

---

## 🔔 Tester avec une Notification de Test Backend

### Créer un Endpoint de Test (Backend - NestJS)

```typescript
// Dans votre NotificationController (backend)

@Post('test')
async sendTestNotification(@CurrentUser() user) {
  return this.notificationService.create({
    userId: user.id,
    title: '🧪 Test Notification',
    body: 'Ceci est une notification de test envoyée à ' + new Date().toLocaleTimeString(),
    type: 'test',
    data: {
      timestamp: new Date().toISOString()
    }
  });
}
```

### Appeler depuis Postman

```
POST https://dam-4sim2.onrender.com/notifications/test
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 📱 Tester sur Appareil Réel

### Étapes
1. **Build l'APK**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Installer sur téléphone**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Vérifier les logs**
   ```bash
   adb logcat | grep "NotificationPolling"
   ```

4. **Tester**
   - Ouvrir l'app et se connecter
   - Créer une publication/message depuis un autre appareil
   - Attendre 15 secondes
   - Notification devrait apparaître

---

## 🐛 Debug : Voir les Notifications en Temps Réel

### Logcat Filter

```
NotificationPolling|NotificationRepository|NotificationHelper
```

### Logs Attendus (Normal)

```
🚀 User logged in, starting notification polling
🔄 Starting notification polling (every 15s)
📡 Polling notifications...
✅ No new notifications
📡 Polling notifications...
✅ No new notifications
```

### Logs Attendus (Notification Reçue)

```
📡 Polling notifications...
📬 3 new notification(s)
   [0] CHAT_MESSAGE - Jane Smith • Weekend Chat
   [1] NEW_PUBLICATION - John Doe a publié
   [2] PARTICIPATION_ACCEPTED - ✅ Participation acceptée
✅ Notification displayed: Jane Smith • Weekend Chat
📝 Marking notification as read: 675d1234...
✅ Notification marquée comme lue
```

---

## 🎉 Félicitations !

Vous avez maintenant toutes les connaissances pour utiliser et personnaliser le système de notification !

**Questions fréquentes :**
- Intervalle recommandé : **15 secondes** ⭐
- Permissions : Automatiques (Android 13+ demandée au lancement)
- Backend URL : Déjà configurée dans `RetrofitInstance.kt`
- Deep links : Déjà configurés dans `MainActivity.kt`

**Prêt à tester ! 🚀**

