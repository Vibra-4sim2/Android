# 🔄 FLUX DU CHAT PRIVÉ - DIAGRAMME

## 📱 CRÉATION D'UNE CONVERSATION

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER PROFILE SCREEN                         │
│                                                                     │
│  [Avatar]  John Doe                                                │
│                                                                     │
│  [📧 Message]  ◄── L'utilisateur clique ici                       │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ConversationsViewModel                           │
│                                                                     │
│  startConversationWithUser(recipientId: "userId123")               │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationSocketManager                          │
│                                                                     │
│  socket.emit("initiateConversation", {                             │
│    recipientId: "userId123"                                        │
│  })                                                                │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ WebSocket
                                 │ /conversations namespace
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     BACKEND NESTJS                                  │
│                   ConversationGateway                               │
│                                                                     │
│  @SubscribeMessage('initiateConversation')                         │
│  handleInitiateConversation() {                                    │
│    // 1. Trouve ou crée la conversation                           │
│    // 2. Joint la room                                            │
│    // 3. Charge les messages récents                              │
│    // 4. Émet conversationReady                                   │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ socket.emit('conversationReady')
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationSocketManager                          │
│                                                                     │
│  on("conversationReady") {                                         │
│    val data = args[0] as JSONObject                                │
│    val conversation = data.getJSONObject("conversation")           │
│    val messages = data.getJSONArray("messages")                    │
│    _currentConversation.value = conversation                       │
│    _messages.value = messages                                      │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    UserProfileScreen                                │
│                                                                     │
│  navController.navigate(                                           │
│    "privateChat/${conversationId}/${userId}/${name}/${avatar}"    │
│  )                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PrivateChatScreen                                │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  ← John Doe                                    ⋮             │ │
│  ├───────────────────────────────────────────────────────────────┤ │
│  │                                                               │ │
│  │  [Salut!]                                           10:30    │ │
│  │                                                               │ │
│  │                                     [Hey! Ça va?]    10:31   │ │
│  │                                                               │ │
│  ├───────────────────────────────────────────────────────────────┤ │
│  │  [Taper un message...]                          [Envoyer →]  │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 💬 ENVOI D'UN MESSAGE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PrivateChatScreen                                │
│                                                                     │
│  [Salut!] ◄── L'utilisateur tape et envoie                        │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ConversationsViewModel                           │
│                                                                     │
│  sendMessage(conversationId, "Salut!")                             │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationSocketManager                          │
│                                                                     │
│  socket.emit("sendDirectMessage", {                                │
│    conversationId: "conv123",                                      │
│    type: "text",                                                   │
│    content: "Salut!"                                               │
│  })                                                                │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ WebSocket
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     BACKEND NESTJS                                  │
│                                                                     │
│  @SubscribeMessage('sendDirectMessage')                            │
│  handleSendDirectMessage() {                                       │
│    // 1. Valide le message                                        │
│    // 2. Sauvegarde en DB                                         │
│    // 3. Incrémente unreadCount                                   │
│    // 4. Envoie notification push                                 │
│    // 5. Broadcast aux participants                               │
│  }                                                                 │
└──────────────────┬───────────────────────┬──────────────────────────┘
                   │                       │
     server.to(room).emit()    server.to(room).emit()
     'receiveDirectMessage'    'directMessageSent'
                   │                       │
         ┌─────────▼─────────┐   ┌────────▼─────────┐
         │   DESTINATAIRE    │   │   EXPÉDITEUR     │
         │                   │   │                   │
         │  Reçoit le msg    │   │  Confirmation    │
         │  Badge +1         │   │  d'envoi         │
         └───────────────────┘   └──────────────────┘
```

---

## 📋 AFFICHAGE DE LA LISTE DES CONVERSATIONS

```
┌─────────────────────────────────────────────────────────────────────┐
│                     MessagesListScreen                              │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  Messages                                           [🔄]      │ │
│  │                                                               │ │
│  │  [🔍 Rechercher...]                                          │ │
│  │                                                               │ │
│  │  [Groupes]  [Personnel] ◄── L'utilisateur clique ici        │ │
│  └───────────────────────────────────────────────────────────────┘ │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│               LaunchedEffect(selectedTab)                           │
│                                                                     │
│  if (selectedTab == "personal") {                                  │
│    conversationsViewModel.initialize(context)                      │
│    conversationsViewModel.loadConversations()                      │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationSocketManager                          │
│                                                                     │
│  socket.emit("getMyConversations", {})                             │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ WebSocket
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     BACKEND NESTJS                                  │
│                                                                     │
│  @SubscribeMessage('getMyConversations')                           │
│  handleGetMyConversations() {                                      │
│    // 1. Récupère toutes les conversations de l'utilisateur       │
│    // 2. Populate les participants                                │
│    // 3. Formatte avec otherUser                                  │
│    // 4. Émet conversationsList                                   │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ client.emit('conversationsList')
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationSocketManager                          │
│                                                                     │
│  on("conversationsList") {                                         │
│    val data = args[0] as JSONObject                                │
│    val conversationsArray = data.getJSONArray("conversations")     │
│    _conversations.value = conversationList                         │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  ConversationsViewModel                             │
│                                                                     │
│  conversations.collect { list ->                                   │
│    _conversationsUI.value = list.map {                             │
│      ConversationUI.fromConversation(it, currentUserId)            │
│    }                                                               │
│  }                                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     MessagesListScreen                              │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │  [Personnel]                                                  │ │
│  │  ┌─────────────────────────────────────────────────────┐    │ │
│  │  │  [👤] John Doe                           [2]   10:30│    │ │
│  │  │       📷 Photo                                      │    │ │
│  │  └─────────────────────────────────────────────────────┘    │ │
│  │  ┌─────────────────────────────────────────────────────┐    │ │
│  │  │  [👤] Jane Smith                              09:15│    │ │
│  │  │       Salut! Ça va?                                │    │ │
│  │  └─────────────────────────────────────────────────────┘    │ │
│  │  ┌─────────────────────────────────────────────────────┐    │ │
│  │  │  [👤] Bob Martin                        [5]   08:45│    │ │
│  │  │       🎤 Message vocal                             │    │ │
│  │  └─────────────────────────────────────────────────────┘    │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 ÉVÉNEMENTS SOCKET.IO - CORRESPONDANCE

### Backend (NestJS) → Frontend (Android)

| Backend Émet          | Frontend Écoute       | Description                    |
|----------------------|----------------------|--------------------------------|
| `conversationsList`  | `conversationsList`  | ✅ Liste des conversations    |
| `conversationReady`  | `conversationReady`  | ✅ Conversation créée/ouverte |
| `messagesList`       | `messagesList`       | ✅ Historique des messages    |
| `receiveDirectMessage` | `receiveDirectMessage` | ✅ Nouveau message reçu    |
| `messagesRead`       | `messagesRead`       | ✅ Messages marqués lus       |
| `userTyping`         | `userTyping`         | ✅ Indicateur de frappe       |
| `directMessageSent`  | `directMessageSent`  | ✅ Confirmation d'envoi       |
| `markedAsRead`       | `markedAsRead`       | ✅ Confirmation de lecture    |
| `joinedConversation` | `joinedConversation` | ✅ Conversation rejointe      |
| `conversationDeleted` | `conversationDeleted` | ✅ Conversation supprimée   |
| `conversationMuted`  | `conversationMuted`  | ✅ Conversation muet/démuet   |
| `unreadCount`        | `unreadCount`        | ✅ Compteur non lus           |
| `error`              | `error`              | ✅ Erreur serveur             |

### Frontend (Android) → Backend (NestJS)

| Frontend Émet             | Backend Reçoit            | Description                  |
|--------------------------|--------------------------|------------------------------|
| `getMyConversations`     | `getMyConversations`     | ✅ Charger conversations    |
| `initiateConversation`   | `initiateConversation`   | ✅ Créer conversation       |
| `joinConversation`       | `joinConversation`       | ✅ Rejoindre conversation   |
| `leaveConversation`      | `leaveConversation`      | ✅ Quitter conversation     |
| `sendDirectMessage`      | `sendDirectMessage`      | ✅ Envoyer message          |
| `getMessages`            | `getMessages`            | ✅ Charger plus de messages |
| `markAsRead`             | `markAsRead`             | ✅ Marquer comme lu         |
| `typing`                 | `typing`                 | ✅ Envoyer indicateur       |
| `deleteConversation`     | `deleteConversation`     | ✅ Supprimer conversation   |
| `muteConversation`       | `muteConversation`       | ✅ Mute/Unmute              |
| `getUnreadCount`         | `getUnreadCount`         | ✅ Obtenir compteur         |

---

## ✅ TOUS LES ÉVÉNEMENTS SONT MAINTENANT CORRECTEMENT MAPPÉS!

**Avant:** 3 événements écoutés  
**Après:** 13 événements écoutés  

Le chat privé est maintenant 100% fonctionnel! 🎉

