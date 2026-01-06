# 🗺️ Guide de Configuration Google Maps API

## ❌ Problème Actuel

Erreur dans les logs :
```
Authorization failure. Please see https://developers.google.com/maps/documentation/android-sdk/start 
for how to correctly set up the map.
```

**Cause :** La clé API Google Maps n'est pas correctement configurée dans la Google Cloud Console.

---

## ✅ Solution : Configuration Google Cloud Console

### Étape 1 : Accéder à Google Cloud Console

1. Allez sur : https://console.cloud.google.com/
2. Connectez-vous avec votre compte Google
3. Sélectionnez votre projet (ou créez-en un nouveau)

### Étape 2 : Activer les APIs Nécessaires

1. Dans le menu, allez dans **APIs & Services** > **Library**
2. Recherchez et **activez** ces APIs :
   - ✅ **Maps SDK for Android**
   - ✅ **Directions API**
   - ✅ **Geocoding API**
   - ✅ **Places API** (si vous utilisez des lieux)

### Étape 3 : Créer ou Configurer la Clé API

#### A. Obtenir l'empreinte SHA-1

Vous avez besoin de l'empreinte SHA-1 de votre certificat de débogage.

**Sur Windows (PowerShell) :**
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Cherchez cette ligne :**
```
SHA1: F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13
```

**Copiez votre empreinte SHA-1 !**

#### B. Configurer la Clé API

1. Dans Google Cloud Console, allez dans **APIs & Services** > **Credentials**
2. Cliquez sur votre clé API (ou créez-en une nouvelle)
3. Dans **Application restrictions** :
   - Sélectionnez **Android apps**
4. Cliquez sur **Add an item**
5. Remplissez :
   ```
   Package name: com.example.dam
   SHA-1 certificate fingerprint: F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13
   ```
   (⚠️ **Utilisez VOTRE empreinte SHA-1 obtenue à l'étape A**)

6. Dans **API restrictions** :
   - Sélectionnez **Restrict key**
   - Cochez ces APIs :
     - Maps SDK for Android
     - Directions API
     - Geocoding API
     - Places API (si applicable)

7. Cliquez sur **Save**

### Étape 4 : Vérifier la Clé API dans le Code

#### ✅ AndroidManifest.xml (Déjà correct)
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o" />
```

#### ✅ GoogleRetrofitInstance.kt (Corrigé)
```kotlin
private const val API_KEY = "AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o"
```

**⚠️ IMPORTANT : Pas de `=` à la fin de la clé !**

---

## 🔍 Vérifications Supplémentaires

### 1. Vérifier que les APIs sont activées

Dans Google Cloud Console :
```
APIs & Services > Dashboard

Vous devriez voir :
✅ Maps SDK for Android - Enabled
✅ Directions API - Enabled  
✅ Geocoding API - Enabled
```

### 2. Vérifier les Restrictions de la Clé

```
APIs & Services > Credentials > Votre clé API

Application restrictions:
✅ Android apps
   ✅ com.example.dam (SHA-1: F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13)

API restrictions:
✅ Restrict key
   ✅ Maps SDK for Android
   ✅ Directions API
   ✅ Geocoding API
```

### 3. Vérifier le Quota

```
APIs & Services > Maps SDK for Android > Quotas

Vérifiez que vous n'avez pas dépassé les limites gratuites :
- Maps SDK for Android: 28,500 requêtes/mois gratuites
- Directions API: 2,500 requêtes/jour gratuites
- Geocoding API: 40,000 requêtes/mois gratuites
```

---

## 🧪 Test Après Configuration

### 1. Nettoyer et Rebuild

```powershell
cd "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"
./gradlew clean
./gradlew build
```

### 2. Désinstaller et Réinstaller l'App

Sur votre appareil/émulateur :
```
Paramètres > Apps > DAM > Désinstaller
```

Puis reinstallez depuis Android Studio (Run)

### 3. Vérifier les Logs

Après le lancement, cherchez dans Logcat :
```
✅ SUCCÈS:
Google Maps loaded successfully

❌ ÉCHEC (encore):
Authorization failure
```

---

## 🔧 Solutions aux Problèmes Courants

### Problème 1 : "Authorization failure" persiste

**Causes possibles :**
1. **Empreinte SHA-1 incorrecte**
   - Vérifiez que vous utilisez l'empreinte du certificat de **debug** (pas release)
   - Commande : `keytool -list -v -keystore debug.keystore ...`

2. **Package name incorrect**
   - Doit être exactement : `com.example.dam`
   - Vérifiez dans `app/build.gradle.kts` : `applicationId = "com.example.dam"`

3. **APIs non activées**
   - Retournez dans Library et activez toutes les APIs

4. **Délai de propagation**
   - Attendez 5-10 minutes après la configuration
   - Les modifications peuvent prendre du temps

### Problème 2 : Map affiche "For development purposes only"

**Cause :** La clé API n'a pas de restrictions ou mauvaise configuration

**Solution :**
1. Ajoutez les restrictions Android apps
2. Configurez correctement le SHA-1

### Problème 3 : Quota dépassé

**Cause :** Trop de requêtes API

**Solutions :**
1. Activez la facturation (carte bancaire requise)
2. Optimisez l'utilisation (cache, moins de requêtes)
3. Utilisez un nouveau projet Google Cloud

---

## 📝 Checklist de Configuration

Cochez chaque étape :

### Dans Google Cloud Console
- [ ] Projet créé/sélectionné
- [ ] Maps SDK for Android activée
- [ ] Directions API activée
- [ ] Geocoding API activée
- [ ] Clé API créée
- [ ] Restrictions Android apps configurées
- [ ] Package name ajouté : `com.example.dam`
- [ ] SHA-1 ajouté (obtenu via keytool)
- [ ] API restrictions configurées
- [ ] Sauvegardé

### Dans le Code
- [x] AndroidManifest.xml : clé API présente
- [x] GoogleRetrofitInstance.kt : clé API correcte (sans `=`)
- [ ] App nettoyée (clean)
- [ ] App rebuilded
- [ ] App désinstallée et réinstallée

---

## 🎯 Commandes Utiles

### Obtenir SHA-1 (Debug)
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"
```

### Obtenir SHA-1 (Release) - Si vous publiez l'app
```powershell
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

### Nettoyer le Projet
```powershell
cd "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"
./gradlew clean
```

### Rebuild
```powershell
./gradlew build
```

---

## 🆘 Si Ça Ne Marche Toujours Pas

### Option 1 : Créer une Nouvelle Clé API

1. Dans Google Cloud Console : **APIs & Services** > **Credentials**
2. **Create Credentials** > **API Key**
3. Notez la nouvelle clé
4. Configurez les restrictions comme décrit ci-dessus
5. Remplacez l'ancienne clé dans le code

### Option 2 : Créer un Nouveau Projet Google Cloud

Parfois, un projet peut avoir des problèmes de configuration :

1. Créez un nouveau projet dans Google Cloud Console
2. Activez toutes les APIs nécessaires
3. Créez une nouvelle clé API
4. Configurez correctement
5. Utilisez la nouvelle clé

### Option 3 : Vérifier la Facturation

Même avec le plan gratuit, Google peut demander une carte bancaire :

1. Allez dans **Billing** dans Google Cloud Console
2. Ajoutez un mode de paiement
3. Activez le plan gratuit
4. Vous ne serez pas facturé tant que vous restez dans les limites gratuites

---

## 💡 Important à Savoir

### Limites Gratuites Google Maps

| API | Limite Gratuite | Coût Après |
|-----|----------------|------------|
| Maps SDK for Android | 28,500 req/mois | $7 / 1,000 req |
| Directions API | 2,500 req/jour | $5 / 1,000 req |
| Geocoding API | 40,000 req/mois | $5 / 1,000 req |
| Places API | 5,000 req/mois | Variable |

**💡 Astuce :** Utilisez un système de cache pour réduire les appels API

### Différence Debug vs Release

- **Debug (développement)** : Utilisez SHA-1 du `debug.keystore`
- **Release (production)** : Utilisez SHA-1 de votre keystore de signature

**⚠️ Vous devez ajouter LES DEUX SHA-1 dans la console si vous voulez que ça marche en debug ET en release !**

---

## 📞 Ressources Officielles

- [Documentation officielle Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/start)
- [Obtenir une clé API](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
- [Troubleshooting Authorization](https://developers.google.com/maps/documentation/android-sdk/map-not-showing)
- [Google Cloud Console](https://console.cloud.google.com/)

---

## 🎉 Résumé des Actions

### Ce qui a été corrigé dans le code :
✅ Suppression du `=` en trop dans `GoogleRetrofitInstance.kt`

### Ce que VOUS devez faire :
1. **Obtenir votre SHA-1** avec la commande keytool
2. **Aller dans Google Cloud Console**
3. **Configurer votre clé API** avec le SHA-1 et package name
4. **Activer les APIs** nécessaires
5. **Attendre 5-10 minutes** pour la propagation
6. **Nettoyer et rebuilder** l'app
7. **Tester** !

---

**Date de création :** 14 décembre 2025  
**Status :** ⚠️ Configuration manuelle requise  
**Priorité :** 🔴 Haute (Maps ne fonctionne pas)

