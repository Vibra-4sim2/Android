# 🧪 Quick Badge Test Script
# Run this in PowerShell while the app is running

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🧪 BADGE FIX - LIVE TEST MONITOR" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📱 Instructions:" -ForegroundColor Yellow
Write-Host "1. Open the app and go to Messages list"
Write-Host "2. Find a chat with unread messages (red badge)"
Write-Host "3. Click on it to open the conversation"
Write-Host "4. Watch the logs below for badge state changes"
Write-Host "5. Return to the list and verify badge disappeared"
Write-Host "6. Ask someone to send a new message"
Write-Host "7. Verify badge reappears with new count"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 LIVE LOGS (Ctrl+C to stop):" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Filter logs for badge-related activity
adb logcat -c  # Clear previous logs

adb logcat | Select-String -Pattern "GroupChatItem|ChatStateManager|MessagesListScreen" | ForEach-Object {
    $line = $_.Line

    # Color coding for better readability
    if ($line -match "✅") {
        Write-Host $line -ForegroundColor Green
    }
    elseif ($line -match "⚠️|❌") {
        Write-Host $line -ForegroundColor Yellow
    }
    elseif ($line -match "🔄|🔍|📊") {
        Write-Host $line -ForegroundColor Cyan
    }
    elseif ($line -match "MARKING CHAT AS OPENED") {
        Write-Host $line -ForegroundColor Magenta
    }
    elseif ($line -match "Backend confirmed read") {
        Write-Host $line -ForegroundColor Green
    }
    elseif ($line -match "New messages arrived") {
        Write-Host $line -ForegroundColor Red
    }
    elseif ($line -match "Displaying badge count") {
        Write-Host $line -ForegroundColor Blue
    }
    else {
        Write-Host $line
    }
}

