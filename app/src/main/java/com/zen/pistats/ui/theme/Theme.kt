package com.zen.pistats.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Mint500,
    onPrimary = Ink950,
    primaryContainer = Ocean700,
    onPrimaryContainer = Sand100,
    secondary = Cyan400,
    onSecondary = Slate900,
    secondaryContainer = Ocean900,
    onSecondaryContainer = Steel100,
    tertiary = Mint300,
    onTertiary = Slate900,
    background = Ink950,
    onBackground = Sand100,
    surface = Slate900,
    onSurface = Sand100,
    surfaceVariant = Ocean900,
    onSurfaceVariant = Steel100,
    error = Danger,
)

private val LightColorScheme = lightColorScheme(
    primary = Ocean700,
    onPrimary = Color.White,
    primaryContainer = Cyan200,
    onPrimaryContainer = Ink950,
    secondary = Cyan400,
    onSecondary = Slate900,
    secondaryContainer = Mist100,
    onSecondaryContainer = Slate900,
    tertiary = Mint500,
    onTertiary = Ink950,
    background = Mist50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Mist100,
    onSurfaceVariant = Slate700,
    error = Danger,
)

@Composable
fun PiStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
