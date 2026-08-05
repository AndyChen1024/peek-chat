package com.peekchat.capture

/**
 * 采集引擎接口。
 * 各平台实现：
 * - Android: 浮窗 (Overlay API) + 自动截图 (MediaProjection) + 手动导入
 * - iOS: (future)
 */
interface CaptureEngine {
    /**
     * 启动采集流程。
     * @param source 采集来源（浮窗 / 手动导入）
     * @return 截图文件路径列表
     */
    suspend fun capture(source: CaptureSource): Result<List<String>>
}

enum class CaptureSource {
    /** 浮窗自动采集 */
    FLOATING_WINDOW,

    /** 从相册手动导入已有截图 */
    MANUAL_IMPORT
}
