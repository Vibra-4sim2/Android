# 🧪 Guide de test rapide - Envoi d'images en temps réel

## 🚀 Avant de commencer

### Prérequis :
- ✅ Backend NestJS déployé sur Render : `https://dam-4sim2.onrender.com`
- ✅ Cloudinary configuré côté backend
- ✅ Deux appareils de test prêts (émulateur + téléphone réel)

---

## 📱 Configuration des appareils

### Appareil 1 : Émulateur Android Studio
```
1. Ouvrir Android Studio
2. Device Manager → Sélectionner un émulateur (ex: Pixel 8 Pro)
3. Run l'application
```

### Appareil 2 : Téléphone réel (via Wi-Fi)
```
1. Sur le téléphone :
   - Paramètres → Options développeur
   - Activer "Wireless debugging"
   - Noter l'IP:Port affichée (ex: 192.168.1.100:5555)

2. Sur Android Studio (Device Manager) :
   - Cliquer "Pair devices using Wi-Fi"
   - Scanner le QR code OU entrer le code de pairage
   - Le téléphone apparaît dans la liste

3. Sélectionner le téléphone et Run l'application
```

**Alternative avec ADB** :
```powershell
adb pair 192.168.x.y:port    # Remplacer par l'IP:Port du téléphone
adb connect 192.168.x.y:5555
adb devices                   # Vérifier que le device est listé
```

---

## 🧪 Scénario de test

### Étape 1 : Préparation
```
1. Sur appareil 1 : Se connecter avec le compte A (ex: user1@test.com)
2. Sur appareil 2 : Se connecter avec le compte B (ex: user2@test.com)
3. Les deux comptes doivent être membres de la même sortie
4. Ouvrir la même conversation sur les deux appareils
```

### Étape 2 : Vérification de la connexion
```
✅ Sur les deux appareils, vérifier :
   - Pastille verte "🟢" dans le header
   - Texte "X participants" (pas "Connexion...")
   - Les anciens messages sont chargés
```

### Étape 3 : Test de texte (vérifier que le temps réel fonctionne)
```
1. Sur appareil 1 : Envoyer "Test 123"
2. Sur appareil 2 : Le message doit apparaître INSTANTANÉMENT
3. Si ça fonctionne → Passer à l'étape suivante
   Si ça ne fonctionne pas → Voir section Debug ci-dessous
```

### Étape 4 : Test d'envoi d'image
```
1. Sur appareil 1 :
   a. Cliquer sur le bouton 📎 (Attach) en bas à gauche
   b. Cliquer sur "📷 Image" dans le panneau qui s'ouvre
   c. Sélectionner une image depuis la galerie
   d. Attendre l'indicateur de chargement (CircularProgressIndicator)
   e. L'image doit apparaître dans la conversation

2. Sur appareil 2 :
   ✅ L'image doit apparaître AUTOMATIQUEMENT et INSTANTANÉMENT
   ✅ Pas besoin de rafraîchir ou de recharger
   ✅ L'image est cliquable et bien affichée
```

### Étape 5 : Test dans l'autre sens
```
1. Sur appareil 2 : Envoyer une autre image
2. Sur appareil 1 : L'image doit apparaître instantanément
```

### Étape 6 : Tests de validation
```
1. Tester avec une image > 10 MB :
   ✅ Doit afficher : "L'image est trop grande. Taille maximale: 10 MB"

2. Tester avec un fichier non-image (si possible) :
   ✅ Doit afficher : "Format d'image non supporté"

3. Annuler la sélection d'image :
   ✅ Doit afficher : "Aucune image sélectionnée"
```

---

## 🔍 Points à vérifier

### Interface utilisateur :
- ✅ Le bouton 📎 est accessible et cliquable
- ✅ Le panneau d'attachement s'ouvre avec animation
- ✅ Le bouton "📷 Image" est visible et réactif
- ✅ La galerie Android s'ouvre correctement
- ✅ L'indicateur de chargement apparaît pendant l'upload
- ✅ L'image apparaît dans la bulle de message
- ✅ L'image est bien affichée (pas d'erreur de chargement)
- ✅ Le timestamp et le statut (✓✓) sont affichés

### Temps réel :
- ✅ L'autre appareil reçoit l'image SANS recharger
- ✅ Délai < 2 secondes entre l'envoi et la réception
- ✅ L'image apparaît au bon endroit dans la conversation
- ✅ Le scroll automatique fonctionne (vers le bas)

### Logs Logcat (appareil qui envoie) :
```
🖼️ Image sélectionnée: content://...
✅ File created: /data/user/0/.../cache/photo.jpg (1234567 bytes)
✅ Image valide, envoi en cours...
📤 Uploading image: photo.jpg
✅ Image uploaded: https://res.cloudinary.com/...
📤 Data envoyée: {"sortieId":"...","type":"image","mediaUrl":"..."}
✅ Message sent confirmation: 67ab... (success: true)
```

### Logs Logcat (appareil qui reçoit) :
```
📨 New message received: 67ab...
✅ Message added to list (total: X)
```

---

## 🐛 Debug en cas de problème

### ❌ Problème : L'image picker ne s'ouvre pas
```
Vérification :
1. Permissions accordées ? (STORAGE)
2. Galerie installée sur l'appareil ?
3. Vérifier les logs : "📸 Bouton Image cliqué"

Solution :
- Réinstaller l'app
- Accorder les permissions manuellement
- Redémarrer l'appareil
```

### ❌ Problème : "Non connecté au serveur"
```
Vérification :
1. Le backend est-il accessible ? (https://dam-4sim2.onrender.com)
2. Le token JWT est-il valide ? (se reconnecter)
3. La connexion internet fonctionne-t-elle ?
4. Render cold start ? (attendre 60 secondes)

Logs à vérifier :
- "🔌 Connexion au serveur Socket.IO..."
- "✅ Socket connected"
- "🏠 Tentative de rejoindre la room"
- "✅ Demande de join envoyée"

Solution :
- Se déconnecter et se reconnecter
- Redémarrer l'app
- Attendre que Render démarre (1ère connexion)
```

### ❌ Problème : "Échec de l'upload"
```
Vérification :
1. Token JWT valide ?
2. Backend accessible ?
3. Cloudinary configuré ?
4. Taille de l'image < 10 MB ?

Logs à vérifier :
- "❌ Error uploading image: ..."
- Code d'erreur HTTP (401, 500, etc.)

Solution :
- Se reconnecter
- Vérifier la taille de l'image
- Tester avec une autre image
- Vérifier les logs backend
```

### ❌ Problème : L'image s'envoie mais n'apparaît pas sur l'autre appareil
```
Vérification :
1. Les deux appareils sont dans la même room ?
2. Les deux appareils sont connectés (pastille verte) ?
3. Le WebSocket fonctionne (tester avec un message texte) ?

Logs appareil 1 (envoi) :
- "📤 Data envoyée: ..."
- "✅ Message sent confirmation"

Logs appareil 2 (réception) :
- "📨 New message received: ..." (doit apparaître !)

Solution :
- Forcer le refresh : quitter la conversation et revenir
- Vérifier les logs backend :
  "🔔 Broadcast receiveMessage to room sortie_XXX"
- Redémarrer les deux apps
```

### ❌ Problème : L'image est reçue mais ne s'affiche pas
```
Vérification :
1. L'URL Cloudinary est-elle valide ?
2. Coil est-il configuré ?
3. La connexion internet fonctionne-t-elle ?

Test manuel :
- Copier l'URL Cloudinary depuis les logs
- Ouvrir l'URL dans un navigateur
- Si l'image s'affiche → Problème Coil
- Si l'image ne s'affiche pas → Problème Cloudinary

Solution :
- Vérifier la connexion internet
- Vérifier la configuration Cloudinary backend
- Nettoyer le cache Coil (réinstaller l'app)
```

---

## 📊 Métriques de performance

### Temps attendus :
- **Sélection d'image** : < 1 seconde
- **Validation** : < 100 ms
- **Upload vers Cloudinary** : 2-5 secondes (selon taille et connexion)
- **Envoi via WebSocket** : < 100 ms
- **Réception sur autre appareil** : < 500 ms
- **Total** : 3-7 secondes de bout en bout

### Tailles d'images recommandées :
- **Optimale** : 500 KB - 2 MB
- **Acceptable** : 2 MB - 5 MB
- **Limite** : 10 MB

---

## ✅ Checklist de test complète

### Tests fonctionnels :
- [ ] Sélection d'image depuis galerie
- [ ] Validation de format (JPG, PNG)
- [ ] Validation de taille (< 10 MB)
- [ ] Upload vers Cloudinary
- [ ] Envoi via WebSocket
- [ ] Réception en temps réel
- [ ] Affichage de l'image
- [ ] Indicateur de chargement
- [ ] Message d'erreur si échec
- [ ] Annulation de sélection

### Tests temps réel (2 appareils) :
- [ ] Message texte apparaît instantanément
- [ ] Image apparaît instantanément
- [ ] Pas de décalage > 2 secondes
- [ ] Scroll automatique vers le bas
- [ ] Statut du message (✓✓)

### Tests de robustesse :
- [ ] Image > 10 MB (doit être rejetée)
- [ ] Mauvais format (doit être rejeté)
- [ ] Connexion internet coupée (gestion d'erreur)
- [ ] Token expiré (reconnexion)
- [ ] Backend indisponible (gestion d'erreur)
- [ ] Plusieurs images successives

### Tests multi-utilisateurs :
- [ ] 2 utilisateurs envoient en même temps
- [ ] 3+ utilisateurs dans la même room
- [ ] Quitter/revenir dans la conversation
- [ ] Tuer l'app et revenir

---

## 🎯 Résultat attendu

### ✅ Test réussi si :
1. ✅ L'image s'envoie sans erreur
2. ✅ L'autre appareil reçoit l'image en < 2 secondes
3. ✅ L'image s'affiche correctement
4. ✅ Aucune erreur dans les logs
5. ✅ Le comportement est identique au message texte (mais avec une image)

### ❌ Test échoué si :
- ❌ "Non connecté au serveur" après 60 secondes
- ❌ "Échec de l'upload" avec Cloudinary
- ❌ L'image ne s'affiche pas sur l'autre appareil
- ❌ Délai > 10 secondes
- ❌ Crash de l'application

---

## 📝 Rapport de test

Après les tests, noter :

```
✅ Tests réussis :
- [x] Envoi d'image JPG (2 MB)
- [x] Réception en temps réel (< 2s)
- [x] Affichage correct

❌ Tests échoués :
- [ ] ...

⚠️ Problèmes rencontrés :
- ...

⏱️ Temps mesurés :
- Upload : X secondes
- Réception : X ms
- Total : X secondes
```

---

## 🚀 Commandes utiles

### Logs Logcat en temps réel :
```powershell
# Tous les logs de l'app
adb logcat -s ChatViewModel SocketService ChatConversation ImagePicker

# Filtrer les erreurs uniquement
adb logcat *:E

# Nettoyer les logs
adb logcat -c
```

### Redémarrer l'app sans rebuild :
```
Stop l'app → Run (Android Studio)
```

### Nettoyer le build :
```powershell
cd C:\Users\cyrin\AndroidStudioProjects\latest_clone\Android
.\gradlew clean
.\gradlew build
```

---

🎉 **Bon test !** 

Si tout fonctionne comme prévu, tu devrais voir l'image apparaître instantanément sur l'autre appareil. C'est magique ! ✨

