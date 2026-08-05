package com.peekchat.capture

/**
 * Android 平台采集引擎桩。
 * MVP 阶段将集成：
 * - Overlay API: 浮窗绘制
 * - MediaProjection: 系统级截图
 * - AccessibilityService: 自动滚动微信页面
 */
class AndroidCaptureEngine : CaptureEngine {

    override suspend fun capture(source: CaptureSource): Result<List<String>> {
        return when (source) {
            CaptureSource.FLOATING_WINDOW -> {
                // TODO: 启动浮窗 → 用户点击 → 触发自动滚动 + 连续截图
                error("Floating window capture not yet implemented")
            }
            CaptureSource.MANUAL_IMPORT -> {
                // TODO: 打开系统图片选择器，返回选中图片的路径列表
                error("Manual import not yet implemented")
            }
        }
    }
}
