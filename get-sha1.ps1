# Script PowerShell pour obtenir le SHA-1 du certificat de debug Android
# Exécutez ce script dans PowerShell pour obtenir votre empreinte SHA-1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Récupération SHA-1 Android Debug" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin du keystore de debug
$keystorePath = "$env:USERPROFILE\.android\debug.keystore"

# Vérifier si le keystore existe
if (-Not (Test-Path $keystorePath)) {
    Write-Host "❌ ERREUR: Fichier debug.keystore introuvable!" -ForegroundColor Red
    Write-Host "   Chemin attendu: $keystorePath" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   Solutions:" -ForegroundColor Yellow
    Write-Host "   1. Lancez Android Studio et buildez votre projet une fois" -ForegroundColor White
    Write-Host "   2. Le keystore sera créé automatiquement" -ForegroundColor White
    Write-Host ""
    exit
}

Write-Host "✅ Keystore trouvé: $keystorePath" -ForegroundColor Green
Write-Host ""
Write-Host "🔍 Extraction du SHA-1..." -ForegroundColor Yellow
Write-Host ""

try {
    # Exécuter keytool pour obtenir les informations
    $output = & keytool -list -v -keystore $keystorePath -alias androiddebugkey -storepass android -keypass android 2>&1

    # Extraire le SHA-1
    $sha1Line = $output | Select-String "SHA1:"

    if ($sha1Line) {
        $sha1 = $sha1Line.Line.Trim()

        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  ✅ SHA-1 TROUVÉ !" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host $sha1 -ForegroundColor Cyan
        Write-Host ""

        # Extraire juste la valeur du SHA-1
        $sha1Value = $sha1 -replace "SHA1: ", ""

        Write-Host "----------------------------------------" -ForegroundColor White
        Write-Host "  Copiez cette valeur:" -ForegroundColor Yellow
        Write-Host "----------------------------------------" -ForegroundColor White
        Write-Host $sha1Value -ForegroundColor White
        Write-Host ""

        # Copier dans le presse-papier
        $sha1Value | Set-Clipboard
        Write-Host "✅ SHA-1 copié dans le presse-papier !" -ForegroundColor Green
        Write-Host ""

        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "  📋 PROCHAINES ÉTAPES" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "1. Allez sur: https://console.cloud.google.com/" -ForegroundColor White
        Write-Host "2. Sélectionnez votre projet" -ForegroundColor White
        Write-Host "3. Allez dans: APIs & Services > Credentials" -ForegroundColor White
        Write-Host "4. Cliquez sur votre clé API" -ForegroundColor White
        Write-Host "5. Dans 'Application restrictions', choisissez 'Android apps'" -ForegroundColor White
        Write-Host "6. Cliquez sur 'Add an item'" -ForegroundColor White
        Write-Host "7. Remplissez:" -ForegroundColor White
        Write-Host "   Package name: com.example.dam" -ForegroundColor Yellow
        Write-Host "   SHA-1: $sha1Value" -ForegroundColor Yellow
        Write-Host "8. Cliquez sur 'Save'" -ForegroundColor White
        Write-Host ""
        Write-Host "⏳ Attendez 5-10 minutes pour que les changements prennent effet" -ForegroundColor Magenta
        Write-Host ""

    } else {
        Write-Host "❌ SHA-1 non trouvé dans la sortie keytool" -ForegroundColor Red
        Write-Host ""
        Write-Host "Sortie complète:" -ForegroundColor Yellow
        Write-Host $output
    }

} catch {
    Write-Host "❌ ERREUR lors de l'exécution de keytool:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Assurez-vous que Java JDK est installé et dans le PATH" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Appuyez sur une touche pour fermer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

