package com.peekchat.ocr

import com.peekchat.model.OcrResult

/**
 * OCR 引擎接口。
 * 各平台实现：
 * - Android: ML Kit Text Recognition v2
 * - iOS: Vision framework (future)
 */
interface OcrEngine {
    /**
     * 对单张图片执行 OCR。
     * @param imagePath 图片文件路径
     * @return OCR 结果，包含所有文本行及其 bounding box
     */
    suspend fun recognize(imagePath: String): OcrResult
}
