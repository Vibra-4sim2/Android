# Script de Test Google Maps après ajout du SHA-1

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   TEST GOOGLE MAPS - Après Ajout SHA-1" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Votre SHA-1 à ajouter dans Google Cloud Console :" -ForegroundColor Yellow
Write-Host "F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13" -ForegroundColor Green
Write-Host ""

Write-Host "SHA-1 déjà présent (votre amie) :" -ForegroundColor Yellow
Write-Host "39:70:7D:A5:91:6C:BC:1A:7D:47:4D:F6:CB:24:6C:98:1F:43:0D:0B" -ForegroundColor Cyan
Write-Host ""

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Demander si l'utilisateur a ajouté le SHA-1
$added = Read-Host "Avez-vous ajouté votre SHA-1 dans Google Cloud Console ? (O/N)"

if ($added -ne "O" -and $added -ne "o") {
    Write-Host ""
    Write-Host "⚠️  IMPORTANT : Vous devez d'abord ajouter votre SHA-1 !" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Allez sur : https://console.cloud.google.com/apis/credentials" -ForegroundColor White
    Write-Host "2. Cliquez sur votre clé API (AIzaSyDBAlApULWx9IjcK-z7k8i46QcD4h9I20o)" -ForegroundColor White
    Write-Host "3. Application restrictions > Android apps > + Add an item" -ForegroundColor White
    Write-Host "4. Package : com.example.dam" -ForegroundColor White
    Write-Host "5. SHA-1 : F2:56:E1:B6:92:7A:EA:33:35:AF:96:73:F9:5D:6B:5D:3D:BD:C9:13" -ForegroundColor White
    Write-Host "6. Cliquez sur 'Done' puis 'SAVE'" -ForegroundColor White
    Write-Host "7. Attendez 2-3 minutes" -ForegroundColor White
    Write-Host ""
    Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host ""
Write-Host "✅ Parfait ! Lançons les tests..." -ForegroundColor Green
Write-Host ""

# Demander si l'utilisateur a attendu
$waited = Read-Host "Avez-vous attendu 2-3 minutes après la sauvegarde ? (O/N)"

if ($waited -ne "O" -and $waited -ne "o") {
    Write-Host ""
    Write-Host "⏱️  Veuillez attendre 2-3 minutes pour que Google applique les modifications." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   ÉTAPE 1 : Clean du projet" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
cd "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"

Write-Host "Nettoyage en cours..." -ForegroundColor Yellow
.\gradlew clean

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   ÉTAPE 2 : Build du projet" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Compilation en cours..." -ForegroundColor Yellow
.\gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Build réussi !" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erreur de build" -ForegroundColor Red
    Write-Host ""
    Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   ÉTAPE 3 : Désinstallation de l'ancienne version" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Désinstallation..." -ForegroundColor Yellow
adb uninstall com.example.dam 2>$null

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   ÉTAPE 4 : Installation de la nouvelle version" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Installation..." -ForegroundColor Yellow
.\gradlew installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Installation réussie !" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erreur d'installation" -ForegroundColor Red
    Write-Host "Vérifiez qu'un appareil/émulateur est connecté : adb devices" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   🎉 TERMINÉ !" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ L'application a été réinstallée avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "🧪 Maintenant, lancez l'application sur votre appareil et :" -ForegroundColor White
Write-Host "   1. Allez dans un écran qui utilise Google Maps" -ForegroundColor White
Write-Host "   2. Vérifiez que la carte s'affiche correctement" -ForegroundColor White
Write-Host ""

$checkLogs = Read-Host "Voulez-vous surveiller les logs en temps réel ? (O/N)"

if ($checkLogs -eq "O" -or $checkLogs -eq "o") {
    Write-Host ""
    Write-Host "📱 Surveillance des logs Google Maps..." -ForegroundColor Yellow
    Write-Host "Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Gray
    Write-Host ""
    adb logcat | Select-String -Pattern "Google|Maps|Authorization"
} else {
    Write-Host ""
    Write-Host "Pour voir les logs manuellement :" -ForegroundColor Cyan
    Write-Host "adb logcat | findstr Google" -ForegroundColor White
    Write-Host ""
    Write-Host "Si vous voyez 'Authorization failure' :" -ForegroundColor Yellow
    Write-Host "1. Vérifiez que le SHA-1 est bien ajouté dans Google Cloud Console" -ForegroundColor White
    Write-Host "2. Vérifiez que vous avez cliqué sur 'SAVE'" -ForegroundColor White
    Write-Host "3. Attendez encore 2-3 minutes" -ForegroundColor White
    Write-Host ""
    Write-Host "Si vous ne voyez pas d'erreur :" -ForegroundColor Green
    Write-Host "✅ Google Maps fonctionne correctement !" -ForegroundColor Green
}

Write-Host ""
Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

