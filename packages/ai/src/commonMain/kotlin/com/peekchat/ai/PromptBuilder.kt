package com.peekchat.ai

import com.peekchat.model.ChatMessage
import com.peekchat.model.Conversation

/**
 * 构造发送给 AI 的分析 prompt。
 * 仅传递纯文本，不包含原始截图。
 */
object PromptBuilder {

    fun build(conversation: Conversation): String {
        val messagesText = conversation.messages.joinToString("\n") { message ->
            val role = when (message.speaker) {
                com.peekchat.model.Speaker.SELF -> "我"
                com.peekchat.model.Speaker.OTHER -> "对方"
                com.peekchat.model.Speaker.UNKNOWN -> "系统"
            }
            "$role: ${message.content}"
        }

        return """
你是一个聊天记录分析助手。请分析以下微信聊天记录，并以 JSON 格式输出分析结果。

要求：
1. 输出一段对话摘要（summary）
2. 提取所有待办事项（todos），并标注该事项由谁负责
3. 判断整体情绪（sentiment），列出正面和负面话题
4. 提取关键决策（decisions），并标注参与决策的人

聊天记录：
$messagesText

请严格按以下 JSON schema 格式输出（不要包含 markdown 标记）：
{
  "summary": "...",
  "todos": [{ "content": "...", "assignee": "SELF|OTHER|UNKNOWN" }],
  "sentiment": { "overall": "...", "positive": ["..."], "negative": ["..."] },
  "decisions": [{ "content": "...", "participants": ["SELF", "OTHER"] }]
}
""".trimIndent()
    }
}
