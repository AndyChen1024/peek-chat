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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.peekchat.designsystem.theme.SemanticTodo
import com.peekchat.designsystem.theme.SemanticTodoBg
import com.peekchat.model.TodoItem

/**
 * 待办事项卡片 — 展示从对话中提取的待办列表。
 *
 * Spec: Amber semantic color, icon + title → todo list with owner tags.
 */
@Composable
fun TodoCard(
    todos: List<TodoItem>,
    modifier: Modifier = Modifier
) {
    if (todos.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋")
                Spacer(Modifier.width(4.dp))
                Text(
                    "待办事项",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(Modifier.height(8.dp))

            todos.forEach { todo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("○")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        todo.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "@${todo.assignee.name.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SemanticTodo,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .then(
                                Modifier
                                    // Simple text-based tag — background via drawBehind or a Surface
                            )
                    )
                }
            }
        }
    }
}
