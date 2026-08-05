package com.peekchat.ocr

import com.peekchat.model.ChatMessage
import com.peekchat.model.Speaker

/**
 * 多截图拼接去重器。
 *
 * 用户可能连续截 3-5 张图来覆盖长聊天，截图间有重叠。
 * 通过文本相似度检测重叠区域并去重，形成一条连续对话时间线。
 *
 * TODO: 实现文本相似度算法（MVP 阶段可用简单的前缀/后缀匹配）
 */
class ConversationStitcher {

    /**
     * 拼接多张截图的 OCR 结果，去重后返回完整消息列表。
     */
    fun stitch(
        messagesPerImage: List<List<ChatMessage>>
    ): List<ChatMessage> {
        if (messagesPerImage.isEmpty()) return emptyList()
        if (messagesPerImage.size == 1) return messagesPerImage.first()

        val result = mutableListOf<ChatMessage>()
        result.addAll(messagesPerImage.first())

        for (i in 1 until messagesPerImage.size) {
            val previous = messagesPerImage[i - 1]
            val current = messagesPerImage[i]

            // 找到上一张图的末尾与当前图的开头的重叠点
            val overlapCount = findOverlap(previous, current)

            // 跳过重叠的消息，追加新消息
            result.addAll(current.drop(overlapCount))
        }

        return result
    }

    /**
     * 找到两张截图消息列表的重叠数。
     * 简单实现：从第一张末尾和第二张开头逐条匹配。
     *
     * TODO: 替换为更精确的文本相似度算法
     */
    private fun findOverlap(
        previous: List<ChatMessage>,
        current: List<ChatMessage>
    ): Int {
        var maxOverlap = 0
        for (len in 1..minOf(previous.size, current.size)) {
            val prevTail = previous.takeLast(len)
            val currHead = current.take(len)
            if (messagesMatch(prevTail, currHead)) {
                maxOverlap = len
            }
        }
        return maxOverlap
    }

    private fun messagesMatch(a: List<ChatMessage>, b: List<ChatMessage>): Boolean {
        if (a.size != b.size) return false
        return a.zip(b).all { (x, y) ->
            x.content.trim() == y.content.trim() && x.speaker == y.speaker
        }
    }
}
