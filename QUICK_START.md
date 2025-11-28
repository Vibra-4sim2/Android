# 🚀 Aide-mémoire rapide - Test envoi d'images

## ✅ Status : PRÊT POUR LES TESTS
Toutes les modifications sont terminées. Aucune erreur de compilation.

---

## 📋 Checklist avant de commencer

- [ ] Backend accessible : https://dam-4sim2.onrender.com
- [ ] Émulateur démarré (Android Studio)
- [ ] Téléphone réel connecté (Wi-Fi debugging)
- [ ] Deux comptes de test prêts (membres de la même sortie)

---

## 🎯 Test rapide (5 minutes)

### 1️⃣ Préparation (1 min)
```
Émulateur : Login avec user1@test.com
Téléphone : Login avec user2@test.com
Les deux : Ouvrir la même conversation
```

### 2️⃣ Vérification connexion (30 sec)
```
Les deux appareils : Vérifier pastille verte 🟢
Émulateur : Envoyer "Test 123"
Téléphone : Doit recevoir instantanément
```

### 3️⃣ Test image (2 min)
```
Émulateur :
  1. Clic 📎 (Attach)
  2. Clic 📷 (Image)
  3. Sélectionner une image
  4. Attendre l'upload (2-5 sec)
  5. ✅ Image apparaît

Téléphone :
  ✅ Image apparaît AUTOMATIQUEMENT (< 2 sec)
```

### 4️⃣ Test inverse (1 min)
```
Téléphone : Envoyer une autre image
Émulateur : ✅ Doit recevoir instantanément
```

---

## 🔍 Logs à surveiller

### ✅ Logs normaux (tout va bien) :
```
ChatConversation: 📸 Bouton Image cliqué
ImagePicker: ✅ Image sélectionnée: content://...
ChatConversation: ✅ Image valide, envoi en cours...
MessageRepository: 📤 Uploading image: photo.jpg
MessageRepository: ✅ Image uploaded: https://res.cloudinary.com/...
SocketService: 📤 Data envoyée: {"sortieId":"...","type":"image",...}
SocketService: ✅ Message sent confirmation: 67ab... (success: true)
SocketService: 📨 New message received: 67ab...
ChatViewModel: ✅ Message added to list (total: X)
```

### ❌ Logs d'erreur (problème) :
```
"❌ Non connecté au serveur" → Attendre 60 sec (cold start)
"❌ Error uploading image" → Vérifier token / backend
"❌ L'image est trop grande" → Fichier > 10 MB
"❌ Format d'image non supporté" → Utiliser JPG/PNG
```

---

## 🐛 Debug rapide

### Problème : Image ne s'envoie pas
```bash
# Vérifier connexion
adb logcat | Select-String "SocketService|ChatViewModel"

# Vérifier backend
curl https://dam-4sim2.onrender.com/api/health
```

### Problème : Image n'arrive pas sur l'autre appareil
```
1. Tester avec message texte d'abord
2. Vérifier pastille verte (🟢) sur les deux
3. Quitter/revenir dans la conversation
4. Vérifier les deux appareils sont dans la même sortie
```

### Problème : Upload échoue
```
1. Se déconnecter / reconnecter (token expiré ?)
2. Vérifier taille image (< 10 MB)
3. Tester avec une autre image
4. Vérifier logs backend (Render)
```

---

## 📊 Résultat attendu

### ✅ Test réussi :
```
[Émulateur] Sélection → Upload → Envoi → ✓
[Téléphone] Réception instantanée (< 2s) → ✓
[Les deux] Image bien affichée → ✓
[Logs] Aucune erreur → ✓
```

### ❌ Test échoué :
```
Voir GUIDE_TEST_IMAGES.md section "Debug en cas de problème"
```

---

## 🚀 Commandes utiles

### Build et Run :
```powershell
# Clean build
.\gradlew clean && .\gradlew build

# Run sur device (Android Studio)
Run → Sélectionner appareil → Run
```

### Logs en temps réel :
```powershell
# Filtrer les logs pertinents
adb logcat | Select-String "ChatViewModel|SocketService|ChatConversation"

# Effacer les logs
adb logcat -c
```

### Connecter téléphone Wi-Fi :
```powershell
adb pair 192.168.x.y:port    # Première fois
adb connect 192.168.x.y:5555 # Connexion
adb devices                   # Vérifier
```

---

## 📚 Documentation complète

- **RESUME_ENVOI_IMAGES.md** - Détails techniques complets
- **GUIDE_TEST_IMAGES.md** - Guide de test détaillé avec debug
- **CHANGEMENTS_FINAUX.md** - Récapitulatif des modifications

---

## 🎯 Points clés à retenir

### Architecture :
```
Sélection → Validation → Upload Cloudinary (REST)
  → URL retournée → Envoi via WebSocket
  → Broadcast temps réel → Tous les clients reçoivent
```

### Un seul event WebSocket :
```json
{
  "sortieId": "...",
  "type": "image",     // ← "text" pour texte, "image" pour image
  "mediaUrl": "https://res.cloudinary.com/..."
}
```

### Temps réel :
- ✅ Pas de polling
- ✅ Push instantané via Socket.IO
- ✅ Room = `sortie_${sortieId}`
- ✅ Broadcast à tous les membres connectés

---

## ✨ Résultat attendu

```
┌─────────────────┐         ┌─────────────────┐
│   Émulateur     │         │   Téléphone     │
│                 │         │                 │
│  [📎] [Image]   │         │                 │
│       ↓         │         │                 │
│  [⏳ Upload...] │         │                 │
│       ↓         │         │                 │
│  [✅ 🖼️ Envoyé] │   →→→   │  [✅ 🖼️ Reçu]   │
│                 │  < 2s   │  Instantané !   │
└─────────────────┘         └─────────────────┘
```

🎉 **C'est parti pour les tests !**

---

**Date** : 2025-01-26  
**Status** : ✅ READY  
**Version** : 1.0

