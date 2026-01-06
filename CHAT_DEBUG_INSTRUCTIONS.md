# 🔍 Instructions de Debug pour le Problème de Blocage du Chat

## 📋 Symptômes du Problème

Après avoir envoyé un message et utilisé la flèche de retour, la réouverture de la même discussion montre :
- ❌ Champ de saisie bloqué (grisé / non focusable)
- ❌ Indicateur "Connexion..." en rouge persistant
- ✅ Historique des messages se recharge correctement
- ✅ Événements `joinedRoom` arrivent bien

## 🎯 Points de Diagnostic Critiques

### 1. **Cycle de Navigation**
Surveillez la séquence complète :
```
[Entrée chat] → [Envoi message] → [Navigation arrière] → [Réouverture chat]
```

### 2. **Logs à Surveiller dans l'Ordre**

#### A. Premier Accès au Chat (Normal)
```
ChatConversationScreen: 🚀 LaunchedEffect(sortieId) DÉCLENCHÉ
ChatConversationScreen:    isSending: false
ChatViewModel: 🔌 DÉBUT CONNEXION CHAT - DIAGNOSTIC COMPLET
ChatViewModel:    isSending: false ⚠️ CRITIQUE
ChatViewModel: 🧹 Nettoyage de l'état...
ChatViewModel:    isSending après nettoyage: false
SocketService: ✅ Listeners configurés (doublons évités)
ChatViewModel: ✅ Socket déjà connecté
ChatViewModel: 🏠 Tentative de rejoindre la room
SocketService: 🏠 EVENT: joinedRoom
ChatViewModel:    isSending: false ⚠️ (devrait rester false)
```

#### B. Envoi d'un Message
```
ChatViewModel: 📤 ENVOI MESSAGE TEXTE
ChatViewModel:    isSending avant: false
ChatViewModel:    isSending après: true
SocketService: Sending message...
SocketService: ✅ MESSAGE SENT confirmation
ChatViewModel:    isSending: false (réinitialisé après confirmation)
```

#### C. Navigation Arrière (CRITIQUE)
```
ChatConversationScreen: 🚪 DisposableEffect onDispose APPELÉ
ChatViewModel: 👋 LEAVE ROOM APPELÉ
ChatViewModel: État AVANT leave:
ChatViewModel:    isSending: false ⚠️⚠️⚠️ (DOIT ÊTRE FALSE ICI)
ChatViewModel:    sendTimeoutJob: null ou ACTIF ⚠️
ChatViewModel: 🧹 Nettoyage...
ChatViewModel:    isSending après leave: false
```

#### D. Réouverture du Chat (PROBLÈME ICI)
```
ChatConversationScreen: 🚀 LaunchedEffect(sortieId) DÉCLENCHÉ
ChatConversationScreen:    isSending: ??? ⚠️⚠️⚠️ VÉRIFIER CETTE VALEUR
ChatViewModel: 🔌 DÉBUT CONNEXION CHAT
ChatViewModel: État AVANT nettoyage:
ChatViewModel:    isSending: ??? ⚠️⚠️⚠️ SI TRUE = BUG CONFIRMÉ
ChatViewModel:    sendTimeoutJob: ??? ⚠️ Si ACTIF = timeout non annulé
ChatViewModel: 🧹 Nettoyage...
ChatViewModel:    isSending après nettoyage: false
SocketService: 🏠 EVENT: joinedRoom (peut apparaître 2x si duplication)
ChatViewModel:    isSending: ??? ⚠️ Vérifier s'il redevient true
```

## 🐛 Causes Possibles Identifiées

### 1. **État `isSending` Non Réinitialisé**
- **Symptôme** : `isSending` reste à `true` après navigation
- **Impact** : `BasicTextField` reste désactivé (`enabled = isConnected && !isSending`)
- **Vérification** : Logs `isSending` dans `leaveRoom()` et `connectAndJoinRoom()`

### 2. **`sendTimeoutJob` Actif Persiste**
- **Symptôme** : Le timeout de 10s continue après navigation
- **Impact** : Peut modifier `isSending` pendant la réouverture
- **Vérification** : Log `sendTimeoutJob` dans `leaveRoom()`

### 3. **Duplication des Listeners Socket.IO**
- **Symptôme** : `joinedRoom` reçu 2 fois sur réouverture
- **Impact** : État réinitialisé puis réaffecté incorrectement
- **Correction Appliquée** : Nettoyage des listeners dans `setupListeners()`

### 4. **État `_isConnected` Incorrect**
- **Symptôme** : `isConnected` reste `false` malgré socket connecté
- **Impact** : `BasicTextField` désactivé (`enabled = isConnected && !isSending`)
- **Vérification** : Comparer `_isConnected.value` vs `SocketService.isConnected()`

### 5. **Recomposition avec Ancien État**
- **Symptôme** : UI utilise un snapshot d'état ancien (avant nettoyage)
- **Impact** : Champ reste bloqué malgré état correct dans ViewModel
- **Vérification** : Logs dans `ChatConversationScreen` vs `ChatViewModel`

## 📊 Checklist de Reproduction

1. ✅ Ouvrir une conversation (sortieId valide)
2. ✅ Envoyer un message texte
3. ✅ **IMMÉDIATEMENT** après voir la confirmation (checkmark vert), cliquer sur la flèche de retour
4. ✅ Attendre que la liste des discussions s'affiche
5. ✅ **IMMÉDIATEMENT** réouvrir la même conversation
6. ❌ Constater : champ bloqué + "Connexion..." rouge

## 🔬 Tests Spécifiques à Effectuer

### Test 1 : État `isSending` Persistant
```
1. Envoi message → isSending = true
2. Confirmation reçue → isSending = false
3. Navigation arrière AVANT 10s
4. Vérifier log leaveRoom(): isSending = ???
5. Réouverture immédiate
6. Vérifier log connectAndJoinRoom() AVANT nettoyage: isSending = ???
```

**Résultat attendu** : `isSending` DOIT être `false` dans leaveRoom() et AVANT nettoyage

### Test 2 : Timeout Job Persistant
```
1. Envoi message → sendTimeoutJob créé
2. Confirmation reçue → sendTimeoutJob annulé
3. Navigation arrière
4. Vérifier log leaveRoom(): sendTimeoutJob = null ou ACTIF ???
```

**Résultat attendu** : `sendTimeoutJob` DOIT être `null`

### Test 3 : Duplication joinedRoom
```
1. Réouverture chat
2. Compter combien de fois "🏠 EVENT: joinedRoom" apparaît
```

**Résultat attendu** : 1 seule fois (correction appliquée)

### Test 4 : État isConnected
```
1. Réouverture chat
2. Comparer dans les logs:
   - ChatViewModel._isConnected.value
   - SocketService.isConnected()
   - ChatConversationScreen isConnected (UI)
```

**Résultat attendu** : Les 3 doivent être `true`

## 🛠️ Corrections Appliquées

### ✅ 1. Nettoyage Complet dans `leaveRoom()`
```kotlin
sendTimeoutJob?.cancel()
sendTimeoutJob = null
_isSending.value = false
_isLoading.value = false
_errorMessage.value = null
_successMessage.value = null
```

### ✅ 2. Nettoyage Préventif dans `connectAndJoinRoom()`
```kotlin
sendTimeoutJob?.cancel()
sendTimeoutJob = null
_isSending.value = false
_isLoading.value = true
```

### ✅ 3. Suppression des Listeners Dupliqués
```kotlin
private fun setupListeners() {
    socket?.apply {
        off(Socket.EVENT_CONNECT)
        off("joinedRoom")
        // ... tous les événements
        on(Socket.EVENT_CONNECT, onConnect)
        on("joinedRoom", onRoomJoined)
        // ...
    }
}
```

### ✅ 4. Logs de Diagnostic Détaillés
- Tous les points critiques loggés
- États UI loggés dans ChatConversationScreen
- États ViewModel loggés avec marqueurs ⚠️

## 📈 Procédure de Collecte des Logs

1. Activer Logcat dans Android Studio
2. Filtrer par tags :
   - `ChatViewModel`
   - `ChatConversationScreen`
   - `SocketService`
3. Reproduire le problème selon la checklist
4. Copier TOUS les logs depuis "🚀 LaunchedEffect" du premier accès jusqu'à "🚀 LaunchedEffect" de la réouverture
5. Analyser la valeur de `isSending` à chaque étape marquée ⚠️

## ❓ Questions à Répondre avec les Logs

1. **Dans `leaveRoom()`** : Quelle est la valeur de `isSending` AVANT le nettoyage ?
2. **Dans `connectAndJoinRoom()` (réouverture)** : Quelle est la valeur de `isSending` AVANT le nettoyage ?
3. **Dans `onJoinedRoom`** : Combien de fois cet événement est-il reçu ? (1x ou 2x ?)
4. **Dans `ChatConversationScreen`** : Quelle est la valeur de `isSending` affichée dans le LaunchedEffect lors de la réouverture ?
5. **Délai** : Combien de temps s'écoule entre "DisposableEffect onDispose" et "LaunchedEffect DÉCLENCHÉ" ?

## 🎯 Hypothèses de Bug

### Hypothèse 1 : Race Condition avec sendTimeoutJob
Le `sendTimeoutJob` de 10s n'est pas annulé correctement et modifie `isSending` APRÈS que l'utilisateur ait quitté et réouvert le chat.

**Test** : Attendre 12 secondes avant de réouvrir le chat

### Hypothèse 2 : État Capturé par Compose
Le `LaunchedEffect(sortieId)` capture un snapshot de `isSending` avant que `connectAndJoinRoom()` ne le nettoie.

**Test** : Vérifier le timing des logs UI vs ViewModel

### Hypothèse 3 : Callback `onMessageSent` Tardif
La confirmation `messageSent` arrive APRÈS `leaveRoom()`, réinitialisant `isSending` à false... mais trop tard car un nouvel état est déjà créé.

**Test** : Vérifier l'ordre des logs entre "📤 Émission leaveRoom" et "✅ MESSAGE SENT confirmation"

### Hypothèse 4 : ViewModel Non Réinitialisé
Le `ChatViewModel` n'est pas recréé entre les navigations (singleton ou scoped mal configuré).

**Test** : Ajouter log dans `init {}` du ViewModel pour voir s'il est recréé

## 🚀 Prochaines Étapes

1. Exécuter l'app avec les nouveaux logs
2. Reproduire le bug selon la checklist
3. Copier et analyser les logs complets
4. Répondre aux 5 questions ci-dessus
5. Identifier quelle hypothèse correspond aux logs observés
6. Appliquer la correction ciblée

## 📝 Format de Rapport de Bug

```
=== RAPPORT DE BUG CHAT BLOQUÉ ===

Date/Heure : [YYYY-MM-DD HH:MM]
Version : [commit hash ou version]

ÉTAPES DE REPRODUCTION :
1. [détail]
2. [détail]
...

LOGS COMPLETS :
[coller tous les logs filtrés]

RÉPONSES AUX QUESTIONS CRITIQUES :
1. isSending dans leaveRoom() AVANT nettoyage : [true/false]
2. isSending dans connectAndJoinRoom() AVANT nettoyage : [true/false]
3. Nombre de joinedRoom reçus : [1/2/plus]
4. isSending dans ChatConversationScreen LaunchedEffect : [true/false]
5. Délai entre onDispose et LaunchedEffect : [X ms]

HYPOTHÈSE CONFIRMÉE :
[Hypothèse 1/2/3/4 + justification]

CORRECTION PROPOSÉE :
[description de la correction à appliquer]
```

---

**🔴 IMPORTANT** : Ne pas modifier le code avant d'avoir collecté et analysé les logs complets selon cette procédure.

