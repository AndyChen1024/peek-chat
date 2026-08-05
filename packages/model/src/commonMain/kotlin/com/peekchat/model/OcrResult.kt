package com.peekchat.model

import kotlinx.serialization.Serializable

/**
 * 单次 OCR 扫描的原始结果（单张截图）。
 */
@Serializable
data class OcrResult(
    /** 图片标识 */
    val imageId: String,

    /** 提取到的文本行列表，每个元素包含文本和 bounding box */
    val lines: List<TextLine>,

    /** 图片宽度 (px) */
    val imageWidth: Int,

    /** 图片高度 (px) */
    val imageHeight: Int
)

@Serializable
data class TextLine(
    val text: String,

    /** bounding box 左边界 x 坐标 */
    val left: Int,

    /** bounding box 上边界 y 坐标 */
    val top: Int,

    /** bounding box 右边界 x 坐标 */
    val right: Int,

    /** bounding box 下边界 y 坐标 */
    val bottom: Int
)
