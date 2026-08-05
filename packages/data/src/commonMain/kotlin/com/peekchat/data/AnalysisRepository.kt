package com.peekchat.data

import com.peekchat.ai.AiProvider
import com.peekchat.database.AnalysisReportRepository
import com.peekchat.database.ConversationRepository
import com.peekchat.model.AnalysisReport
import com.peekchat.model.Conversation
import com.peekchat.ocr.OcrEngine

/**
 * 分析结果 Repository。
 * 协调 OCR、AI、数据库，完成一次完整的分析流程。
 */
class AnalysisRepository(
    private val ocrEngine: OcrEngine,
    private val aiProvider: AiProvider,
    private val conversationRepository: ConversationRepository,
    private val reportRepository: AnalysisReportRepository
) {
    /**
     * 完整分析流程：OCR → 拼接 → AI 分析 → 持久化。
     * @param imagePaths 截图文件路径列表
     * @return 分析报告
     */
    suspend fun analyzeConversation(imagePaths: List<String>): Result<AnalysisReport> {
        // TODO: Step 1 — OCR + 气泡归属 + 拼接
        // TODO: Step 2 — 构造 Conversation
        // TODO: Step 3 — 保存 Conversation
        // TODO: Step 4 — 调用 AiProvider.analyze()
        // TODO: Step 5 — 保存 AnalysisReport
        error("Analysis pipeline not yet implemented")
    }
}
