# 🔔 Guide d'Implémentation du Système de Notification par Polling

## ✅ Ce qui a été implémenté

### 1. **Modèles de Données** (`models/Notification.kt`)
- `Notification` : Modèle pour les notifications reçues du backend
- `NotificationData` : Données pour le deep linking
- `NotificationType` : Enum pour les types de notifications (NEW_PUBLICATION, CHAT_MESSAGE, NEW_SORTIE, etc.)
- `UnreadCountResponse` : Réponse du compteur de notifications non lues
- `MarkAsReadResponse` : Réponse du marquage comme lu

### 2. **Service API** (`remote/NotificationApiService.kt`)
Interface Retrofit avec les endpoints :
- `GET /notifications` - Récupérer les notifications
- `PATCH /notifications/{id}/read` - Marquer comme lue
- `GET /notifications/unread-count` - Obtenir le compteur

### 3. **Repository** (`repository/NotificationRepository.kt`)
Gestion de la logique métier :
- Polling des notifications
- Marquage comme lu
- Récupération du compteur de non lues
- Gestion des erreurs et logs détaillés

### 4. **Service de Polling** (`services/NotificationPollingService.kt`)
Service léger avec Kotlin Coroutines :
- **Intervalle personnalisable** (par défaut 15 secondes)
- Gestion automatique du cycle de vie
- Arrêt automatique si token expiré
- Alternative à WorkManager (qui a une limite de 15 minutes)

### 5. **Worker en Arrière-Plan** (`services/NotificationPollingWorker.kt`)
Alternative avec WorkManager pour polling périodique :
- **Minimum 15 minutes** (limitation Android)
- Gestion des contraintes réseau
- Retry automatique en cas d'échec

### 6. **Gestionnaire de Notifications Locales** (`utils/NotificationHelper.kt`)
- Création du canal de notification (Android 8.0+)
- Affichage de notifications locales avec icônes personnalisées
- Gestion des deep links
- Vérification des permissions (Android 13+)

### 7. **ViewModel** (`viewmodel/NotificationViewModel.kt`)
- Gestion de l'état des notifications
- Démarrage/arrêt du polling
- Chargement des notifications pour l'UI
- Compteur de notifications non lues
- Refresh manuel

### 8. **Écran UI** (`Screens/NotificationsScreen.kt`)
- Liste des notifications avec design Material 3
- Indicateur de non lu
- Icônes colorées par type
- Timestamps formatés
- Navigation au clic vers les écrans appropriés
- État vide et erreur

### 9. **Intégration MainActivity**
- Initialisation automatique au démarrage
- Demande de permission Android 13+
- Gestion des deep links depuis les notifications
- Arrêt du polling à la fermeture

### 10. **Intégration LoginScreen**
- **Démarrage automatique** du polling après login réussi (normal ou Google)
- Intervalle de 15 secondes configuré

---

## 🚀 Comment ça Fonctionne

### Flux Complet

1. **Login** → Token sauvegardé → Polling démarre automatiquement
2. **Polling** (toutes les 15s) → Backend vérifie les nouvelles notifications
3. **Notification reçue** → Affichée comme notification Android locale
4. **Clic sur notification** → App s'ouvre avec deep link vers l'écran approprié
5. **Notification marquée comme lue** → Ne réapparaît plus

### Types de Notifications Supportés

| Type | Description | Navigation |
|------|-------------|------------|
| `NEW_PUBLICATION` | Publication créée | Détail publication |
| `CHAT_MESSAGE` | Message dans un chat | Écran de chat |
| `NEW_SORTIE` | Sortie créée | Détail sortie |
| `PARTICIPATION_ACCEPTED` | Participation acceptée | Détail sortie |
| `PARTICIPATION_REJECTED` | Participation refusée | Détail sortie |
| `TEST` | Notification de test | Aucune navigation |

---

## ⚙️ Configuration

### URL Backend
Configuré dans `RetrofitInstance.kt` :
```kotlin
private const val BASE_URL = "https://dam-4sim2.onrender.com/"
```

### Intervalle de Polling
Par défaut : **15 secondes** (configurable dans `LoginScreen.kt` ligne 135)

```kotlin
NotificationPollingService.startPolling(
    context = context,
    intervalSeconds = 15  // ← Modifier ici
)
```

Recommandations :
- **10s** : Messagerie instantanée (plus de batterie)
- **15s** : Réseau social (équilibré) ⭐ **Recommandé**
- **30s** : Notifications moins urgentes
- **60s** : Mises à jour en arrière-plan

---

## 🧪 Tests

### Test 1 : Vérifier le Polling
1. Login dans l'app
2. Vérifier les logs :
   ```
   🚀 User logged in, starting notification polling
   🔔 Notification polling started
   📡 Polling notifications...
   ```

### Test 2 : Recevoir une Notification
1. Depuis Postman/Backend, créer une publication
2. Attendre max 15 secondes
3. Notification Android doit apparaître
4. Vérifier les logs :
   ```
   📬 X new notification(s)
   ✅ Notification displayed: [titre]
   ```

### Test 3 : Cliquer sur Notification
1. Taper sur la notification
2. L'app doit s'ouvrir
3. Naviguer vers l'écran approprié
4. Vérifier le log :
   ```
   📲 Deep link detected: NEW_PUBLICATION
   ```

### Test 4 : Écran Liste Notifications
1. Dans l'app, naviguer vers "Notifications"
2. Voir la liste des notifications
3. Tirer vers le bas pour rafraîchir
4. Cliquer sur une notification → navigation

---

## 📱 Permissions Requises

### AndroidManifest.xml ✅ Déjà ajouté
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

### Demande Runtime (Android 13+) ✅ Déjà implémenté
MainActivity demande automatiquement la permission au démarrage.

---

## 🎯 Navigation Deep Links

### Déjà Configuré
Toutes les routes existent dans `MainActivity` :

- **Publication** : `"publicationDetail/{id}"` (TODO: créer cet écran si manquant)
- **Chat** : `"chatConversation/{sortieId}/..."` ✅
- **Sortie** : `"sortieDetail/{sortieId}"` ✅

### Si Écran Manquant
Créer la route dans `MainActivity.kt` :

```kotlin
composable(
    route = "publicationDetail/{publicationId}",
    arguments = listOf(navArgument("publicationId") { type = NavType.StringType })
) { backStackEntry ->
    val publicationId = backStackEntry.arguments?.getString("publicationId") ?: ""
    PublicationDetailScreen(navController, publicationId)
}
```

---

## 🔧 Dépendances Ajoutées

### build.gradle.kts ✅ Déjà ajouté
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

---

## 📂 Fichiers Créés/Modifiés

### Nouveaux Fichiers (8)
1. ✅ `models/Notification.kt`
2. ✅ `remote/NotificationApiService.kt`
3. ✅ `repository/NotificationRepository.kt`
4. ✅ `services/NotificationPollingService.kt`
5. ✅ `services/NotificationPollingWorker.kt`
6. ✅ `utils/NotificationHelper.kt`
7. ✅ `viewmodel/NotificationViewModel.kt`
8. ✅ `Screens/NotificationsScreen.kt`

### Fichiers Modifiés (5)
1. ✅ `remote/RetrofitInstance.kt` - Ajout de `notificationApi`
2. ✅ `app/build.gradle.kts` - Ajout de WorkManager
3. ✅ `AndroidManifest.xml` - Permission POST_NOTIFICATIONS
4. ✅ `MainActivity.kt` - Initialisation + deep links + route
5. ✅ `Screens/LoginScreen.kt` - Démarrage du polling

---

## 🎛️ Contrôles Disponibles

### Démarrer le Polling (Automatique après login)
```kotlin
NotificationPollingService.startPolling(context, intervalSeconds = 15)
```

### Arrêter le Polling (Automatique au logout/destroy)
```kotlin
NotificationPollingService.stopPolling()
```

### Vérifier si Actif
```kotlin
if (NotificationPollingService.isPollingActive()) {
    // Polling en cours
}
```

### Polling Immédiat (Manuel)
```kotlin
viewModel.refreshNotifications(context)
```

### Charger les Notifications pour l'UI
```kotlin
viewModel.loadNotifications(context, unreadOnly = false)
```

### Obtenir le Compteur
```kotlin
viewModel.loadUnreadCount(context)
```

---

## 🚨 Gestion des Erreurs

### Token Expiré (401)
- Le polling s'arrête automatiquement
- L'utilisateur est redirigé vers login
- Log : `🔐 Token expired, stopping polling`

### Pas de Connexion Internet
- Retry automatique au prochain poll
- WorkManager respecte la contrainte réseau

### Backend Indisponible
- Retry avec backoff exponentiel
- Logs détaillés pour debug

---

## 📊 Logs de Debug

Recherchez ces tags dans Logcat :
- `NotificationPolling` : Activité du service
- `NotificationRepository` : Requêtes API
- `NotificationHelper` : Affichage notifications
- `MainActivity` : Deep links
- `LoginScreen` : Démarrage du polling

---

## ✨ Prochaines Étapes (Optionnel)

### 1. Badge sur l'Icône Notifications
Dans votre TabBar ou TopBar :
```kotlin
val unreadCount by viewModel.unreadCount.collectAsState()

BadgedBox(badge = { Badge { Text("$unreadCount") } }) {
    Icon(Icons.Default.Notifications, "Notifications")
}
```

### 2. Son/Vibration
Dans `NotificationHelper.kt`, ajouter :
```kotlin
.setDefaults(NotificationCompat.DEFAULT_ALL)
```

### 3. Groupement des Notifications
Grouper les notifications par type (ex: tous les messages ensemble)

### 4. Actions Rapides
Ajouter des boutons sur la notification (Répondre, Accepter, etc.)

---

## 🎉 Résumé

✅ **Système complet de notifications par polling**  
✅ **Fonctionne sans FCM Firebase**  
✅ **Démarrage automatique après login**  
✅ **Intervalle de 15 secondes**  
✅ **Deep linking configuré**  
✅ **UI pour voir les notifications**  
✅ **Permissions Android 13+ gérées**  
✅ **Gestion d'erreurs robuste**  

---

## 🆘 Problèmes Courants

### Pas de Notifications Reçues
1. Vérifier que le backend retourne des notifications dans Postman
2. Vérifier les logs : `📡 Polling notifications...`
3. Vérifier le token : `UserPreferences.getToken(context)`
4. Vérifier la permission : Paramètres > Apps > Votre App > Notifications

### Polling Ne Démarre Pas
1. Vérifier que le token est sauvegardé après login
2. Vérifier les logs : `🚀 User logged in, starting notification polling`
3. Redémarrer l'app après modifications

### Deep Links Ne Fonctionnent Pas
1. Vérifier que la route existe dans `MainActivity`
2. Vérifier les logs : `📲 Deep link detected: [type]`
3. Créer la route manquante si nécessaire

---

**Système prêt à l'emploi ! 🚀**

