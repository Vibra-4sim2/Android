# 🔍 Vérification Configuration Google Cloud Console

## ❌ Erreur Actuelle
```
Authorization failure. StatusCode=INVALID_ARGUMENT
API Key: AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o
Android Application: F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13;com.example.dam
```

Cette erreur signifie que **votre clé API n'est PAS ENCORE autorisée** sur Google Cloud Console.

---

## ✅ SOLUTION ÉTAPE PAR ÉTAPE (10 minutes)

### 🔑 Informations dont vous avez besoin :
```
Clé API      : AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o
Package      : com.example.dam
SHA-1        : F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13
```

---

## 📋 ÉTAPE 1 : Aller sur Google Cloud Console

1. **Ouvrez** : https://console.cloud.google.com/
2. **Connectez-vous** avec votre compte Google
3. **Sélectionnez votre projet** : `damm-d8e73` (ou créez-en un si nécessaire)

---

## 📋 ÉTAPE 2 : Activer "Maps SDK for Android"

### Option A : Via la recherche rapide
1. Dans la barre de recherche en haut, tapez : **"Maps SDK for Android"**
2. Cliquez sur le résultat
3. Si vous voyez un bouton **"ENABLE"** ou **"ACTIVER"**, cliquez dessus
4. Attendez quelques secondes

### Option B : Via le menu
1. Cliquez sur **☰ Menu** (en haut à gauche)
2. Allez dans **APIs & Services** > **Library**
3. Dans la barre de recherche, tapez : **"Maps SDK for Android"**
4. Cliquez sur **"Maps SDK for Android"**
5. Cliquez sur **"ENABLE"** / **"ACTIVER"**

### ✅ Vérification
Vous devriez voir : **"API enabled"** avec une coche verte ✅

---

## 📋 ÉTAPE 3 : Créer ou Configurer une Clé API

### Si vous n'avez PAS ENCORE de clé API :

1. Allez dans **APIs & Services** > **Credentials**
2. Cliquez sur **+ CREATE CREDENTIALS** en haut
3. Sélectionnez **API key**
4. Une nouvelle clé sera générée (copiez-la)
5. Remplacez l'ancienne clé dans votre code
6. Passez à l'ÉTAPE 4

### Si vous avez DÉJÀ la clé `AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o` :

1. Allez dans **APIs & Services** > **Credentials**
2. **Trouvez votre clé** dans la liste (elle commence par `AIzaSy...`)
3. Cliquez sur **l'icône crayon ✏️** à droite pour éditer
4. Passez à l'ÉTAPE 4

---

## 📋 ÉTAPE 4 : Configurer les Restrictions Android

### 4.1 Application Restrictions

1. Descendez jusqu'à **"Application restrictions"**
2. Sélectionnez **"Android apps"**
3. Cliquez sur **"+ Add an item"**
4. Remplissez :
   ```
   Package name: com.example.dam
   SHA-1 certificate fingerprint: F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13
   ```
5. Cliquez sur **"Done"**

### 4.2 API Restrictions

1. Descendez jusqu'à **"API restrictions"**
2. Sélectionnez **"Restrict key"**
3. **COCHEZ** ces APIs dans la liste :
   - ✅ **Maps SDK for Android** (OBLIGATOIRE)
   - ✅ **Directions API** (si vous utilisez les itinéraires)
   - ✅ **Geocoding API** (si vous utilisez les adresses)
   - ✅ **Places API** (optionnel)

4. Cliquez sur **"Save"** en bas

### ⏱️ Attendez 5 minutes
Les modifications peuvent prendre **jusqu'à 5 minutes** pour être effectives.

---

## 📋 ÉTAPE 5 : Activer les APIs supplémentaires

Retournez dans **APIs & Services** > **Library** et activez aussi :

1. **Directions API** (pour les itinéraires)
   - Recherchez "Directions API"
   - Cliquez dessus
   - Cliquez sur **"ENABLE"**

2. **Geocoding API** (pour les conversions adresse ↔ coordonnées)
   - Recherchez "Geocoding API"
   - Cliquez dessus
   - Cliquez sur **"ENABLE"**

---

## 📋 ÉTAPE 6 : Vérifier la Facturation (IMPORTANT !)

⚠️ **Google Maps nécessite un compte de facturation**, même avec le crédit gratuit de 200$/mois.

1. Allez dans **☰ Menu** > **Billing**
2. Si vous voyez **"This project has no billing account"** :
   - Cliquez sur **"Link a billing account"**
   - Suivez les étapes pour ajouter une carte bancaire
   - Ne vous inquiétez pas : **les 200$ gratuits/mois couvrent la plupart des usages**

3. Si vous avez déjà un compte de facturation, vérifiez qu'il est **actif**

---

## 🧪 ÉTAPE 7 : Tester votre Application

Après avoir configuré Google Cloud Console :

### 1. Nettoyez et recompilez
```powershell
cd "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"
.\gradlew clean assembleDebug
```

### 2. Désinstallez l'ancienne version
```powershell
adb uninstall com.example.dam
```

### 3. Installez la nouvelle version
```powershell
.\gradlew installDebug
```

### 4. Lancez l'app et vérifiez les logs
```powershell
adb logcat | findstr "Google"
```

**Résultat attendu** : Plus d'erreur "Authorization failure" ✅

---

## 🔍 VÉRIFICATION : Checklist Complète

### Sur Google Cloud Console :
- [ ] Mon projet est sélectionné : `damm-d8e73`
- [ ] **Maps SDK for Android** est activé (état : "API enabled")
- [ ] **Directions API** est activé
- [ ] **Geocoding API** est activé
- [ ] J'ai créé/trouvé ma clé API : `AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o`
- [ ] J'ai configuré les **Application restrictions** :
  - Type : Android apps
  - Package : `com.example.dam`
  - SHA-1 : `F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13`
- [ ] J'ai configuré les **API restrictions** :
  - Maps SDK for Android ✅
  - Directions API ✅
  - Geocoding API ✅
- [ ] J'ai cliqué sur **"Save"**
- [ ] J'ai attendu **5 minutes**
- [ ] Mon compte de **facturation est actif**

### Dans mon code :
- [x] La clé API est dans `AndroidManifest.xml` ✅
- [x] La clé API est dans `strings.xml` ✅
- [x] Le SHA-1 correspond ✅
- [x] Le package name correspond ✅

---

## 🆘 SI ÇA NE MARCHE TOUJOURS PAS

### Erreur : "API not enabled"
➡️ Retournez à l'ÉTAPE 2 et vérifiez que "Maps SDK for Android" est bien activé

### Erreur : "This API project is not authorized"
➡️ Vérifiez l'ÉTAPE 4 : Les restrictions Android (Package + SHA-1) doivent être exactes

### Erreur : "Billing account required"
➡️ Allez à l'ÉTAPE 6 : Configurez la facturation (obligatoire)

### Erreur persiste après 5 minutes
1. Vérifiez que vous avez bien cliqué sur **"Save"**
2. Vérifiez que la clé API est dans le bon projet
3. Essayez de créer une **nouvelle clé API** et remplacez l'ancienne

---

## 📸 Captures d'Écran Attendues

### 1. APIs & Services > Library > Maps SDK for Android
```
✅ API enabled
[MANAGE] [TRY THIS API]
```

### 2. APIs & Services > Credentials > Votre clé API
```
Application restrictions: Android apps
  • com.example.dam (F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13)

API restrictions: 
  • Maps SDK for Android
  • Directions API
  • Geocoding API
```

### 3. Billing
```
✅ Billing account: [Nom de votre compte]
Status: Active
```

---

## 🎯 RÉSUMÉ RAPIDE (Si vous êtes pressé)

1. **Allez sur** : https://console.cloud.google.com/
2. **Activez** : Maps SDK for Android
3. **Configurez votre clé API** avec :
   - Android apps
   - Package : `com.example.dam`
   - SHA-1 : `F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13`
4. **Activez** : Directions API + Geocoding API
5. **Configurez** : La facturation (si pas déjà fait)
6. **Attendez** : 5 minutes
7. **Testez** : Rebuild + Réinstaller l'app

---

## 📞 Liens Utiles

- **Console Google Cloud** : https://console.cloud.google.com/
- **Documentation Maps Android** : https://developers.google.com/maps/documentation/android-sdk/start
- **Gestion API Keys** : https://console.cloud.google.com/apis/credentials
- **Facturation** : https://console.cloud.google.com/billing

---

**📌 IMPORTANT** : Sans configuration Google Cloud Console, votre app **ne pourra jamais** afficher Google Maps, même si le code est correct.

**Date** : 14 Décembre 2025

