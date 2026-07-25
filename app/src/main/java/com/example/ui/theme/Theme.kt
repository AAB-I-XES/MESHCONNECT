package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MeshPrimary,
    onPrimary = Color.White,
    primaryContainer = MeshSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1C1C1E),
    onSecondaryContainer = TextSecondary,
    tertiary = NeonAmber,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = CoralRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00893A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF00200A),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    background = MeshBackgroundLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    onBackground = Color(0xFF0F171B),
    onSurface = Color(0xFF0F171B),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8)
)

@Composable
fun MeshLinkTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
