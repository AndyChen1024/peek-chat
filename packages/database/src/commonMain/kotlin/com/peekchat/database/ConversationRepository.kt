package com.peekchat.database

import com.peekchat.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * 对话持久化接口。
 * 各平台通过 expect/actual 提供实现：
 * - Android: Room
 * - iOS: SQLDelight (future)
 */
interface ConversationRepository {
    fun getAll(): Flow<List<Conversation>>
    suspend fun getById(id: String): Conversation?
    suspend fun insert(conversation: Conversation)
    suspend fun delete(id: String)
}
