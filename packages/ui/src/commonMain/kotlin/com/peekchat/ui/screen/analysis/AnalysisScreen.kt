package com.peekchat.ui.screen.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peekchat.model.AnalysisReport
import com.peekchat.ui.component.DecisionCard
import com.peekchat.ui.component.SentimentCard
import com.peekchat.ui.component.SummaryCard
import com.peekchat.ui.component.TodoCard

/**
 * AI 分析报告页面。
 *
 * Layout per Iris spec: SummaryCard → TodoCard → SentimentCard → DecisionCard,
 * vertical scroll, 16dp page padding, 12dp card spacing.
 */
@Composable
fun AnalysisScreen(
    report: AnalysisReport,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Text(
                "分析报告",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(summary = report.summary)

            TodoCard(todos = report.todos)

            SentimentCard(sentiment = report.sentiment)

            DecisionCard(decisions = report.decisions)
        }
    }
}
