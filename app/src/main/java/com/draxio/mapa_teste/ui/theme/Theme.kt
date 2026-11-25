package com.draxio.mapa_teste.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
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
        prefs.edit { putBoolean(KEY_DARK_MODE, isDark) }
    }

    // null = o usuário nunca escolheu (segue o sistema)
    fun getDarkModeOrNull(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    }
}

// Estado observável do tema, em nível de processo.
// null = segue o sistema; true/false = escolha do usuário.
object ThemeState {
    var darkMode by mutableStateOf<Boolean?>(null)

    private var loaded = false

    // Carrega a preferência salva na primeira leitura
    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        try {
            darkMode = ThemePreferences.getDarkModeOrNull(context.applicationContext)
        } catch (_: Exception) {
            // mantém o valor atual
        }
    }

    // Aplica e persiste a escolha do usuário
    fun setDarkMode(context: Context, enabled: Boolean) {
        loaded = true
        darkMode = enabled
        ThemePreferences.setDarkMode(context.applicationContext, enabled)
    }
}

private val LightColorScheme = lightColorScheme(
    primary = CarameloRust,
    onPrimary = CarameloSurface,
    primaryContainer = CarameloSelectedBg,
    onPrimaryContainer = CarameloRustDark,
    secondary = CarameloOrange,
    onSecondary = CarameloCocoa,
    tertiary = CarameloCocoa,
    onTertiary = CarameloSurface,
    background = CarameloBackground,
    onBackground = CarameloTextPrimary,
    surface = CarameloSurface,
    onSurface = CarameloTextPrimary,
    surfaceVariant = CarameloSurfaceVariant,
    onSurfaceVariant = CarameloTextMuted,
    outline = CarameloCardBorder,
    outlineVariant = CarameloCardBorder
)

// No escuro o botao continua caramelo: o codigo usa texto branco sobre primary,
// e branco sobre o laranja do pino ficaria ilegivel (2,4:1).
private val DarkColorScheme = darkColorScheme(
    primary = CarameloRust,
    onPrimary = Color.White,
    primaryContainer = CarameloDarkSelectedBg,
    onPrimaryContainer = CarameloDarkTextPrimary,
    secondary = CarameloOrange,
    onSecondary = CarameloCocoa,
    tertiary = CarameloOrange,
    onTertiary = CarameloCocoa,
    background = CarameloDarkBackground,
    onBackground = CarameloDarkTextPrimary,
    surface = CarameloDarkSurface,
    onSurface = CarameloDarkTextPrimary,
    surfaceVariant = CarameloDarkSurfaceVariant,
    onSurfaceVariant = CarameloDarkTextMuted,
    outline = CarameloDarkCardBorder,
    outlineVariant = CarameloDarkCardBorder
)

// Cantos arredondados no estilo do redesign: cartoes de 20 a 32 dp.
private val CarameloShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun Mapa_testeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Escolha do usuário vale; sem escolha, segue o sistema
    val isDark = if (!view.isInEditMode) {
        ThemeState.ensureLoaded(context)
        ThemeState.darkMode ?: darkTheme
    } else {
        darkTheme
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CarameloShapes,
        content = content
    )
}