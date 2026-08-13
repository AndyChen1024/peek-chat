package com.peekchat.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.peekchat.designsystem.theme.Brand400
import com.peekchat.designsystem.theme.Brand500
import com.peekchat.designsystem.theme.Brand700
import com.peekchat.designsystem.theme.PeekChatTheme
import com.peekchat.model.AnalysisReport
import com.peekchat.ui.screen.analysis.AnalysisScreen
import com.peekchat.ui.screen.capture.CaptureScreen

/**
 * 应用根 Composable。
 *
 * 权限引导流程（Iris PERMISSION-GUIDE.md spec）：
 * App 启动 → 检测 overlay 权限？
 *   YES → 正常采集页 + OverlayService
 *   NO  → 是否已被引导过？
 *           YES → 降级页面（不再弹引导卡片）
 *           NO  → 引导卡片 → 用户选择
 *                    去开启 → 系统设置 → onResume 重检
 *                    以后再说 → 记录已引导 → 降级页面
 */
@Composable
fun PeekChatApp(
    onRequestOverlayPermission: (() -> Unit)? = null,
    onShareLog: (() -> Unit)? = null,
    analysisReport: AnalysisReport? = null,
    onDismissReport: () -> Unit = {}
) {
    PeekChatTheme(
        darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    ) {
        AppContent(
            onRequestOverlayPermission = onRequestOverlayPermission,
            onShareLog = onShareLog,
            analysisReport = analysisReport,
            onDismissReport = onDismissReport
        )
    }
}

@Composable
private fun AppContent(
    onRequestOverlayPermission: (() -> Unit)? = null,
    onShareLog: (() -> Unit)? = null,
    analysisReport: AnalysisReport? = null,
    onDismissReport: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    val hasOverlayPermission = remember { canDrawOverlays(context) }
    val hasBeenGuided = remember { prefs.getBoolean(KEY_GUIDE_SHOWN, false) }

    var showGuideDialog by remember {
        mutableStateOf(!hasOverlayPermission && !hasBeenGuided)
    }

    if (showGuideDialog) {
        PermissionGuideDialog(
            onGoToSettings = {
                showGuideDialog = false
                prefs.edit().putBoolean(KEY_GUIDE_SHOWN, true).apply()
                onRequestOverlayPermission?.invoke()
            },
            onDismiss = {
                showGuideDialog = false
                prefs.edit().putBoolean(KEY_GUIDE_SHOWN, true).apply()
            }
        )
    } else if (!hasOverlayPermission && hasBeenGuided) {
        DeniedFallbackScreen(
            onPickImages = { /* TODO: launch system photo picker */ },
            onGoToSettings = {
                onRequestOverlayPermission?.invoke()
            }
        )
    } else if (analysisReport != null) {
        AnalysisScreen(
            report = analysisReport,
            onBack = onDismissReport
        )
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CaptureScreen(
                onStartAnalysis = { /* TODO */ },
                onViewHistory = { /* TODO */ }
            )
            if (onShareLog != null) {
                androidx.compose.material3.TextButton(
                    onClick = onShareLog,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        "导出日志",
                        color = Brand400
                    )
                }
            }
        }
    }
}

// ── Permission Guide Dialog ────────────────────────────────────────
// Iris spec: Modal Card, centered, branded copy

@Composable
private fun PermissionGuideDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp),  // radius-md
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // shadow-md
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration placeholder (Iris will provide final asset)
                Text(
                    text = "💬",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(12.dp))

                // Title: 18px/600
                Text(
                    text = "在微信上面放一个小圆点\n点一下就能记录这段对话",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                // Subtitle: 13px/400, text-secondary
                Text(
                    text = "眯聊需要在其他应用上层显示浮窗，\n系统要求你手动开启一次",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brand500,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                // Primary button: brand-700, white text, radius-full
                Button(
                    onClick = onGoToSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50), // radius-full
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700)
                ) {
                    Text("去开启")
                }
                Spacer(Modifier.height(8.dp))

                // Secondary: text link, no background
                TextButton(onClick = onDismiss) {
                    Text("以后再说", color = Brand400)
                }
            }
        }
    }
}

// ── Denied Fallback Screen ─────────────────────────────────────────
// Iris spec: icon + title + description + two buttons + history list

@Composable
private fun DeniedFallbackScreen(
    onPickImages: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            Text(
                "眯聊",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Disabled float icon placeholder
            Text(
                text = "🚫",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(16.dp))

            // Title: 18px/600
            Text(
                text = "浮窗采集功能未开启",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            // Description: 13px/400, text-secondary
            Text(
                text = "你可以手动导入微信截图，\n或在设置中开启浮窗权限",
                style = MaterialTheme.typography.bodySmall,
                color = Brand500,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Primary: brand-700 filled
            Button(
                onClick = onPickImages,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Brand700)
            ) {
                Text("从相册选择截图")
            }
            Spacer(Modifier.height(12.dp))

            // Secondary: outlined, brand-700 border
            OutlinedButton(
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("开启浮窗权限", color = Brand700)
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text(
                "历史记录",
                style = MaterialTheme.typography.titleSmall,
                color = Brand400
            )
            Spacer(Modifier.height(8.dp))

            // TODO: Wire up real history from Room database
            Text(
                "暂无记录",
                style = MaterialTheme.typography.bodySmall,
                color = Brand400
            )
        }
    }
}

// ── Utilities ──────────────────────────────────────────────────────

private fun canDrawOverlays(context: Context): Boolean {
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(context)
}

private const val PREFS_NAME = "peekchat_prefs"
private const val KEY_GUIDE_SHOWN = "permission_guide_shown"
