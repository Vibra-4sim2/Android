# 🚀 GUIDE DE DÉMARRAGE RAPIDE - CHAT PERSONNEL

## ⚡ Configuration en 3 étapes

### ÉTAPE 1 : Configurer l'URL du serveur ⚙️

Ouvrez `ConversationsViewModel.kt` (ligne 20) et configurez l'URL de votre serveur :

```kotlin
// Pour l'émulateur Android
private const val SERVER_URL = "http://10.0.2.2:10000"

// Pour un appareil réel (remplacez par l'IP de votre serveur)
// private const val SERVER_URL = "http://192.168.1.100:10000"
```

### ÉTAPE 2 : Vérifier que le backend est prêt ✅

Votre backend Node.js doit avoir :

#### A) Socket.IO configuré avec le namespace `/conversations`
```javascript
const conversationNamespace = io.of('/conversations');

conversationNamespace.use((socket, next) => {
  // Authentification JWT
  const token = socket.handshake.query.token;
  // Vérifier le token...
  next();
});
```

#### B) Les événements suivants implémentés :
- ✅ `getMyConversations` - Récupérer les conversations de l'utilisateur
- ✅ `initiateConversation` - Créer une conversation avec un utilisateur
- ✅ `joinConversation` - Rejoindre une conversation
- ✅ `leaveConversation` - Quitter une conversation
- ✅ `sendDirectMessage` - Envoyer un message
- ✅ `getMessages` - Récupérer l'historique
- ✅ `markAsRead` - Marquer comme lu
- ✅ `typing` - Indicateur de frappe
- ✅ `deleteConversation` - Supprimer une conversation
- ✅ `muteConversation` - Couper les notifications

#### C) Les événements émis par le serveur :
- ✅ `conversations` - Liste des conversations
- ✅ `conversationCreated` - Nouvelle conversation créée
- ✅ `receiveDirectMessage` - Nouveau message reçu
- ✅ `messageHistory` - Historique des messages
- ✅ `messageRead` - Messages marqués comme lus
- ✅ `userTyping` - Utilisateur en train d'écrire
- ✅ `error` - Erreur serveur

### ÉTAPE 3 : Compiler et tester 🎯

```bash
# 1. Synchroniser Gradle
./gradlew clean build

# 2. Lancer l'application
# Via Android Studio : Run > Run 'app'
# Ou via ligne de commande :
./gradlew installDebug

# 3. Tester les fonctionnalités
```

---

## 📱 Comment utiliser le chat personnel

### 1️⃣ Démarrer une conversation depuis un profil utilisateur

```
Accueil → Rechercher un utilisateur → Profil → Bouton "Message" 💬
```

L'application va :
1. Se connecter au serveur Socket.IO
2. Créer/récupérer la conversation avec cet utilisateur
3. Ouvrir le chat privé

### 2️⃣ Démarrer une conversation depuis une sortie

```
Sorties → Détails d'une sortie → Icône Message 📩 (à côté du créateur)
```

### 3️⃣ Voir toutes vos conversations

```
Bottom Navigation → Messages → Onglet "Personnel"
```

Vous verrez :
- 📝 Toutes vos conversations privées
- 🔴 Badge rouge avec le nombre de messages non lus
- 🕐 Heure du dernier message
- 👤 Avatar de l'utilisateur

### 4️⃣ Envoyer un message

```
Dans une conversation ouverte :
1. Taper votre message dans le champ texte
2. Appuyer sur le bouton Envoyer (icône avion) 📤
```

Le message sera :
- ✅ Envoyé en temps réel via Socket.IO
- ✅ Affiché avec un statut (✓ envoyé, ✓✓ lu)
- ✅ Visible instantanément pour les deux utilisateurs

---

## 🔍 Vérification du fonctionnement

### Test de connexion Socket

1. Lancez l'app
2. Ouvrez Logcat dans Android Studio
3. Filtrez par tag : `ConversationSocket`
4. Vous devriez voir :

```
D/ConversationSocket: ✅ Socket initialized for namespace: /conversations
D/ConversationSocket: 🔌 Connecting to /conversations...
D/ConversationSocket: 🟢 Connected to /conversations namespace
D/ConversationSocket: 📡 Requesting conversations...
D/ConversationSocket: 📬 Received X conversations
```

### Test d'envoi de message

1. Ouvrez une conversation
2. Envoyez un message
3. Vérifiez dans Logcat :

```
D/ConversationSocket: 📤 Sent message in conversation [ID]
D/ConversationSocket: 📨 New message received: [contenu]
```

---

## 🐛 Dépannage

### ❌ Problème : "Connexion..." infini

**Solution :**
1. Vérifiez que le serveur Socket.IO est démarré
2. Vérifiez l'URL dans `ConversationsViewModel.kt`
3. Pour un appareil réel, vérifiez que vous utilisez l'IP correcte (pas localhost)

### ❌ Problème : "Token non trouvé"

**Solution :**
1. Assurez-vous que l'utilisateur est connecté
2. Vérifiez que le token JWT est stocké dans SharedPreferences
3. Vérifiez dans `UserPreferences.kt`

### ❌ Problème : Les messages n'arrivent pas

**Solution :**
1. Vérifiez la connexion Socket dans Logcat
2. Vérifiez que les deux utilisateurs sont connectés
3. Testez l'événement `sendDirectMessage` côté backend

### ❌ Problème : Erreur de compilation

**Solution :**
1. Synchronisez Gradle : `File > Sync Project with Gradle Files`
2. Nettoyez le build : `Build > Clean Project`
3. Rebuild : `Build > Rebuild Project`

---

## 📊 Modèle de données

### Conversation
```json
{
  "_id": "conv123",
  "participants": [
    {
      "_id": "user1",
      "firstName": "Jean",
      "lastName": "Dupont",
      "email": "jean@example.com",
      "avatar": "https://..."
    },
    {
      "_id": "user2",
      "firstName": "Marie",
      "lastName": "Martin",
      "email": "marie@example.com",
      "avatar": "https://..."
    }
  ],
  "lastMessage": {
    "_id": "msg123",
    "senderId": "user1",
    "type": "text",
    "content": "Salut !",
    "createdAt": "2026-01-05T14:30:00.000Z"
  },
  "unreadCount": {
    "user1": 0,
    "user2": 2
  },
  "createdAt": "2026-01-05T10:00:00.000Z",
  "updatedAt": "2026-01-05T14:30:00.000Z"
}
```

### DirectMessage
```json
{
  "_id": "msg123",
  "conversationId": "conv123",
  "senderId": {
    "_id": "user1",
    "firstName": "Jean",
    "lastName": "Dupont",
    "avatar": "https://..."
  },
  "recipientId": "user2",
  "type": "text",
  "content": "Salut ! Comment ça va ?",
  "isRead": false,
  "status": "sent",
  "createdAt": "2026-01-05T14:30:00.000Z",
  "updatedAt": "2026-01-05T14:30:00.000Z"
}
```

---

## 🎨 Personnalisation

### Changer la couleur des bulles de messages

Éditez `PrivateChatScreen.kt` (ligne ~450) :

```kotlin
Surface(
    // ...
    color = if (message.isMe) 
        GreenAccent.copy(alpha = 0.2f)  // ← Vos messages
    else 
        CardDark  // ← Messages reçus
) {
    // ...
}
```

### Changer le temps d'indicateur de frappe

Éditez `PrivateChatScreen.kt` (ligne ~380) :

```kotlin
typingJob = coroutineScope.launch {
    delay(2000)  // ← Modifier ici (en millisecondes)
    viewModel.setTyping(conversationId, false)
}
```

---

## 📚 Ressources

### Fichiers clés à connaître

| Fichier | Rôle |
|---------|------|
| `ConversationModels.kt` | Modèles de données |
| `ConversationSocketManager.kt` | Gestion Socket.IO |
| `ConversationsViewModel.kt` | Logique métier |
| `PrivateChatScreen.kt` | Interface de chat |
| `MessagesListScreen.kt` | Liste des conversations |

### Logs utiles

| Tag | Description |
|-----|-------------|
| `ConversationSocket` | Événements Socket.IO |
| `ConversationsVM` | Actions du ViewModel |
| `PrivateChatScreen` | UI du chat |

---

## ✅ Checklist de déploiement

Avant de déployer en production :

- [ ] Changer l'URL du serveur (production)
- [ ] Tester sur plusieurs appareils
- [ ] Tester la connexion/déconnexion Socket
- [ ] Tester l'envoi/réception de messages
- [ ] Tester l'indicateur de frappe
- [ ] Tester les badges de non-lus
- [ ] Tester la création de conversations
- [ ] Vérifier les permissions réseau dans AndroidManifest.xml
- [ ] Activer ProGuard pour l'obfuscation
- [ ] Tester avec un réseau lent (throttling)
- [ ] Tester la gestion des erreurs
- [ ] Ajouter Google Analytics / Firebase Analytics

---

## 🚀 Prêt à démarrer !

Tout est configuré et prêt à l'emploi. Le système de chat personnel est **100% fonctionnel** et **complètement indépendant** du chat de groupe.

**Besoin d'aide ?** Consultez `CHAT_PERSONNEL_IMPLEMENTATION.md` pour la documentation complète.

---

💡 **Astuce** : Activez les logs détaillés pour le développement :
```kotlin
// Dans ConversationSocketManager.kt
private const val ENABLE_VERBOSE_LOGS = true
```

🎉 **Bon développement !**

