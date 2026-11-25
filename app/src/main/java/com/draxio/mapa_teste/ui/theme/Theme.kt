package com.draxio.mapa_teste.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Gerenciador de Preferência de Tema Persistente para todo o aplicativo
object ThemePreferences {
    private const val PREFS_NAME = "caramelo_theme_prefs"
    private const val KEY_DARK_MODE = "is_dark_mode"

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }
}

private val LightColorScheme = lightColorScheme(
    primary = CarameloRust,
    onPrimary = CarameloSurface,
    primaryContainer = CarameloYellow,
    onPrimaryContainer = CarameloRustDark,
    secondary = CarameloOrange,
    onSecondary = CarameloSurface,
    background = CarameloBackground,
    onBackground = CarameloTextPrimary,
    surface = CarameloSurface,
    onSurface = CarameloTextPrimary,
    surfaceVariant = CarameloSurfaceVariant,
    onSurfaceVariant = CarameloTextMuted,
    outline = CarameloCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = CarameloOrange,
    onPrimary = Color.Black,
    primaryContainer = CarameloYellow,
    onPrimaryContainer = Color.Black,
    secondary = CarameloOrange,
    onSecondary = Color.Black,
    background = CarameloDarkBackground,
    onBackground = CarameloDarkTextPrimary,
    surface = CarameloDarkSurface,
    onSurface = CarameloDarkTextPrimary,
    surfaceVariant = CarameloDarkSurfaceVariant,
    onSurfaceVariant = CarameloDarkTextMuted,
    outline = CarameloDarkCardBorder
)

@Composable
fun Mapa_testeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Determina se o tema escuro deve ser aplicado (por preferência salva ou tema do sistema)
    val isDark = if (!view.isInEditMode) {
        try {
            ThemePreferences.isDarkMode(context) || darkTheme
        } catch (e: Exception) {
            darkTheme
        }
    } else {
        darkTheme
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}