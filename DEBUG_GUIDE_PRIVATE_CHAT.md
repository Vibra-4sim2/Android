# 🔍 GUIDE DE DÉBOGAGE - CHAT PERSONNEL

## 🎯 PROBLÈMES ACTUELS

1. ❌ **Liste vide** - Aucune conversation ne s'affiche alors qu'elles existent dans le backend
2. ❌ **Création impossible** - Toast d'erreur après 4 secondes

## 🔧 LOGS AJOUTÉS POUR DÉBOGUER

J'ai ajouté des logs SUPER DÉTAILLÉS pour identifier le problème exact !

### Nouveaux Logs dans ConversationSocketManager

#### 1. Listener Universel
```
🔔 EVENT RECEIVED: [eventName] with X args
```
→ Capture **TOUS** les événements du backend

#### 2. Event `conversations`
```
========================================
📡 EVENT: conversations
Args count: X
Args[0] type: [type]
Args[0] content: [data]
========================================
```

#### 3. Event `conversationCreated`
```
========================================
📡 EVENT: conversationCreated
Args count: X
Args[0] type: [type]
Args[0] content: [data]
========================================
```

#### 4. Émission `getMyConversations`
```
========================================
📡 EMITTING: getMyConversations
Connected: true
Socket ID: [id]
========================================
```

#### 5. Émission `initiateConversation`
```
========================================
📡 EMITTING: initiateConversation
Recipient ID: [userId]
Payload: {"recipientId":"..."}
Connected: true
Socket ID: [id]
========================================
```

---

## 📱 PROCÉDURE DE TEST

### Test 1 : Vérifier la Liste des Conversations

1. **Lancer l'app**
2. **Aller dans Messages → Onglet "Personnel"**
3. **Regarder Logcat et chercher :**

#### ✅ CE QU'ON DEVRAIT VOIR (Si tout marche)
```
ConversationSocket: 🟢 Connected to /conversations namespace
ConversationSocket: 📡 Requesting conversations...
ConversationSocket: 🔔 EVENT RECEIVED: conversations with X args
ConversationSocket: ========================================
ConversationSocket: 📡 EVENT: conversations
ConversationSocket: Args count: 1
ConversationSocket: Args[0] type: JSONArray
ConversationSocket: Args[0] content: [{"_id":"...","participants":[...],...}]
ConversationSocket: ========================================
ConversationSocket: 📬 Received X conversations
```

#### ❌ CE QU'ON POURRAIT VOIR (Problèmes)

**Problème A : Aucun événement reçu**
```
ConversationSocket: 📡 Requesting conversations...
(rien après)
```
→ **Backend n'envoie rien** ou **nom d'événement incorrect**

**Problème B : Événement avec mauvais format**
```
ConversationSocket: 🔔 EVENT RECEIVED: myConversations with X args
```
→ **Nom différent !** Backend envoie `myConversations` au lieu de `conversations`

**Problème C : Erreur de parsing**
```
ConversationSocket: ❌ Error parsing conversations
ConversationSocket: Exception details: [...]
```
→ **Format de données incorrect** du backend

**Problème D : Événement vide**
```
ConversationSocket: 📡 EVENT: conversations
ConversationSocket: Args[0] content: []
ConversationSocket: 📬 Received 0 conversations
```
→ Backend répond mais avec une liste vide (problème backend)

---

### Test 2 : Vérifier la Création de Conversation

1. **Profil utilisateur → "Message"**
2. **Regarder Logcat et chercher :**

#### ✅ CE QU'ON DEVRAIT VOIR (Si tout marche)
```
UserProfileScreen: 🚀 Initiating conversation with userId: [userId]
ConversationSocket: ========================================
ConversationSocket: 📡 EMITTING: initiateConversation
ConversationSocket: Recipient ID: [userId]
ConversationSocket: Payload: {"recipientId":"..."}
ConversationSocket: Connected: true
ConversationSocket: Socket ID: [socketId]
ConversationSocket: ========================================
ConversationSocket: 💬 Initiating conversation with [userId]
ConversationSocket: 🔔 EVENT RECEIVED: conversationCreated with X args
ConversationSocket: ========================================
ConversationSocket: 📡 EVENT: conversationCreated
ConversationSocket: Args[0] content: {"_id":"...","participants":[...],...}
ConversationSocket: ========================================
ConversationSocket: ✅ Conversation created: [conversationId]
```

#### ❌ CE QU'ON POURRAIT VOIR (Problèmes)

**Problème A : Pas de réponse du backend**
```
ConversationSocket: 💬 Initiating conversation with [userId]
(rien après pendant 4 secondes)
UserProfileScreen: ❌ Conversation not found after 4 seconds
```
→ **Backend ne répond pas** à `initiateConversation`

**Problème B : Événement différent**
```
ConversationSocket: 🔔 EVENT RECEIVED: conversation with X args
```
→ Backend envoie `conversation` (singulier) au lieu de `conversationCreated`

**Problème C : Erreur backend**
```
ConversationSocket: 🔔 EVENT RECEIVED: error with X args
ConversationSocket: ❌ Server error: [message]
```
→ Backend retourne une erreur

---

## 🎯 SCÉNARIOS POSSIBLES

### Scénario 1 : Nom d'événement différent ⚠️

**Symptôme :**
```
🔔 EVENT RECEIVED: myConversations
🔔 EVENT RECEIVED: conversation
```

**Solution :**
Le backend utilise des noms différents ! Il faut ajouter ces listeners :

```kotlin
// Dans setupEventListeners()
on("myConversations", onConversations)  // Au lieu de "conversations"
on("conversation", onConversationCreated)  // Au lieu de "conversationCreated"
```

---

### Scénario 2 : Format de données différent ❌

**Symptôme :**
```
❌ Error parsing conversations
Exception details: JSONArray cannot be cast to JSONObject
```

**Solution :**
Le backend envoie un objet au lieu d'un tableau ! Format différent de celui attendu.

Il faudra adapter le parsing dans `onConversations` et `onConversationCreated`.

---

### Scénario 3 : Backend ne répond pas 🔇

**Symptôme :**
```
📡 EMITTING: getMyConversations
(rien après)
```

**Solution :**
- Vérifier que le backend écoute bien sur `/conversations`
- Vérifier l'authentification JWT
- Vérifier que le backend implémente les événements

---

### Scénario 4 : Namespace incorrect 🌐

**Symptôme :**
```
🟢 Connected to /conversations namespace
📡 EMITTING: getMyConversations
(rien)
```

**Solution :**
Le backend n'a peut-être PAS de namespace `/conversations` !
Peut-être utilise-t-il le namespace par défaut `/` ?

---

## 📋 CHECKLIST DE DÉBOGAGE

### Étape 1 : Lancer l'app et vérifier Logcat

- [ ] Socket se connecte ? (`🟢 Connected to /conversations namespace`)
- [ ] `getMyConversations` est émis ? (`📡 EMITTING: getMyConversations`)
- [ ] Un événement est reçu ? (`🔔 EVENT RECEIVED: ...`)
- [ ] Quel est le NOM de l'événement reçu ?
- [ ] Quel est le FORMAT des données ? (JSONArray, JSONObject, autre ?)

### Étape 2 : Tester la création

- [ ] Clic sur "Message" dans un profil
- [ ] `initiateConversation` est émis ? (`📡 EMITTING: initiateConversation`)
- [ ] Un événement est reçu ? (`🔔 EVENT RECEIVED: ...`)
- [ ] Quel événement ? (`conversationCreated`, `conversation`, autre ?)
- [ ] Une erreur est retournée ? (`❌ Server error:`)

### Étape 3 : Analyser les données

- [ ] Les données sont-elles dans le bon format ?
- [ ] Les champs requis sont présents ? (`_id`, `participants`, etc.)
- [ ] Les participants sont des objets ou des IDs ?

---

## 🚀 PROCHAINES ÉTAPES

### 1. COMPILER ET LANCER L'APP

```bash
./gradlew clean build
./gradlew installDebug
```

### 2. OUVRIR LOGCAT

```bash
adb logcat -s ConversationSocket UserProfileScreen ConversationsVM
```

### 3. TESTER ET COPIER LES LOGS

- Aller dans Messages → Onglet "Personnel"
- Cliquer sur "Message" dans un profil
- **COPIER TOUS LES LOGS** et me les envoyer !

### 4. JE VAIS ANALYSER

Avec ces logs, je pourrai identifier :
- ✅ Le nom exact des événements du backend
- ✅ Le format exact des données
- ✅ Les erreurs de parsing
- ✅ La solution précise à appliquer

---

## 📝 FORMAT POUR M'ENVOYER LES LOGS

```
=== TEST 1 : LISTE DES CONVERSATIONS ===
[Copier les logs ici]

=== TEST 2 : CRÉATION DE CONVERSATION ===
[Copier les logs ici]
```

---

## 💡 SOLUTIONS RAPIDES POSSIBLES

### Si événement `myConversations` au lieu de `conversations`

```kotlin
on("myConversations", onConversations)
```

### Si événement `conversation` au lieu de `conversationCreated`

```kotlin
on("conversation", onConversationCreated)
```

### Si format différent (objet au lieu de tableau)

Je devrai modifier le parsing dans les listeners.

---

**COMPILEZ, TESTEZ, ET ENVOYEZ-MOI LES LOGS COMPLETS ! 🔥**

Je pourrai alors corriger EXACTEMENT le problème !

---

📅 **Date :** 5 Janvier 2026 22:15  
🔍 **Action requise :** TESTER ET ENVOYER LES LOGS  
🎯 **Objectif :** Identifier le problème exact du backend

