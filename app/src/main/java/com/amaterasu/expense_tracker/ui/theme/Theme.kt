package com.amaterasu.expense_tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    secondary = NightPrimary,
    tertiary = NightPrimary,
    background = NightBackground,
    surface = NightSurface,
    surfaceVariant = NightSurfaceVariant,
    outline = NightOutline,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = NightInk,
    onSurface = NightInk,
    onSurfaceVariant = NightMutedInk
)

private val LightColorScheme = lightColorScheme(
    primary = SlateBlue,
    secondary = SlateBlue,
    tertiary = SlateBlue,
    background = MistBackground,
    surface = SoftSurface,
    surfaceVariant = SoftSurfaceVariant,
    outline = SoftOutline,
    onPrimary = SoftSurface,
    onSecondary = SoftSurface,
    onTertiary = SoftSurface,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk
)

@Composable
fun ExpensetrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) DarkColorScheme else LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
