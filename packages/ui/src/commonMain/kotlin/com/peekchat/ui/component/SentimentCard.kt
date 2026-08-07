package com.peekchat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.peekchat.designsystem.theme.Brand400
import com.peekchat.designsystem.theme.SemanticSentimentNegative
import com.peekchat.designsystem.theme.SemanticSentimentPositive
import com.peekchat.model.Sentiment

/**
 * 情绪洞察卡片 — 对话基调 + 正负面话题分布。
 *
 * Spec: Green/Orange semantic colors, horizontal distribution bar with percentages.
 */
@Composable
fun SentimentCard(
    sentiment: Sentiment,
    modifier: Modifier = Modifier
) {
    val total = sentiment.positive.size + sentiment.negative.size
    val positiveRatio = if (total > 0) sentiment.positive.size.toFloat() / total else 0.5f
    val negativeRatio = if (total > 0) sentiment.negative.size.toFloat() / total else 0.5f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎭")
                Spacer(Modifier.width(4.dp))
                Text(
                    "情绪洞察",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(Modifier.height(8.dp))

            // Overall sentiment text
            Text(
                sentiment.overall,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            // Distribution bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (total > 0) {
                    Row(
                        modifier = Modifier
                            .weight(positiveRatio)
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(SemanticSentimentPositive)
                    ) {}
                    Row(
                        modifier = Modifier
                            .weight(negativeRatio)
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(SemanticSentimentNegative)
                    ) {}
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Brand400)
                    ) {}
                }
            }
            Spacer(Modifier.height(4.dp))

            // Percentage labels
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "正面 ${(positiveRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SemanticSentimentPositive
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "负面 ${(negativeRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SemanticSentimentNegative
                )
            }
        }
    }
}
