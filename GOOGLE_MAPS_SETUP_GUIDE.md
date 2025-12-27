# 🗺️ Guide de Configuration Google Maps API

## ✅ Configuration Actuelle dans l'Application

### 1. Clé API Google Maps
```
AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o
```

### 2. Package Name
```
com.example.dam
```

### 3. SHA-1 Certificate Fingerprint (Debug)
```
F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13
```

### 4. SHA-256 Certificate Fingerprint (Debug)
```
B1:78:77:7E:ED:B6:EE:9F:C8:B2:92:5F:7D:59:0F:9E:B2:24:A0:A1:61:25:68:D5:31:41:05:48:E9:02:3C:EB
```

---

## 📋 Configuration à Faire sur Google Cloud Console

### Étape 1 : Accéder à Google Cloud Console
1. Allez sur : https://console.cloud.google.com/
2. Sélectionnez ou créez votre projet : **damm-d8e73**

### Étape 2 : Activer les APIs Nécessaires
Allez dans **APIs & Services** > **Library** et activez :

- ✅ **Maps SDK for Android** (OBLIGATOIRE)
- ✅ **Directions API** (pour les itinéraires)
- ✅ **Geocoding API** (pour la conversion adresse ↔ coordonnées)
- ✅ **Places API** (optionnel, pour la recherche de lieux)
- ✅ **Geolocation API** (optionnel)

### Étape 3 : Créer/Configurer la Clé API

#### A. Si vous devez créer une nouvelle clé :
1. Allez dans **APIs & Services** > **Credentials**
2. Cliquez sur **+ CREATE CREDENTIALS** > **API Key**
3. Une nouvelle clé sera générée
4. Cliquez sur **RESTRICT KEY** pour la configurer

#### B. Si vous configurez la clé existante :
1. Allez dans **APIs & Services** > **Credentials**
2. Trouvez votre clé : `AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o`
3. Cliquez sur l'icône **Edit** (✏️ crayon)

### Étape 4 : Configurer les Restrictions Android

#### Application Restrictions
1. Sélectionnez **Android apps**
2. Cliquez sur **+ Add an item**
3. Entrez :
   - **Package name** : `com.example.dam`
   - **SHA-1 certificate fingerprint** : `F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13`
4. Cliquez sur **Done**

⚠️ **Important** : Pour une app en production, vous devrez aussi ajouter le SHA-1 de votre keystore de release.

#### API Restrictions
1. Sélectionnez **Restrict key**
2. Cochez les APIs que vous utilisez :
   - ✅ Maps SDK for Android
   - ✅ Directions API
   - ✅ Geocoding API
   - ✅ Places API (si utilisé)

### Étape 5 : Sauvegarder
1. Cliquez sur **SAVE**
2. ⏱️ Attendez **5 minutes** pour que les modifications prennent effet

---

## 🔧 Fichiers Configurés dans l'Application

### ✅ AndroidManifest.xml
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o" />
```

### ✅ strings.xml
```xml
<string name="google_maps_key">AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o</string>
```

### ✅ GoogleRetrofitInstance.kt
```kotlin
private const val API_KEY = "AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o"
```

---

## 🧪 Comment Tester

### 1. Vérifier la Configuration
Après avoir configuré Google Cloud Console, testez votre app :

```bash
# Clean et rebuild
./gradlew clean assembleDebug

# Installer sur l'appareil
./gradlew installDebug
```

### 2. Vérifier les Logs
Si vous voyez encore des erreurs, vérifiez les logs Android :

```bash
adb logcat | findstr "Google.*Maps"
```

### 3. Erreurs Courantes

#### Erreur : "API key not found"
- ✅ Vérifiez que la clé est bien dans `AndroidManifest.xml`
- ✅ Nettoyez et rebuilder le projet
- ✅ Désinstallez et réinstallez l'app

#### Erreur : "This API key is not authorized to use this service or API"
- ✅ Vérifiez que "Maps SDK for Android" est activé
- ✅ Vérifiez les restrictions Android (Package name + SHA-1)
- ✅ Attendez 5 minutes après la sauvegarde

#### Erreur : "The provided API key is invalid"
- ✅ Vérifiez que vous avez copié la clé correctement
- ✅ Pas d'espaces avant/après la clé
- ✅ La clé doit être active dans Google Cloud Console

---

## 🔑 Pour Obtenir un Nouveau SHA-1

Si vous avez besoin de générer un nouveau certificat ou de vérifier le SHA-1 :

### Debug Keystore
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### Release Keystore (si vous en avez un)
```powershell
keytool -list -v -keystore path/to/your/release.keystore -alias your_alias
```

---

## 📞 Support

Si vous avez toujours des problèmes :

1. **Vérifiez Google Cloud Console** : https://console.cloud.google.com/
   - Quota & System > Quotas
   - Vérifiez que vous n'avez pas dépassé les quotas

2. **Documentation officielle** :
   - https://developers.google.com/maps/documentation/android-sdk/start
   - https://developers.google.com/maps/documentation/android-sdk/config

3. **Vérifiez votre facturation** :
   - Google Maps API nécessite un compte de facturation actif
   - Même avec le crédit gratuit, vous devez configurer la facturation

---

## ✅ Checklist de Vérification

- [ ] La clé API est créée dans Google Cloud Console
- [ ] "Maps SDK for Android" est activé
- [ ] "Directions API" est activé
- [ ] "Geocoding API" est activé
- [ ] Les restrictions Android sont configurées (Package + SHA-1)
- [ ] La clé est dans `AndroidManifest.xml`
- [ ] La clé est dans `strings.xml`
- [ ] Le projet a été nettoyé et recompilé
- [ ] L'app a été désinstallée et réinstallée
- [ ] J'ai attendu 5 minutes après la configuration
- [ ] La facturation est configurée sur Google Cloud

---

## 🎉 Une fois que tout fonctionne

Votre application devrait maintenant :
- ✅ Afficher Google Maps correctement
- ✅ Utiliser l'API Directions pour les itinéraires
- ✅ Utiliser l'API Geocoding pour les adresses
- ✅ Ne plus afficher d'erreurs dans les logs

**Date de dernière mise à jour** : 14 décembre 2025

