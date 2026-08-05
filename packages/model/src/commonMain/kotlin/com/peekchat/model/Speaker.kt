package com.peekchat.model

import kotlinx.serialization.Serializable

@Serializable
enum class Speaker {
    /** 自己说的 */
    SELF,

    /** 对方说的 */
    OTHER,

    /** 无法判断（如系统消息） */
    UNKNOWN
}
