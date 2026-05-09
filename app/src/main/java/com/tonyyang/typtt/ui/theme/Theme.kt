package com.tonyyang.typtt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = TextPrimary,
)

@Composable
fun TypttTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
