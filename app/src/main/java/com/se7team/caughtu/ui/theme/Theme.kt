package com.se7team.caughtu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Dark mode is forced
private val CaughtUColors = darkColorScheme(
    primary = Purple,
    background = Background,
    surface = Surface,
)

@Composable
fun CaughtUTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CaughtUColors, content = content)
}
