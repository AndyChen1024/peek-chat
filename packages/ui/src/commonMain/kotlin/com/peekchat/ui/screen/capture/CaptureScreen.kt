package com.peekchat.ui.screen.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 截图选择页面。
 * 用户从相册选择多张微信聊天截图。
 */
@Composable
fun CaptureScreen(
    onStartAnalysis: (List<String>) -> Unit,
    onViewHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            Text("Peek Chat", modifier = Modifier.padding(16.dp))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // TODO: 图片选择器网格
            Text("选择微信聊天截图")
            Button(onClick = { /* TODO: 打开相册 */ }) {
                Text("从相册选择")
            }
            Button(onClick = onViewHistory) {
                Text("历史记录")
            }
        }
    }
}
