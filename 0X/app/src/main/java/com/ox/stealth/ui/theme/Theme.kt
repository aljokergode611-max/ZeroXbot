package com.ox.stealth.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ألوان Zero X - مستوحاة من الأيقونة
val ZeroXRed = Color(0xFFCC0033)
val ZeroXRedDark = Color(0xFF8B0022)
val ZeroXRedGlow = Color(0xFFFF0040)
val HackerGreen = Color(0xFF00FF41)
val HackerGreenDark = Color(0xFF00CC33)
val HackerGreenDim = Color(0xFF00AA28)
val HackerCyan = Color(0xFF00FFFF)
val HackerRed = Color(0xFFFF0040)
val HackerOrange = Color(0xFFFF6600)
val HackerYellow = Color(0xFFFFFF00)
val HackerPurple = Color(0xFFBB00FF)
val DarkBg = Color(0xFF080808)
val DarkSurface = Color(0xFF0E0E0E)
val DarkCard = Color(0xFF151515)
val DarkCardBorder = Color(0xFF00FF41).copy(alpha = 0.15f)
val TerminalGreen = Color(0xFF33FF33)
val GlowGreen = Color(0xFF00FF41)
val GlowRed = Color(0xFFFF0040)

private val ZeroXColorScheme = darkColorScheme(
    primary = HackerGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF002200),
    onPrimaryContainer = HackerGreen,
    secondary = ZeroXRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF330011),
    onSecondaryContainer = ZeroXRedGlow,
    tertiary = HackerCyan,
    onTertiary = Color.Black,
    error = HackerRed,
    onError = Color.White,
    background = DarkBg,
    onBackground = HackerGreen,
    surface = DarkSurface,
    onSurface = Color(0xFFBBBBBB),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFF888888),
    outline = DarkCardBorder,
)

@Composable
fun OXTheme(content: @Composable () -> Unit) {
    val colorScheme = ZeroXColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
