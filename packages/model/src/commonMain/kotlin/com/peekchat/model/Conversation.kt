package com.peekchat.model

import kotlinx.serialization.Serializable

/**
 * 一次完整的聊天对话，可能由多张截图拼接而成。
 */
@Serializable
data class Conversation(
    val id: String,
    val messages: List<ChatMessage>,

    /** 来源截图数量 */
    val imageCount: Int = 1,

    /** 创建时间 (epoch millis) */
    val createdAt: Long = 0L
)
