package com.zen.pistats.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val DarkColorScheme = darkColorScheme(
    primary = Mint500,
    secondary = Cyan400,
    tertiary = Mint300,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onPrimary = Slate900,
    onSecondary = Slate900,
    onBackground = Sand100,
    onSurface = Sand100,
    onSurfaceVariant = Steel100
)

private val LightColorScheme = lightColorScheme(
    primary = Slate900,
    secondary = Cyan400,
    tertiary = Mint500,
    background = Sand100,
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EDF3),
    onPrimary = Color.White,
    onSecondary = Slate900,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate700
)

@Composable
fun PiStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
