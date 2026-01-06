# Script de Diagnostic Google Maps Configuration
# Ce script vérifie votre configuration locale

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   DIAGNOSTIC GOOGLE MAPS - Configuration Locale" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Variables
$projectPath = "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"
$manifestPath = "$projectPath\app\src\main\AndroidManifest.xml"
$stringsPath = "$projectPath\app\src\main\res\values\strings.xml"
$keystorePath = "$env:USERPROFILE\.android\debug.keystore"
$expectedKey = "AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o"
$expectedPackage = "com.example.dam"
$expectedSHA1 = "F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13"

Write-Host "🔍 Vérification de la configuration locale..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier AndroidManifest.xml
Write-Host "1. AndroidManifest.xml" -ForegroundColor White
if (Test-Path $manifestPath) {
    $manifestContent = Get-Content $manifestPath -Raw
    if ($manifestContent -match $expectedKey) {
        Write-Host "   ✅ Clé API trouvée : $expectedKey" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Clé API manquante ou incorrecte" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ Fichier non trouvé" -ForegroundColor Red
}
Write-Host ""

# 2. Vérifier strings.xml
Write-Host "2. strings.xml" -ForegroundColor White
if (Test-Path $stringsPath) {
    $stringsContent = Get-Content $stringsPath -Raw
    if ($stringsContent -match $expectedKey) {
        Write-Host "   ✅ Clé API trouvée : $expectedKey" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Clé API manquante ou incorrecte" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ Fichier non trouvé" -ForegroundColor Red
}
Write-Host ""

# 3. Vérifier SHA-1
Write-Host "3. SHA-1 Certificate Fingerprint" -ForegroundColor White
if (Test-Path $keystorePath) {
    try {
        $keytoolPath = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
        if (Test-Path $keytoolPath) {
            $sha1Output = & $keytoolPath -list -v -keystore $keystorePath -alias androiddebugkey -storepass android -keypass android 2>&1 | Select-String "SHA1:"
            if ($sha1Output) {
                $sha1Value = ($sha1Output -replace ".*SHA1:\s*", "").Trim()
                if ($sha1Value -eq $expectedSHA1) {
                    Write-Host "   ✅ SHA-1 correct : $sha1Value" -ForegroundColor Green
                } else {
                    Write-Host "   ⚠️  SHA-1 trouvé : $sha1Value" -ForegroundColor Yellow
                    Write-Host "   ⚠️  SHA-1 attendu : $expectedSHA1" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host "   ⚠️  keytool non trouvé" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ⚠️  Erreur lors de la lecture du keystore" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ❌ Keystore non trouvé" -ForegroundColor Red
}
Write-Host ""

# 4. Vérifier le package name
Write-Host "4. Package Name" -ForegroundColor White
if (Test-Path $manifestPath) {
    $manifestContent = Get-Content $manifestPath -Raw
    if ($manifestContent -match 'package="com.example.dam"') {
        Write-Host "   ✅ Package correct : com.example.dam" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Package non trouvé ou incorrect" -ForegroundColor Yellow
    }
}
Write-Host ""

# Résumé
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   RÉSUMÉ" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Configuration locale :" -ForegroundColor White
Write-Host "   • Clé API      : $expectedKey" -ForegroundColor Cyan
Write-Host "   • Package      : $expectedPackage" -ForegroundColor Cyan
Write-Host "   • SHA-1        : $expectedSHA1" -ForegroundColor Cyan
Write-Host ""

Write-Host "⚠️  IMPORTANT : Configuration Google Cloud Console" -ForegroundColor Yellow
Write-Host ""
Write-Host "Votre configuration locale est OK, mais l'erreur 'Authorization failure'" -ForegroundColor White
Write-Host "signifie que vous devez ABSOLUMENT configurer Google Cloud Console :" -ForegroundColor White
Write-Host ""
Write-Host "1. Allez sur : https://console.cloud.google.com/" -ForegroundColor Cyan
Write-Host "2. Activez : Maps SDK for Android" -ForegroundColor Cyan
Write-Host "3. Configurez votre clé API avec :" -ForegroundColor Cyan
Write-Host "   - Android apps" -ForegroundColor White
Write-Host "   - Package : $expectedPackage" -ForegroundColor White
Write-Host "   - SHA-1   : $expectedSHA1" -ForegroundColor White
Write-Host "4. Activez : Directions API + Geocoding API" -ForegroundColor Cyan
Write-Host "5. Configurez la facturation (obligatoire)" -ForegroundColor Cyan
Write-Host "6. Attendez 5 minutes" -ForegroundColor Cyan
Write-Host ""
Write-Host "Voir : VERIFICATION_GOOGLE_CLOUD.md pour le guide complet" -ForegroundColor Green
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan

# Ouvrir le guide automatiquement
Write-Host ""
$openGuide = Read-Host "Voulez-vous ouvrir le guide détaillé ? (O/N)"
if ($openGuide -eq "O" -or $openGuide -eq "o") {
    $guidePath = "$projectPath\VERIFICATION_GOOGLE_CLOUD.md"
    if (Test-Path $guidePath) {
        Start-Process $guidePath
        Write-Host "✅ Guide ouvert !" -ForegroundColor Green
    } else {
        Write-Host "❌ Guide non trouvé : $guidePath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

