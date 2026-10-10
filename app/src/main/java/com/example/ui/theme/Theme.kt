package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LoveHealthyPrimary,
    onPrimary = Color.White,
    secondary = LoveHealthyCyanAccent,
    onSecondary = LoveHealthyDarkBlue,
    tertiary = LoveHealthyLightCyan,
    background = LoveHealthyBackgroundDark,
    surface = LoveHealthySurfaceDark,
    onBackground = LoveHealthyTextPrimaryDark,
    onSurface = LoveHealthyTextPrimaryDark,
    onSurfaceVariant = LoveHealthyTextSecondaryDark,
    surfaceVariant = Color(0xFF1B3238),
    primaryContainer = LoveHealthyCyanAccent,
    onPrimaryContainer = LoveHealthyDarkBlue,
    error = LoveHealthyEmergencyRed
)

private val LightColorScheme = lightColorScheme(
    primary = LoveHealthyPrimary,
    onPrimary = Color.White,
    secondary = LoveHealthyCyanAccent,
    onSecondary = LoveHealthyDarkBlue,
    tertiary = LoveHealthyLightCyan,
    background = LoveHealthyBackground,
    surface = LoveHealthySurface,
    onBackground = LoveHealthyDarkBlue,
    onSurface = LoveHealthyDarkBlue,
    onSurfaceVariant = LoveHealthyDarkBlue.copy(alpha = 0.7f),
    surfaceVariant = LoveHealthyContainer,
    primaryContainer = LoveHealthyLightCyan,
    onPrimaryContainer = LoveHealthyDarkBlue,
    error = LoveHealthyEmergencyRed
)

@Composable
fun KesehatanVisualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep medical identity
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
