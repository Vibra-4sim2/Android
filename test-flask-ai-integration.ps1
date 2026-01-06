 ew# Script de Test Flask AI Itinerary

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   TEST FLASK AI ITINERARY - Intégration" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ Modifications effectuées :" -ForegroundColor Green
Write-Host "   1. FlaskItineraryModels.kt - Géométrie mise à jour" -ForegroundColor White
Write-Host "   2. FlaskAiViewModel.kt - Traitement des coordonnées" -ForegroundColor White
Write-Host "   3. CreateAdventureScreen.kt - Déjà configuré" -ForegroundColor White
Write-Host ""

Write-Host "📋 Fichiers modifiés :" -ForegroundColor Yellow
Write-Host "   • models/FlaskItineraryModels.kt" -ForegroundColor White
Write-Host "   • viewmodel/FlaskAiViewModel.kt" -ForegroundColor White
Write-Host ""

$compile = Read-Host "Voulez-vous compiler et installer l'app maintenant ? (O/N)"

if ($compile -eq "O" -or $compile -eq "o") {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "   COMPILATION EN COURS..." -ForegroundColor White
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""

    $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
    cd "C:\Users\cyrin\frontandroidghalia\dam (2)\dam"

    Write-Host "🔨 Clean..." -ForegroundColor Yellow
    .\gradlew clean --quiet

    Write-Host "🔨 Build..." -ForegroundColor Yellow
    .\gradlew assembleDebug --quiet

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Build réussi !" -ForegroundColor Green
        Write-Host ""

        $install = Read-Host "Installer sur l'appareil ? (O/N)"

        if ($install -eq "O" -or $install -eq "o") {
            Write-Host ""
            Write-Host "📱 Installation..." -ForegroundColor Yellow
            .\gradlew installDebug

            if ($LASTEXITCODE -eq 0) {
                Write-Host ""
                Write-Host "✅ Installation réussie !" -ForegroundColor Green
                Write-Host ""
                Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
                Write-Host "   COMMENT TESTER" -ForegroundColor Green
                Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
                Write-Host ""
                Write-Host "1. Ouvrez l'app sur votre appareil" -ForegroundColor White
                Write-Host "2. Allez dans 'New Adventure'" -ForegroundColor White
                Write-Host "3. Remplissez Section 1 (Informations)" -ForegroundColor White
                Write-Host "4. Passez à Section 2 (Routes Organisation)" -ForegroundColor White
                Write-Host "5. Sélectionnez un point de départ" -ForegroundColor White
                Write-Host "6. Sélectionnez un point d'arrivée" -ForegroundColor White
                Write-Host "7. Cliquez sur 'Calculer avec IA' (bouton vert)" -ForegroundColor Green
                Write-Host ""
                Write-Host "🎯 RÉSULTAT ATTENDU :" -ForegroundColor Yellow
                Write-Host "   • Message 'Génération IA...' pendant le chargement" -ForegroundColor White
                Write-Host "   • Route bleue affichée sur la carte" -ForegroundColor White
                Write-Host "   • Badge 'IA Route générée !'" -ForegroundColor White
                Write-Host "   • Distance affichée (ex: '42.4 km')" -ForegroundColor White
                Write-Host "   • Durée affichée (ex: '2h 28min')" -ForegroundColor White
                Write-Host "   • Badge 'Ombragée & sécurisée'" -ForegroundColor White
                Write-Host ""

                $logs = Read-Host "Voulez-vous surveiller les logs ? (O/N)"

                if ($logs -eq "O" -or $logs -eq "o") {
                    Write-Host ""
                    Write-Host "📱 Surveillance des logs Flask AI..." -ForegroundColor Yellow
                    Write-Host "Cherchez ces messages :" -ForegroundColor Cyan
                    Write-Host "   ✅ 'ROUTE IA CHARGÉE ! X points'" -ForegroundColor Green
                    Write-Host "   📏 'Distance: X km'" -ForegroundColor Green
                    Write-Host "   ⏱️ 'Durée: X min'" -ForegroundColor Green
                    Write-Host ""
                    Write-Host "Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Gray
                    Write-Host ""
                    adb logcat | Select-String -Pattern "FlaskAi|ROUTE IA|Distance|Durée"
                }
            } else {
                Write-Host ""
                Write-Host "❌ Erreur d'installation" -ForegroundColor Red
                Write-Host "Vérifiez qu'un appareil est connecté : adb devices" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host ""
        Write-Host "❌ Erreur de build" -ForegroundColor Red
    }
} else {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "   INSTRUCTIONS DE TEST MANUEL" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Compilez l'app :" -ForegroundColor White
    Write-Host "   .\gradlew clean assembleDebug" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "2. Installez sur l'appareil :" -ForegroundColor White
    Write-Host "   .\gradlew installDebug" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "3. Testez dans l'app :" -ForegroundColor White
    Write-Host "   • New Adventure > Section 2" -ForegroundColor White
    Write-Host "   • Sélectionnez départ et arrivée" -ForegroundColor White
    Write-Host "   • Cliquez 'Calculer avec IA'" -ForegroundColor White
    Write-Host ""
    Write-Host "4. Surveillez les logs :" -ForegroundColor White
    Write-Host "   adb logcat | findstr FlaskAi" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   DOCUMENTATION" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📄 Guide complet : INTEGRATION_FLASK_AI_COMPLETE.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "   • Structure JSON complète" -ForegroundColor White
Write-Host "   • Détails techniques" -ForegroundColor White
Write-Host "   • Instructions de test" -ForegroundColor White
Write-Host "   • Dépannage" -ForegroundColor White
Write-Host "   • Logs importants" -ForegroundColor White
Write-Host ""

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   🎉 INTÉGRATION TERMINÉE !" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Appuyez sur une touche pour quitter..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

