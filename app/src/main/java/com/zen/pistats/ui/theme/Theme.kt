package com.zen.pistats.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Mint500,
    secondary = Cyan400,
    tertiary = Mint300,
    background = Ink950,
    surface = Slate900,
    surfaceVariant = Ocean900,
    onPrimary = Ink950,
    onSecondary = Slate900,
    onBackground = Sand100,
    onSurface = Sand100,
    onSurfaceVariant = Steel100,
    error = Danger,
)

private val LightColorScheme = lightColorScheme(
    primary = Slate900,
    secondary = Cyan400,
    tertiary = Mint500,
    background = Mist50,
    surface = Color.White,
    surfaceVariant = Mist100,
    onPrimary = Color.White,
    onSecondary = Slate900,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate700,
    error = Danger,
)

@Composable
fun PiStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
