package com.peekchat.ui.navigation

/**
 * 自建导航系统。
 *
 * CMP 生态无统一导航方案，MVP 阶段项目规模小，
 * 用手动管理的 back stack 代替第三方导航框架。
 */
sealed class Screen {
    /** 首页：选图入口 */
    data object CaptureHome : Screen()

    /** OCR 预览 */
    data class OcrPreview(val imageCount: Int) : Screen()

    /** 分析报告 */
    data class AnalysisReport(val conversationId: String) : Screen()

    /** 历史记录 */
    data object History : Screen()
}
