package com.peekchat.ai

import com.peekchat.model.AnalysisReport
import com.peekchat.model.Conversation

/**
 * AI Provider 接口。
 * 各平台实现：
 * - Android: OkHttp (Ktor) → DeepSeek API
 * - iOS: URLSession → DeepSeek API (future)
 */
interface AiProvider {
    /**
     * 对结构化对话进行 AI 分析。
     * @param conversation 结构化对话（纯文本，不含原始图片）
     * @return 分析报告
     */
    suspend fun analyze(conversation: Conversation): Result<AnalysisReport>
}
