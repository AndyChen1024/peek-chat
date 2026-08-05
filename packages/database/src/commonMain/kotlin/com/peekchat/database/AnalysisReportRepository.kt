package com.peekchat.database

import com.peekchat.model.AnalysisReport
import kotlinx.coroutines.flow.Flow

interface AnalysisReportRepository {
    fun getAll(): Flow<List<AnalysisReport>>
    suspend fun getByConversationId(conversationId: String): AnalysisReport?
    suspend fun insert(report: AnalysisReport)
    suspend fun delete(id: String)
}
