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
     * @return 采集结果，包含截图列表和元数据
     */
    suspend fun capture(source: CaptureSource): CaptureResult
}

enum class CaptureSource {
    /** 浮窗自动采集 */
    FLOATING_WINDOW,

    /** 从相册手动导入已有截图 */
    MANUAL_IMPORT
}

/**
 * 单次采集的结果。
 */
data class CaptureResult(
    /** 采集到的截图文件路径列表 */
    val imagePaths: List<String>,

    /** 采集来源 */
    val source: CaptureSource,

    /** 截图总数（含滚动采集的多张） */
    val imageCount: Int = imagePaths.size,

    /** 采集耗时 (ms) */
    val durationMs: Long = 0
)
