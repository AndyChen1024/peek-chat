package com.peekchat.model

import kotlinx.serialization.Serializable

/**
 * AI 分析报告。
 */
@Serializable
data class AnalysisReport(
    val conversationId: String,

    /** 对话摘要 */
    val summary: String,

    /** 待办事项 */
    val todos: List<TodoItem>,

    /** 情绪判断 */
    val sentiment: Sentiment,

    /** 关键决策 */
    val decisions: List<Decision>,

    val createdAt: Long
)

@Serializable
data class TodoItem(
    val content: String,

    /** 这件事需要谁来做 */
    val assignee: Speaker
)

@Serializable
data class Sentiment(
    /** 整体情绪标签 */
    val overall: String,

    /** 正面话题 */
    val positive: List<String>,

    /** 负面话题 */
    val negative: List<String>
)

@Serializable
data class Decision(
    val content: String,

    /** 参与决策的人 */
    val participants: List<Speaker>
)
