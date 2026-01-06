# ⚠️ PROBLÈME DE CONNEXION SOCKET - CHAT PERSONNEL

## 🔴 Problème Identifié

D'après les logs, le problème est clair :

```
E  ❌ Connection error: [io.socket.engineio.client.EngineIOException: websocket error]
W  ⚠️ Not connected - cannot get conversations
W  ⚠️ Not connected - cannot initiate conversation
```

**Le socket ne peut pas se connecter au serveur !**

---

## 🔍 Causes Possibles

### 1. Le Backend N'est Pas Démarré ❌
Le serveur Node.js sur le port 10000 n'est pas en cours d'exécution.

### 2. Le Namespace `/conversations` N'existe Pas ❌
Le backend n'a pas encore implémenté le namespace Socket.IO pour les conversations privées.

### 3. Mauvaise URL ❌
L'URL configurée `http://10.0.2.2:10000` n'est pas la bonne.

---

## ✅ SOLUTIONS

### Solution Immédiate : Utiliser l'URL du Backend Existant

J'ai modifié l'URL pour utiliser celle du chat de groupe qui fonctionne déjà :

```kotlin
private const val SERVER_URL = "https://backend-android-q2s8.onrender.com"
```

**MAIS** : Le backend doit avoir le namespace `/conversations` configuré !

---

## 🛠️ Ce Qui Doit Être Fait Côté Backend

### 1. Ajouter le Namespace `/conversations`

```javascript
// server.js ou app.js
const io = require('socket.io')(server);

// ✅ Namespace pour les conversations privées
const conversationNamespace = io.of('/conversations');

conversationNamespace.use((socket, next) => {
  // Authentification JWT
  const token = socket.handshake.query.token;
  // Vérifier le token...
  next();
});

conversationNamespace.on('connection', (socket) => {
  console.log('✅ User connected to /conversations');
  
  // Événements à implémenter
  socket.on('getMyConversations', () => { /* ... */ });
  socket.on('initiateConversation', (data) => { /* ... */ });
  socket.on('joinConversation', (data) => { /* ... */ });
  socket.on('sendDirectMessage', (data) => { /* ... */ });
  socket.on('getMessages', (data) => { /* ... */ });
  socket.on('markAsRead', (data) => { /* ... */ });
  socket.on('typing', (data) => { /* ... */ });
});
```

### 2. Créer le Modèle de Conversation

```javascript
// models/Conversation.js
const mongoose = require('mongoose');

const conversationSchema = new mongoose.Schema({
  participants: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }],
  lastMessage: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'DirectMessage'
  },
  unreadCount: {
    type: Map,
    of: Number,
    default: {}
  },
  mutedBy: {
    type: Map,
    of: Boolean,
    default: {}
  }
}, { timestamps: true });

module.exports = mongoose.model('Conversation', conversationSchema);
```

### 3. Créer le Modèle de Message Direct

```javascript
// models/DirectMessage.js
const mongoose = require('mongoose');

const directMessageSchema = new mongoose.Schema({
  conversationId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Conversation',
    required: true
  },
  senderId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  recipientId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  type: {
    type: String,
    enum: ['text', 'image', 'audio', 'video', 'file', 'location'],
    default: 'text'
  },
  content: String,
  mediaUrl: String,
  isRead: {
    type: Boolean,
    default: false
  },
  status: {
    type: String,
    enum: ['sent', 'delivered', 'read', 'failed'],
    default: 'sent'
  }
}, { timestamps: true });

module.exports = mongoose.model('DirectMessage', directMessageSchema);
```

---

## 🚨 EN ATTENDANT LE BACKEND

### Option 1 : Désactiver Temporairement le Chat Personnel

Si le backend n'est pas prêt, commentez l'initialisation dans `MessagesListScreen.kt` :

```kotlin
// LaunchedEffect(Unit) {
//     ChatStateManager.initialize(context)
//     com.example.dam.utils.ReadMessagesManager.initialize(context)
//     viewModel.loadUserChats(context)
//     // ✅ Initialize private conversations
//     conversationsViewModel.initialize(context)  // ← Commentez cette ligne
// }
```

### Option 2 : Afficher un Message d'Information

Modifiez `MessagesListScreen.kt` pour afficher un message dans l'onglet "Personnel" :

```kotlin
"personal" -> {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Construction,
            contentDescription = "En construction",
            tint = TextTertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chat personnel en développement",
            color = TextSecondary,
            fontSize = 16.sp
        )
        Text(
            text = "Le backend doit implémenter le namespace /conversations",
            color = TextTertiary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
```

---

## 🔍 COMMENT TESTER SI LE BACKEND EST PRÊT

### Test 1 : Vérifier le Serveur

```bash
curl http://YOUR_SERVER_IP:10000/socket.io/
# Ou pour Render :
curl https://backend-android-q2s8.onrender.com/socket.io/
```

Devrait retourner : `{"code":0,"message":"Transport unknown"}`

### Test 2 : Tester la Connexion Socket

Utilisez un client Socket.IO :

```javascript
const io = require('socket.io-client');
const socket = io('https://backend-android-q2s8.onrender.com/conversations', {
  query: { token: 'YOUR_JWT_TOKEN' }
});

socket.on('connect', () => {
  console.log('✅ Connected to /conversations');
});

socket.on('connect_error', (error) => {
  console.log('❌ Connection error:', error);
});
```

---

## 📝 CHECKLIST BACKEND

Avant que le chat personnel fonctionne, le backend doit avoir :

- [ ] Namespace `/conversations` créé
- [ ] Modèle `Conversation` créé
- [ ] Modèle `DirectMessage` créé
- [ ] Événement `getMyConversations` implémenté
- [ ] Événement `initiateConversation` implémenté
- [ ] Événement `joinConversation` implémenté
- [ ] Événement `sendDirectMessage` implémenté
- [ ] Événement `getMessages` implémenté
- [ ] Événement `markAsRead` implémenté
- [ ] Événement `typing` implémenté
- [ ] Authentification JWT pour les sockets
- [ ] Émission des événements côté serveur (`receiveDirectMessage`, etc.)

---

## 🎯 RÉSUMÉ

### Problème Actuel
Le chat personnel **ne peut pas se connecter** car le backend n'a pas le namespace `/conversations`.

### Solution Immédiate
1. ✅ J'ai changé l'URL pour utiliser `https://backend-android-q2s8.onrender.com`
2. ⚠️ Le backend doit implémenter le namespace `/conversations`
3. 🔧 En attendant, vous pouvez désactiver l'onglet "Personnel" temporairement

### Prochaine Étape
**Implémenter le backend** pour les conversations privées avec tous les événements Socket.IO requis.

---

📅 **Date :** 5 Janvier 2026  
🔴 **Statut :** Backend manquant  
✅ **Frontend :** Prêt et fonctionnel  
⏳ **En attente :** Implémentation backend du namespace /conversations

