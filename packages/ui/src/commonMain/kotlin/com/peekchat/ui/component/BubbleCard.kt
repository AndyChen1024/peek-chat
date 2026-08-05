package com.peekchat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peekchat.designsystem.theme.WeChatGreenPrimary
import com.peekchat.model.BubblePosition

/**
 * 聊天气泡组件。
 * 根据 BubblePosition 决定对齐方式和颜色：
 * - LEFT (对方): 左对齐、白色背景
 * - RIGHT (自己): 右对齐、微信绿色背景
 */
@Composable
fun BubbleCard(
    content: String,
    position: BubblePosition,
    timestamp: String? = null,
    modifier: Modifier = Modifier
) {
    val isSelf = position == BubblePosition.RIGHT

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSelf) WeChatGreenPrimary else CardDefaults.cardColors().containerColor
            ),
            shape = CardDefaults.shape,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(content)
                if (timestamp != null) {
                    Text(
                        text = timestamp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
