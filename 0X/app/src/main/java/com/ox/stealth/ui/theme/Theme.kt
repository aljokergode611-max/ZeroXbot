package com.ox.stealth.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============ Modern Professional Theme ============
// ثيم احترافي حديث مستوحى من Material Design 3

// Primary Colors - أزرق احترافي
val ModernBlue = Color(0xFF2196F3)
val ModernBlueDark = Color(0xFF1976D2)
val ModernBlueLight = Color(0xFF64B5F6)

// Accent Colors - أخضر نضر
val ModernGreen = Color(0xFF4CAF50)
val ModernGreenDark = Color(0xFF388E3C)
val ModernGreenLight = Color(0xFF81C784)

// Neutral Colors - رمادي احترافي
val LightBg = Color(0xFFF5F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val BorderLight = Color(0xFFE0E0E0)
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF666666)
val TextTertiary = Color(0xFF999999)

// Status Colors
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFF44336)
val InfoBlue = Color(0xFF2196F3)

// Map Colors
val MapAccent = Color(0xFF2196F3)
val MapMarker = Color(0xFFE91E63)

// Modern Light Color Scheme
private val ModernLightColorScheme = lightColorScheme(
    primary = ModernBlue,
    onPrimary = Color.White,
    primaryContainer = ModernBlueLight,
    onPrimaryContainer = ModernBlueDark,
    
    secondary = ModernGreen,
    onSecondary = Color.White,
    secondaryContainer = ModernGreenLight,
    onSecondaryContainer = ModernGreenDark,
    
    tertiary = Color(0xFF6200EE),
    onTertiary = Color.White,
    
    error = ErrorRed,
    onError = Color.White,
    
    background = LightBg,
    onBackground = TextPrimary,
    
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = TextSecondary,
    
    outline = BorderLight,
    outlineVariant = Color(0xFFEEEEEE),
)

@Composable
fun OXTheme(content: @Composable () -> Unit) {
    val colorScheme = ModernLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LightBg.toArgb()
            window.navigationBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        content = content
    )
}

// Modern Typography
val ModernTypography = Typography(
    displayLarge = Typography().displayLarge.copy(color = TextPrimary),
    displayMedium = Typography().displayMedium.copy(color = TextPrimary),
    displaySmall = Typography().displaySmall.copy(color = TextPrimary),
    headlineLarge = Typography().headlineLarge.copy(color = TextPrimary),
    headlineMedium = Typography().headlineMedium.copy(color = TextPrimary),
    headlineSmall = Typography().headlineSmall.copy(color = TextPrimary),
    titleLarge = Typography().titleLarge.copy(color = TextPrimary),
    titleMedium = Typography().titleMedium.copy(color = TextPrimary),
    titleSmall = Typography().titleSmall.copy(color = TextPrimary),
    bodyLarge = Typography().bodyLarge.copy(color = TextPrimary),
    bodyMedium = Typography().bodyMedium.copy(color = TextSecondary),
    bodySmall = Typography().bodySmall.copy(color = TextSecondary),
    labelLarge = Typography().labelLarge.copy(color = TextPrimary),
    labelMedium = Typography().labelMedium.copy(color = TextSecondary),
    labelSmall = Typography().labelSmall.copy(color = TextTertiary),
)

// Legacy color exports for compatibility
val ZeroXRed = ErrorRed
val ZeroXRedGlow = ErrorRed
val HackerGreen = ModernGreen
val HackerCyan = InfoBlue
val HackerRed = ErrorRed
val DarkBg = LightBg
val DarkSurface = LightSurface
val DarkCard = LightCard
val TerminalGreen = ModernGreen
val GlowGreen = ModernGreen
