package com.peekchat.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = Brand700,
    onPrimary = Color.White,
    secondary = Brand500,
    tertiary = Brand400,
    surface = SurfaceLight,
    background = Brand50,
    onBackground = Color(0xFF1E293B),
    outline = SurfaceBorder,
)

private val darkScheme = darkColorScheme(
    primary = Brand400,
    onPrimary = Color(0xFF0F172A),
    secondary = Brand500,
    tertiary = Brand700,
    surface = SurfaceDark,
    background = Color(0xFF020617),
    onBackground = TextDark,
    outline = SurfaceBorderDark,
)

@Composable
fun PeekChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        content = content
    )
}
