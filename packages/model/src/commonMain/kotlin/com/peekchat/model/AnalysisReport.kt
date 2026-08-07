package com.peekchat.model

import kotlinx.serialization.Serializable

/**
 * AI 分析报告。
 */
@Serializable
data class AnalysisReport(
    /** 对话标识（本地分配，非 AI 输出） */
    val conversationId: String = "",

    /** 对话摘要 */
    val summary: String,

    /** 待办事项 */
    val todos: List<TodoItem> = emptyList(),

    /** 情绪判断 */
    val sentiment: Sentiment = Sentiment("未知", emptyList(), emptyList()),

    /** 关键决策 */
    val decisions: List<Decision> = emptyList(),

    /** 创建时间戳（本地分配，非 AI 输出） */
    val createdAt: Long = 0L
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
