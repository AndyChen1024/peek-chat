package com.peekchat.ui.screen.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 历史记录页面。
 * 展示所有已分析的聊天记录列表。
 */
@Composable
fun HistoryScreen(
    onSelectConversation: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // TODO: 从 ConversationRepository 加载列表
        // TODO: LazyColumn 展示 Conversation 卡片
        Text("历史记录")
    }
}
