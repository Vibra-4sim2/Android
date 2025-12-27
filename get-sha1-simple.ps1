# Script simplifié pour obtenir le SHA-1
Write-Host "Récupération du SHA-1..." -ForegroundColor Cyan
Write-Host ""

$keystorePath = "$env:USERPROFILE\.android\debug.keystore"

if (-Not (Test-Path $keystorePath)) {
    Write-Host "Erreur: debug.keystore introuvable!" -ForegroundColor Red
    Write-Host "Chemin: $keystorePath" -ForegroundColor Yellow
    exit
}

Write-Host "Keystore trouvé!" -ForegroundColor Green
Write-Host ""

keytool -list -v -keystore $keystorePath -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"

Write-Host ""
Write-Host "Copiez le SHA-1 ci-dessus (la partie après 'SHA1: ')" -ForegroundColor Yellow
Write-Host ""
Write-Host "Puis allez sur: https://console.cloud.google.com/" -ForegroundColor Cyan
Write-Host "APIs & Services > Credentials > Votre clé API" -ForegroundColor White
Write-Host "Application restrictions > Android apps > Add an item" -ForegroundColor White
Write-Host "Package name: com.example.dam" -ForegroundColor Green
Write-Host "SHA-1: [COLLEZ LA VALEUR COPIÉE]" -ForegroundColor Green
Write-Host ""
Write-Host "Appuyez sur une touche..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

