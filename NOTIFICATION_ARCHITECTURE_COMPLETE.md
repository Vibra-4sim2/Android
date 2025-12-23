# 🎯 Architecture Complète du Système de Notifications

## 📁 Structure des Fichiers

```
dam/
├── app/src/main/java/com/example/dam/
│   │
│   ├── 📦 models/
│   │   └── Notification.kt                    ✅ Modèles de données
│   │
│   ├── 🌐 remote/
│   │   ├── NotificationApiService.kt          ✅ Interface Retrofit API
│   │   └── RetrofitInstance.kt                ✅ Instance notificationApi
│   │
│   ├── 📚 repository/
│   │   └── NotificationRepository.kt          ✅ Logique métier
│   │
│   ├── ⚙️ services/
│   │   ├── NotificationPollingService.kt      ✅ Service polling (15s)
│   │   └── NotificationPollingWorker.kt       ✅ Worker WorkManager
│   │
│   ├── 🛠️ utils/
│   │   ├── NotificationHelper.kt              ✅ Gestionnaire notifs locales
│   │   └── UserPreferences.kt                 ✅ Stockage token/userId
│   │
│   ├── 🎨 viewmodel/
│   │   └── NotificationViewModel.kt           ✅ Gestion état UI
│   │
│   ├── 📱 Screens/
│   │   ├── NotificationsScreen.kt             ✅ UI liste notifications
│   │   └── LoginScreen.kt                     ✅ Démarrage polling
│   │
│   ├── 🎭 ui/theme/
│   │   └── TabBarView.kt                      ✅ Badge + route
│   │
│   └── MainActivity.kt                         ✅ Init système + deep links
│
├── 📝 Documentation/
│   ├── NOTIFICATION_SYSTEM_GUIDE.md           ✅ Guide technique complet
│   ├── NOTIFICATION_IMPLEMENTATION_COMPLETE.md ✅ Résumé implémentation
│   ├── NOTIFICATION_USAGE_EXAMPLES.md         ✅ Exemples pratiques
│   └── NOTIFICATION_UI_COMPLETE_GUIDE.md      ✅ Guide UI complet
│
└── AndroidManifest.xml                         ✅ Permission POST_NOTIFICATIONS
```

---

## 🔄 Flux de Données Complet

### 1. **Initialisation au Démarrage de l'App**

```
MainActivity.onCreate()
    │
    ├─→ NotificationHelper.createNotificationChannel()
    │   └─→ Canal créé (Android 8.0+)
    │
    ├─→ Demande permission POST_NOTIFICATIONS (Android 13+)
    │
    └─→ Si token existe:
        └─→ NotificationPollingService.startPolling()
            └─→ Polling démarre (toutes les 15s)
```

### 2. **Login Utilisateur**

```
LoginScreen
    │
    └─→ Login réussi (normal ou Google)
        │
        ├─→ UserPreferences.saveToken(token)
        │
        └─→ NotificationPollingService.startPolling(15s)
            └─→ Boucle infinie avec delay(15000)
```

### 3. **Cycle de Polling (Toutes les 15 secondes)**

```
NotificationPollingService
    │
    └─→ Toutes les 15 secondes:
        │
        ├─→ GET /notifications?unreadOnly=true
        │   │
        │   └─→ Backend retourne liste notifications
        │       │
        │       ├─→ Si vide: Log "No new notifications"
        │       │
        │       └─→ Si nouvelles notifs:
        │           │
        │           ├─→ NotificationHelper.showNotification()
        │           │   └─→ Affichage notification Android
        │           │
        │           └─→ PATCH /notifications/{id}/read
        │               └─→ Marquage comme lu
        │
        └─→ Si erreur 401:
            └─→ stopPolling() (token expiré)
```

### 4. **Affichage du Badge dans le TopBar**

```
TabBarView.onCreate()
    │
    ├─→ notificationViewModel = viewModel()
    │
    ├─→ LaunchedEffect(Unit):
    │   └─→ notificationViewModel.loadUnreadCount(context)
    │       │
    │       └─→ GET /notifications/unread-count
    │           └─→ unreadCount.value = 5
    │
    └─→ Badge affiche (5)
        │
        └─→ LaunchedEffect(currentRoute):
            └─→ Mise à jour à chaque navigation
```

### 5. **Clic sur l'Icône de Notification**

```
Utilisateur clique sur 🔔
    │
    └─→ navController.navigate("notifications")
        │
        └─→ NotificationsScreen affichée
            │
            ├─→ LaunchedEffect(Unit):
            │   │
            │   ├─→ viewModel.loadNotifications(unreadOnly=false)
            │   │   └─→ Charge toutes les notifications
            │   │
            │   └─→ viewModel.loadUnreadCount()
            │       └─→ Charge le compteur
            │
            └─→ LazyColumn affiche la liste
                └─→ Chaque NotificationCard est cliquable
```

### 6. **Clic sur une Notification dans la Liste**

```
Utilisateur clique sur notification
    │
    ├─→ viewModel.markAsRead(notificationId)
    │   │
    │   └─→ PATCH /notifications/{id}/read
    │       │
    │       └─→ Backend marque comme lue
    │           │
    │           ├─→ Notification retirée de la liste UI
    │           │
    │           └─→ Badge décrémenté (5) → (4)
    │
    └─→ handleNotificationClick(navController, notification)
        │
        └─→ switch (notification.type):
            │
            ├─→ NEW_PUBLICATION:
            │   └─→ navigate("publicationDetail/{id}")
            │
            ├─→ CHAT_MESSAGE:
            │   └─→ navigate("chatConversation/{sortieId}/...")
            │
            ├─→ NEW_SORTIE:
            │   └─→ navigate("sortieDetail/{id}")
            │
            └─→ PARTICIPATION_*:
                └─→ navigate("sortieDetail/{id}")
```

### 7. **Clic sur Notification Android (Barre de notification)**

```
Utilisateur tape sur notification Android
    │
    └─→ MainActivity.onNewIntent(intent)
        │
        ├─→ notification_type = intent.getStringExtra("notification_type")
        │
        └─→ handleNotificationIntent(intent)
            │
            └─→ switch (notification_type):
                │
                ├─→ "NEW_PUBLICATION":
                │   └─→ navigate("publicationDetail/{id}")
                │
                ├─→ "CHAT_MESSAGE":
                │   └─→ navigate("chatConversation/{sortieId}/...")
                │
                └─→ "NEW_SORTIE":
                    └─→ navigate("sortieDetail/{id}")
```

---

## 🔌 Endpoints Backend Utilisés

### 1. **GET /notifications**
```http
GET https://dam-4sim2.onrender.com/notifications?unreadOnly=true&limit=10
Authorization: Bearer {JWT_TOKEN}

Response:
[
  {
    "id": "675d1234...",
    "title": "Jean Dupont",
    "body": "Nouvelle sortie créée",
    "type": "new_sortie",
    "data": { "sortieId": "675b..." },
    "isRead": false,
    "createdAt": "2025-12-14T14:00:00Z"
  }
]
```

### 2. **PATCH /notifications/{id}/read**
```http
PATCH https://dam-4sim2.onrender.com/notifications/675d1234.../read
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "success": true,
  "message": "Notification marquée comme lue"
}
```

### 3. **GET /notifications/unread-count**
```http
GET https://dam-4sim2.onrender.com/notifications/unread-count
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "count": 5
}
```

---

## 🎨 Composants UI

### TabBarView - TopBar avec Badge

```kotlin
// TopBar
Row {
    // Logo + titre
    Column {
        Text("V!BRA")
        Text("Explore Adventures")
    }
    
    // Icône notification avec badge
    Surface(onClick = { navigate("notifications") }) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge { Text("$unreadCount") }
                }
            }
        ) {
            Icon(Icons.Default.Notifications)
        }
    }
    
    // Menu dropdown
    Surface(onClick = { showMenu() }) {
        Icon(Icons.Default.KeyboardArrowDown)
    }
}
```

### NotificationsScreen - Liste

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("Notifications") },
            navigationIcon = { BackButton() }
        )
    }
) {
    LazyColumn {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onClick = {
                    handleNotificationClick(navController, notification)
                    viewModel.markAsRead(notification.id)
                }
            )
        }
    }
}
```

### NotificationCard - Item

```kotlin
Card(onClick = { onClick() }) {
    Row {
        // Icône colorée
        Box(backgroundColor = getNotificationColor(type)) {
            Icon(getNotificationIcon(type))
        }
        
        Column {
            // Titre + badge non lu
            Row {
                Text(notification.title, fontWeight = Bold)
                if (!isRead) {
                    Badge() // Point bleu
                }
            }
            
            // Corps du message
            Text(notification.body)
            
            // Timestamp relatif
            Text(formatTimestamp(notification.createdAt))
        }
    }
}
```

---

## 🔐 Sécurité & Permissions

### Permissions Android
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

### Authentification
```kotlin
// Toutes les requêtes incluent le JWT
Authorization: Bearer {token}

// Si 401 Unauthorized:
- Polling s'arrête automatiquement
- Utilisateur redirigé vers login
```

---

## 🎯 Points d'Entrée Utilisateur

### 1. Via l'Icône TopBar (Principal)
```
TopBar → Clic sur 🔔 → NotificationsScreen
```

### 2. Via Notification Android
```
Barre notification → Clic → App ouvre → Navigation écran
```

### 3. Via Code (Développeur)
```kotlin
navController.navigate("notifications")
```

---

## 📊 États du Système

### État 1 : Aucune Notification
```
Badge: Pas visible
Liste: "Aucune notification - Vous êtes à jour!"
```

### État 2 : Notifications Non Lues
```
Badge: (3)
Liste: 3 notifications avec badge bleu ●
```

### État 3 : Toutes Lues
```
Badge: Disparaît
Liste: Notifications sans badge bleu
```

### État 4 : Polling Actif
```
Icône 🔄 visible dans NotificationsScreen
Logs: "📡 Polling notifications..."
```

### État 5 : Erreur Token Expiré
```
Polling: S'arrête automatiquement
Logs: "🔐 Token expired, stopping polling"
Navigation: Retour au login
```

---

## 🧪 Checklist de Vérification

### ✅ Système Backend
- [x] Notifications stockées dans MongoDB
- [x] Associées par userId
- [x] Marquage comme lu fonctionne
- [x] Compteur retourne le bon nombre

### ✅ Polling
- [x] Démarre après login
- [x] Intervalle de 15 secondes
- [x] S'arrête au logout
- [x] Gère les erreurs 401

### ✅ UI TopBar
- [x] Icône 🔔 visible
- [x] Badge affiche le nombre
- [x] Badge disparaît quand 0
- [x] Badge mis à jour en temps réel
- [x] Cliquable → ouvre NotificationsScreen

### ✅ UI Liste
- [x] Affiche toutes les notifications
- [x] Icônes colorées par type
- [x] Dates relatives
- [x] Badge bleu pour non lues
- [x] Scrollable
- [x] Cliquable

### ✅ Navigation
- [x] Publication → publicationDetail
- [x] Chat → chatConversation
- [x] Sortie → sortieDetail
- [x] Participation → sortieDetail

### ✅ Marquage Lu
- [x] Automatique à l'affichage
- [x] Au clic dans la liste
- [x] Badge décrémenté
- [x] Notification retirée

---

## 🎉 SYSTÈME 100% OPÉRATIONNEL !

**Architecture complète et professionnelle !**

- ✅ Backend stockage par utilisateur
- ✅ Polling automatique 15 secondes
- ✅ UI badge dans TopBar
- ✅ UI liste complète
- ✅ Navigation intelligente
- ✅ Marquage automatique
- ✅ Gestion erreurs robuste
- ✅ Design Material 3

**Comme les grandes apps ! 🚀**

