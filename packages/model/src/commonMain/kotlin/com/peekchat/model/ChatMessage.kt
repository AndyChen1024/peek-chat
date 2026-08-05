package com.peekchat.model

import kotlinx.serialization.Serializable

/**
 * 单条聊天消息。
 *
 * 由 OCR 引擎从截图中提取，并经气泡归属判断后生成。
 */
@Serializable
data class ChatMessage(
    /** 说话人：自己 / 对方 / 未知 */
    val speaker: Speaker,

    /** 消息文本内容 */
    val content: String,

    /** OCR 提取的原始时间文本（如 "下午 3:42"），可能为 null */
    val timestamp: String? = null,

    /** 气泡在屏幕中的水平位置 */
    val bubblePosition: BubblePosition
)
