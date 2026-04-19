package com.example.luontopelibertti71.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnDark,
    background = Navy900,
    onBackground = OnDark,
    surface = Navy800,
    onSurface = OnDark,
    surfaceVariant = Navy700,
    onSurfaceVariant = OnDarkMuted,
    outline = Navy600
)

@Composable
fun LuontopeliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}