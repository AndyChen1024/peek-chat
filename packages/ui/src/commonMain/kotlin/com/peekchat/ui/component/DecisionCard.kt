package com.peekchat.ui.component

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
import androidx.compose.ui.unit.dp
import com.peekchat.model.Decision

/**
 * 关键决策卡片 — 展示对话中识别出的关键决策。
 *
 * Spec: Blue semantic color, icon + title → decision descriptions.
 */
@Composable
fun DecisionCard(
    decisions: List<Decision>,
    modifier: Modifier = Modifier
) {
    if (decisions.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔵")
                Spacer(Modifier.width(4.dp))
                Text(
                    "关键决策",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(Modifier.height(8.dp))

            decisions.forEach { decision ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•")
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            decision.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (decision.participants.isNotEmpty()) {
                            Text(
                                "参与: ${decision.participants.joinToString(", ") { it.name.lowercase() }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
