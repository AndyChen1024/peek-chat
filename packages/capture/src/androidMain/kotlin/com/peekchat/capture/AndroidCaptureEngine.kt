package com.peekchat.capture

import android.content.Context

/**
 * Android 平台采集引擎。
 *
 * MVP 阶段集成：
 * - Overlay API: 浮窗绘制（眯着态 → 睁眼态）
 * - MediaProjection: 系统级截图（需用户授权）
 * - AccessibilityService: 自动滚动微信页面
 *
 * @param context Android Context，用于权限请求和系统服务
 */
class AndroidCaptureEngine(private val context: Context) : CaptureEngine {

    override suspend fun capture(source: CaptureSource): CaptureResult {
        return when (source) {
            CaptureSource.FLOATING_WINDOW -> captureViaOverlay()
            CaptureSource.MANUAL_IMPORT -> captureViaManualImport()
        }
    }

    private suspend fun captureViaOverlay(): CaptureResult {
        // TODO(Phase 1): 集成浮窗自动采集流程
        // 1. 用户点击浮窗（OverlayService 中的 pill 按钮）
        // 2. 请求 MediaProjection 权限（startActivityForResult → Intent）
        // 3. 启动 AccessibilityService 滚动微信页面
        // 4. 在每次滚动后调用 MediaProjection 截图
        // 5. 收集所有截图路径 → 返回 CaptureResult
        return CaptureResult(
            imagePaths = emptyList(),
            source = CaptureSource.FLOATING_WINDOW,
            durationMs = 0
        )
    }

    private suspend fun captureViaManualImport(): CaptureResult {
        // TODO(Phase 1): 集成系统图片选择器
        // 1. 打开系统图片选择器（Intent.ACTION_GET_CONTENT 或 PhotoPicker）
        // 2. 用户选择已有截图
        // 3. 返回选中图片的 URI 列表
        return CaptureResult(
            imagePaths = emptyList(),
            source = CaptureSource.MANUAL_IMPORT,
            durationMs = 0
        )
    }
}
