package com.peekchat.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = AccentBlue,
    surface = WeChatWhite,
    background = SurfaceLight,
    onBackground = Color(0xFF191919)
)

private val darkScheme = darkColorScheme(
    primary = AccentBlue,
    surface = SurfaceDark,
    background = Color(0xFF000000),
    onBackground = Color(0xFFEBEBEB)
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
