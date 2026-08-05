package com.peekchat.ui.screen.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * AI 分析报告页面。
 * 展示：对话摘要、待办列表、情绪指示、关键决策。
 */
@Composable
fun AnalysisScreen(
    conversationId: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // TODO: 从 AnalysisRepository 加载 AnalysisReport
        // TODO: SummaryCard
        // TODO: TodoListCard
        // TODO: SentimentIndicator
        // TODO: DecisionListCard
        Text("加载分析报告...")
    }
}
