package com.glacier.profilapps

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF050B1E)
val Surface2 = Color(0xFF0D1B3D)
val Ice = Color(0xFFBFE6FF)
val Accent = Color(0xFF4FC3FF)
val AccentDeep = Color(0xFF2563EB)

@Composable
fun GlacierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent, onPrimary = Bg, background = Bg, surface = Surface2,
            onBackground = Color.White, onSurface = Color.White,
        ),
        content = content
    )
}
