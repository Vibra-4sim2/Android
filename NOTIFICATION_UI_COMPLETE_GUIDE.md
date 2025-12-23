# 🎉 Système de Notifications Complet - Guide d'Utilisation

## ✅ Fonctionnalités Implémentées

### 1. **Icône de Notification dans le TopBar** 🔔

L'icône de notification avec badge apparaît maintenant dans le TopBar en haut à droite, à côté du menu déroulant.

**Caractéristiques :**
- ✅ Badge rouge avec le nombre de notifications non lues
- ✅ Affiche "99+" si plus de 99 notifications
- ✅ Disparaît quand il n'y a aucune notification non lue
- ✅ Mise à jour automatique du compteur

**Position :**
```
┌─────────────────────────────────────┐
│ V!BRA              🔔(3)  ⬇️        │  ← TopBar
│ Explore Adventures                  │
└─────────────────────────────────────┘
```

### 2. **Écran Liste des Notifications** 📋

Un écran complet affichant toutes vos notifications avec :

**Interface :**
- ✅ **Titre** : Nom de l'utilisateur et action
- ✅ **Contenu** : Message détaillé
- ✅ **Date** : Timestamp relatif (ex: "2h", "3 min", "À l'instant")
- ✅ **Icône colorée** selon le type de notification
- ✅ **Badge "non lu"** : Point bleu pour les notifications non lues
- ✅ **Design Material 3** avec cards et animations

**Types de Notifications avec Icônes :**

| Type | Icône | Couleur | Description |
|------|-------|---------|-------------|
| **Nouvelle Publication** | ➕ | Bleu | Une nouvelle publication est disponible |
| **Message Chat** | 📧 | Vert | Nouveau message dans une discussion |
| **Nouvelle Sortie** | 📍 | Orange | Une nouvelle sortie/aventure est créée |
| **Participation Acceptée** | ✅ | Vert | Votre demande a été acceptée |
| **Participation Refusée** | ❌ | Rouge | Votre demande a été refusée |
| **Test** | ℹ️ | Gris | Notification de test |

### 3. **Navigation Intelligente** 🎯

**Chaque notification est cliquable** et vous redirige automatiquement vers :

#### **Publication** (NEW_PUBLICATION)
```
Clic → Écran Détail Publication
Route: publicationDetail/{publicationId}
```

#### **Message Chat** (CHAT_MESSAGE)
```
Clic → Écran de Chat/Conversation
Route: chatConversation/{sortieId}/...
Affiche directement la conversation du groupe
```

#### **Sortie** (NEW_SORTIE)
```
Clic → Écran Détail Sortie
Route: sortieDetail/{sortieId}
```

#### **Participation** (ACCEPTED/REJECTED)
```
Clic → Écran Détail Sortie
Route: sortieDetail/{sortieId}
Affiche la sortie pour laquelle votre participation a été traitée
```

### 4. **Système de Stockage** 💾

**Les notifications sont stockées** de deux façons :

#### **Backend (MongoDB)**
- ✅ Toutes les notifications sont sauvegardées dans la base de données
- ✅ Associées à chaque utilisateur (`userId`)
- ✅ Marquées comme "lues" ou "non lues" (`isRead`)
- ✅ Horodatées avec `createdAt` et `readAt`
- ✅ **Persistantes** : disponibles même après redémarrage de l'app

#### **Cache Local (ViewModel)**
- ✅ Les notifications sont chargées dans le `NotificationViewModel`
- ✅ Mises à jour en temps réel via StateFlow
- ✅ Optimisation : évite de recharger inutilement

### 5. **Marquage Automatique comme "Lu"** ✔️

**Deux moments de marquage :**

1. **Quand la notification Android est affichée**
   ```kotlin
   Notification reçue → Affichée → Marquée comme lue automatiquement
   ```

2. **Quand l'utilisateur clique dans l'écran de liste**
   ```kotlin
   Clic sur notification → Navigation → Marquée comme lue
   → Retirée de la liste des non lues
   → Compteur décrémenté
   ```

---

## 🎮 Comment Utiliser

### Accéder aux Notifications

#### **Méthode 1 : Via l'Icône TopBar** (Recommandé)
1. Depuis n'importe quel écran de l'app
2. Cliquez sur l'icône 🔔 en haut à droite
3. L'écran des notifications s'ouvre

#### **Méthode 2 : Via la Route (Programmation)**
```kotlin
navController.navigate("notifications")
```

### Voir le Compteur de Notifications

Le badge rouge sur l'icône 🔔 affiche le nombre de notifications non lues :
- **Aucun badge** : Pas de notifications
- **Badge (3)** : 3 notifications non lues
- **Badge (99+)** : Plus de 99 notifications

### Consulter une Notification

1. Ouvrez l'écran des notifications
2. **Scrollez** pour voir toutes les notifications
3. **Cliquez** sur une notification
4. Vous serez redirigé vers l'écran approprié
5. La notification est automatiquement marquée comme lue

### Rafraîchir la Liste

L'écran se rafraîchit automatiquement mais vous pouvez aussi :
- Le compteur se met à jour à chaque changement de route
- Les nouvelles notifications apparaissent toutes les 15 secondes (polling)

---

## 🎨 Design de l'Interface

### Écran des Notifications

```
┌─────────────────────────────────────┐
│ ← Notifications               🔄     │  ← TopBar avec indicateur polling
├─────────────────────────────────────┤
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 📍 Jean Dupont                │  │
│  │    Nouvelle sortie créée      │  │  ← Card notification
│  │    "Weekend à la montagne"    │  │
│  │    2h                      ●  │  │  ← Badge non lu
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 📧 Marie Dubois • Weekend    │  │
│  │    "Salut tout le monde!"     │  │
│  │    15 min                     │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ ✅ Admin                      │  │
│  │    Participation acceptée     │  │
│  │    "Votre demande acceptée"   │  │
│  │    1j                         │  │
│  └──────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

### États de l'Interface

#### **Chargement**
```
┌─────────────────────────────────────┐
│         ⏳ Chargement...            │
└─────────────────────────────────────┘
```

#### **Liste Vide**
```
┌─────────────────────────────────────┐
│           🔔                         │
│    Aucune notification              │
│    Vous êtes à jour !               │
└─────────────────────────────────────┘
```

#### **Erreur**
```
┌─────────────────────────────────────┐
│           ⚠️                         │
│         Erreur                       │
│    Message d'erreur                 │
│    [  Réessayer  ]                  │
└─────────────────────────────────────┘
```

---

## 🔄 Flux Complet

### Réception d'une Nouvelle Notification

```
1. ⚡ Événement Backend
   ↓
   Un utilisateur crée une publication, envoie un message, etc.

2. 💾 Sauvegarde Backend
   ↓
   Notification stockée dans MongoDB avec userId, type, data, etc.

3. 📡 Polling (15 secondes)
   ↓
   App Android appelle GET /notifications
   Backend retourne les nouvelles notifications

4. 🔔 Notification Android Locale
   ↓
   Affichage dans la barre de notification du téléphone
   Son/vibration (si activé)

5. ✅ Marquage comme "Lu"
   ↓
   PATCH /notifications/{id}/read
   Notification marquée dans la base

6. 🔢 Mise à Jour Badge
   ↓
   Compteur mis à jour dans le TopBar
   Badge rouge avec nouveau nombre

7. 👆 Clic Utilisateur (optionnel)
   ↓
   Ouverture de l'écran des notifications
   OU
   Clic direct sur notification Android

8. 🎯 Navigation
   ↓
   Redirection vers l'écran approprié
   (Chat, Sortie, Publication, etc.)
```

---

## 📊 Architecture Technique

### Composants Principaux

```
┌─────────────────────────────────────┐
│         NotificationViewModel       │
│  - notifications: List<Notification>│
│  - unreadCount: Int                 │
│  - loadNotifications()              │
│  - markAsRead()                     │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│      NotificationRepository         │
│  - getNotifications()               │
│  - markAsRead()                     │
│  - getUnreadCount()                 │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│     NotificationApiService          │
│  GET /notifications                 │
│  PATCH /notifications/{id}/read     │
│  GET /notifications/unread-count    │
└─────────────────────────────────────┘
```

### Intégration dans TabBarView

```kotlin
// ViewModel
val notificationViewModel: NotificationViewModel = viewModel()
val unreadNotifCount by notificationViewModel.unreadCount.collectAsState()

// Chargement au démarrage
LaunchedEffect(Unit) {
    notificationViewModel.loadUnreadCount(context)
}

// Badge dans TopBar
BadgedBox(
    badge = {
        if (unreadNotifCount > 0) {
            Badge { Text("$unreadNotifCount") }
        }
    }
) {
    Icon(Icons.Default.Notifications, "Notifications")
}
```

---

## 🧪 Scénarios de Test

### Test 1 : Voir le Badge
1. ✅ Lancez l'app et connectez-vous
2. ✅ Depuis un autre compte, créez une publication
3. ✅ Attendez 15 secondes max
4. ✅ Le badge (1) apparaît sur l'icône 🔔

### Test 2 : Ouvrir l'Écran Notifications
1. ✅ Cliquez sur l'icône 🔔 en haut à droite
2. ✅ L'écran des notifications s'ouvre
3. ✅ Vous voyez la liste des notifications avec icônes et dates

### Test 3 : Navigation vers Chat
1. ✅ Envoyez un message dans un chat (depuis un autre compte)
2. ✅ Recevez la notification
3. ✅ Cliquez sur la notification dans la liste
4. ✅ Vous êtes redirigé vers l'écran de chat
5. ✅ La notification disparaît de la liste des non lues

### Test 4 : Navigation vers Sortie
1. ✅ Créez une nouvelle sortie (depuis un autre compte)
2. ✅ Recevez la notification "Nouvelle sortie"
3. ✅ Cliquez dessus
4. ✅ Vous êtes sur l'écran détail de la sortie

### Test 5 : Badge Décrémente
1. ✅ Badge affiche (5)
2. ✅ Cliquez sur une notification
3. ✅ Badge devient (4)
4. ✅ Cliquez sur toutes les notifications
5. ✅ Badge disparaît

---

## 🎯 Points Clés

✅ **Icône avec badge** dans le TopBar (en haut à droite)  
✅ **Écran dédié** listant toutes les notifications  
✅ **Stockage backend** dans MongoDB par utilisateur  
✅ **Navigation intelligente** vers les bons écrans  
✅ **Cliquable** : chaque notification redirige  
✅ **Marquage automatique** comme lu  
✅ **Compteur en temps réel** mis à jour automatiquement  
✅ **Design professionnel** avec icônes et couleurs  
✅ **Dates relatives** (ex: "2h", "15 min", "À l'instant")  
✅ **Polling automatique** toutes les 15 secondes  

---

## 🎉 Résumé

Vous avez maintenant un **système de notifications complet** comme Facebook ou Instagram :

- 🔔 **Icône dans le TopBar** avec badge
- 📋 **Écran de liste** avec toutes les notifications
- 💾 **Stockage backend** par utilisateur
- 🎯 **Navigation automatique** vers les écrans
- ✅ **Marquage comme lu** automatique
- 🔢 **Compteur en temps réel**

**Tout fonctionne, tout est cliquable, tout est stocké ! 🚀**

---

**Date de mise à jour :** 14 décembre 2025  
**Version :** 2.0.0  
**Statut :** ✅ Production Ready avec UI complète

