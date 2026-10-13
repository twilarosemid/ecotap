package com.example.ecotap.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EcoEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EcoMintContainer,
    onPrimaryContainer = EcoEmeraldDark,
    secondary = EcoTealPrimary,
    onSecondary = Color.White,
    secondaryContainer = EcoTealContainer,
    tertiary = EcoAmberGold,
    tertiaryContainer = EcoAmberContainer,
    background = EcoSurfaceBg,
    surface = EcoCardBg,
    surfaceVariant = EcoMintContainer,
    onBackground = EcoTextDark,
    onSurface = EcoTextDark,
    error = EcoErrorRed
)

@Composable
fun EcotapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
