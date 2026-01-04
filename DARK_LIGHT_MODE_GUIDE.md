# 🌓 Dark/Light Mode - Quick Reference Guide

## 🎯 How to Use

### For Users:

1. **Access Settings:**
   - Tap the dropdown menu (arrow down icon in top right)
   - Select "Settings"

2. **Toggle Theme:**
   - Find "Dark Mode" at the top
   - Tap the switch to toggle ON/OFF
   - Theme changes **instantly** - no restart needed!

3. **Theme Persists:**
   - Your choice is saved automatically
   - When you reopen the app, your theme is remembered

---

## 🎨 Visual Differences

### Dark Mode (Default) 🌙
```
✅ Deep forest green backgrounds
✅ White text for readability
✅ Dark cards with glassmorphism
✅ Mint green accents (#7FDB8A)
✅ Perfect for night use
✅ Reduces eye strain
```

### Light Mode ☀️
```
✅ Very light greenish-white backgrounds
✅ Dark text for contrast
✅ White cards with subtle borders
✅ Forest green accents (#4CAF50)
✅ Perfect for daytime
✅ Clean, professional look
```

---

## 🔧 Technical Implementation

### Theme Check (Every Screen):
```kotlin
val context = LocalContext.current
val isDarkMode = ThemePreferences.isDarkMode(context)

// Use appropriate colors
val textColor = if (isDarkMode) TextPrimary else TextPrimaryLight
val bgColor = if (isDarkMode) BackgroundDark else BackgroundLight
```

### Theme Toggle (Settings):
```kotlin
Switch(
    checked = isDarkMode,
    onCheckedChange = { isChecked ->
        isDarkMode = isChecked
        ThemePreferences.setDarkMode(context, isChecked)
        onThemeChanged() // Trigger recomposition
    }
)
```

### Persistence:
```kotlin
// Saved to SharedPreferences
ThemePreferences.setDarkMode(context, true/false)

// Retrieved on app start
val isDark = ThemePreferences.isDarkMode(context) // Default: true
```

---

## 📱 Screens Supporting Theme

All major screens now support both themes:

✅ **Home/Explore Screen**
✅ **Profile Screen**
✅ **Edit Profile Screens** (1 & 2)
✅ **Settings Screen**
✅ **Help Center Screen**
✅ **Messages Screen**
✅ **Feed Screen**
✅ **Create Adventure Screen**

*(Other screens can be updated with the same pattern)*

---

## 🎨 Color Reference

### Dark Mode Colors:
```kotlin
BackgroundDark = #0A1F1A              // Deep forest green
BackgroundGradientStart = #1A3A2E     // Forest green
BackgroundGradientEnd = #0F2419       // Dark forest
CardDark = #1E3A30                     // Dark green card
CardGlass = White 25% opacity          // Glass effect
TextPrimary = #FFFFFF                  // Pure white
TextSecondary = #B8C5B8                // Light gray-green
GreenAccent = #7FDB8A                  // Soft mint green
BorderColor = White 20% opacity        // Subtle border
```

### Light Mode Colors:
```kotlin
BackgroundLight = #F5F7F5                    // Very light greenish-white
BackgroundLightGradientStart = #E8F5E9       // Light green tint
BackgroundLightGradientEnd = #F1F8F1         // Soft white-green
CardLight = #FFFFFF                          // Pure white
CardLightGlass = Black 25% opacity           // Glass effect
TextPrimaryLight = #1B1B1B                   // Almost black
TextSecondaryLight = #5C6B5C                 // Dark gray-green
GreenAccentLight = #4CAF50                   // Forest green
BorderColorLight = Black 20% opacity         // Subtle border
```

---

## 🔄 Adding Theme Support to New Screens

### Template:
```kotlin
@Composable
fun YourScreen(navController: NavHostController) {
    val context = LocalContext.current
    val isDarkMode = remember { mutableStateOf(ThemePreferences.isDarkMode(context)) }

    // Dynamic colors
    val backgroundColor = if (isDarkMode.value) BackgroundDark else BackgroundLight
    val textPrimary = if (isDarkMode.value) TextPrimary else TextPrimaryLight
    val cardColor = if (isDarkMode.value) CardDark else CardLight
    val greenAccent = if (isDarkMode.value) GreenAccent else GreenAccentLight
    
    // Use these colors in your UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Text(text = "Hello", color = textPrimary)
        // ... rest of your UI
    }
}
```

---

## 🐛 Troubleshooting

### Theme doesn't change immediately:
- ✅ Make sure `onThemeChanged()` callback is implemented
- ✅ Verify colors are conditionally selected based on `isDarkMode`
- ✅ Check that `remember` is used for state

### Theme doesn't persist:
- ✅ Ensure `ThemePreferences.setDarkMode()` is called
- ✅ Check SharedPreferences are accessible
- ✅ Verify context is valid

### Colors look wrong:
- ✅ Use correct color variables for each mode
- ✅ Don't hardcode colors - use theme colors
- ✅ Check gradients use correct start/end colors

---

## 💡 Best Practices

1. **Always use theme colors:**
   ```kotlin
   // ❌ Don't do this
   color = Color(0xFF000000)
   
   // ✅ Do this
   color = if (isDarkMode) TextPrimary else TextPrimaryLight
   ```

2. **Remember state:**
   ```kotlin
   val isDarkMode = remember { mutableStateOf(ThemePreferences.isDarkMode(context)) }
   ```

3. **Test both themes:**
   - Always test your UI in both dark and light mode
   - Verify text is readable in both
   - Check contrast ratios

4. **Maintain brand identity:**
   - Keep green accents in both themes
   - Use nature-inspired colors
   - Ensure glassmorphism works in both

---

## 📊 Theme Statistics

- **Default Theme:** Dark Mode ✅
- **Theme Preference:** Persists across sessions
- **Switch Time:** Instant (no restart)
- **Supported Screens:** 8+ major screens
- **Color Variations:** 15+ theme-aware colors
- **Storage:** SharedPreferences
- **Performance:** No impact on app speed

---

## 🎉 User Benefits

### Dark Mode Users:
- 🌙 Reduced eye strain at night
- 🔋 Better battery life (OLED screens)
- 😎 Sleek, modern appearance
- 🎨 Beautiful forest green aesthetics

### Light Mode Users:
- ☀️ Better visibility in bright environments
- 📖 Easier to read in daylight
- ✨ Clean, professional look
- 🎨 Crisp white backgrounds

---

## 🚀 Future Enhancements

Possible additions:
- 🌅 Auto theme based on time of day
- 📱 System theme detection
- 🎨 Custom theme colors
- 💾 Theme presets (e.g., "Ocean", "Forest", "Sunset")
- ⏰ Scheduled theme switching

---

**Status:** Fully Implemented & Production Ready! ✅

Users can now personalize their experience with beautiful dark or light themes! 🎉

