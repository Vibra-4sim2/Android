package com.example.dam.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.dam.utils.ThemePreferences

/**
 * ✅ Global Theme Manager for the entire app
 */
class AppThemeState(initialDarkMode: Boolean) {
    var isDarkMode by mutableStateOf(initialDarkMode)
}

val LocalThemeState = compositionLocalOf<AppThemeState> {
    error("No ThemeState provided")
}

@Composable
fun ProvideAppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themeState = remember {
        AppThemeState(ThemePreferences.isDarkMode(context))
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        content()
    }
}

/**
 * ✅ Get current theme colors based on dark/light mode
 */
@Composable
fun getThemeColors(): ThemeColors {
    val themeState = LocalThemeState.current
    return if (themeState.isDarkMode) {
        DarkThemeColors
    } else {
        LightThemeColors
    }
}

/**
 * ✅ Theme Colors Container
 */
data class ThemeColors(
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientEnd: androidx.compose.ui.graphics.Color,
    val cardColor: androidx.compose.ui.graphics.Color,
    val cardGlass: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val greenAccent: androidx.compose.ui.graphics.Color,
    val greenLight: androidx.compose.ui.graphics.Color,
    val borderColor: androidx.compose.ui.graphics.Color,
    val dividerColor: androidx.compose.ui.graphics.Color
)

val DarkThemeColors = ThemeColors(
    backgroundColor = BackgroundDark,
    gradientStart = BackgroundGradientStart,
    gradientEnd = BackgroundGradientEnd,
    cardColor = CardDark,
    cardGlass = CardGlass,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    greenAccent = GreenAccent,
    greenLight = GreenLight,
    borderColor = BorderColor,
    dividerColor = DividerColor
)

val LightThemeColors = ThemeColors(
    backgroundColor = BackgroundLight,
    gradientStart = BackgroundLightGradientStart,
    gradientEnd = BackgroundLightGradientEnd,
    cardColor = CardLight,
    cardGlass = CardLightGlass,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    greenAccent = GreenAccentLight,
    greenLight = GreenLightMode,
    borderColor = BorderColorLight,
    dividerColor = DividerColorLight
)

