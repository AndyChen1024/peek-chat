package com.peekchat.android

import androidx.compose.runtime.Composable
import com.peekchat.designsystem.theme.PeekChatTheme
import com.peekchat.ui.navigation.Screen
import com.peekchat.ui.screen.analysis.AnalysisScreen
import com.peekchat.ui.screen.capture.CaptureScreen
import com.peekchat.ui.screen.history.HistoryScreen

/**
 * 应用根 Composable。
 *
 * 组装 packages/ui 里的 Screen，通过自建 back stack 管理导航。
 * MVP 阶段功能逐步实现，当前为骨架。
 */
@Composable
fun PeekChatApp() {
    PeekChatTheme(
        darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    ) {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    // TODO: 实现 back stack 导航
    // val backStack = remember { mutableStateListOf<Screen>(Screen.CaptureHome) }
    // when (val current = backStack.lastOrNull()) {
    //     is Screen.CaptureHome -> CaptureScreen(...)
    //     is Screen.OcrPreview -> ...
    //     is Screen.AnalysisReport -> AnalysisScreen(...)
    //     is Screen.History -> HistoryScreen(...)
    //     null -> {}
    // }

    CaptureScreen(
        onStartAnalysis = { /* TODO */ },
        onViewHistory = { /* TODO */ }
    )
}
