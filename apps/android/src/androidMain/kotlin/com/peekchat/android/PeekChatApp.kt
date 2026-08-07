package com.peekchat.android

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peekchat.designsystem.theme.Brand500
import com.peekchat.designsystem.theme.PeekChatTheme
import com.peekchat.ui.navigation.Screen
import com.peekchat.ui.screen.capture.CaptureScreen
import com.peekchat.ui.screen.history.HistoryScreen

/**
 * 应用根 Composable。
 *
 * 组装 packages/ui 里的 Screen，通过自建 back stack 管理导航。
 * MVP 阶段功能逐步实现，当前为骨架。
 */
@Composable
fun PeekChatApp(
    onRequestOverlayPermission: (() -> Unit)? = null
) {
    PeekChatTheme(
        darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    ) {
        AppContent(onRequestOverlayPermission = onRequestOverlayPermission)
    }
}

@Composable
private fun AppContent(onRequestOverlayPermission: (() -> Unit)? = null) {
    val context = LocalContext.current
    var showPermissionCard by remember { mutableStateOf(!canDrawOverlays(context)) }

    if (showPermissionCard) {
        OverlayPermissionCard(
            onEnable = {
                onRequestOverlayPermission?.invoke()
            }
        )
    } else {
        // TODO: 实现 back stack 导航
        CaptureScreen(
            onStartAnalysis = { /* TODO */ },
            onViewHistory = { /* TODO */ }
        )
    }
}

@Composable
private fun OverlayPermissionCard(onEnable: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👁️",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "眯聊需要一个浮窗来帮你采集对话",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "在微信上面放一个小圆点，点一下就能记住这段对话",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = Brand500,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "点击下方按钮，在系统设置中\n允许「显示在其他应用上层」",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onEnable) {
            Text("去开启")
        }
    }
}

private fun canDrawOverlays(context: Context): Boolean {
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(context)
}
