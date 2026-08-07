package com.peekchat.ocr

/**
 * PaddleOCR (PP-OCRv4) actual — 备选 OCR 引擎。
 *
 * Phase 1 作为备选方案：ML Kit 实测中文识别率 <90% 时启用。
 * PaddleOCR 优势：中文 97.2% 准确率、5MB 模型体积、文字方向校正。
 * 集成方式：通过 JNI 桥接 PaddleOCR 的 C++ native 库。
 *
 * TODO(Phase 1): ML Kit 不达标时实现
 */
class PaddleOcrEngine : OcrEngine {

    override suspend fun recognize(imagePath: String): com.peekchat.model.OcrResult {
        // TODO: 实现 PaddleOCR JNI 集成
        // 1. 加载图片 → Bitmap
        // 2. 调用 PaddleOCR native 方法 (JNI bridge)
        // 3. 解析识别结果 → OcrResult (engineType = "paddleocr")
        error("PaddleOCR not yet implemented — use MlKitOcrEngine for Phase 1")
    }
}
