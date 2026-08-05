package com.peekchat.ocr

import android.graphics.Bitmap

/**
 * Android 平台 OCR 引擎桩。
 * MVP 开发阶段将集成 ML Kit Text Recognition v2。
 */
class MlKitOcrEngine : OcrEngine {

    override suspend fun recognize(imagePath: String): com.peekchat.model.OcrResult {
        // TODO: 实现 ML Kit OCR
        // 1. 加载图片 → Bitmap
        // 2. 创建 InputImage (fromBitmap + rotation)
        // 3. 调用 TextRecognition.getClient(ChineseTextRecognizerOptionsBuilder.build())
        // 4. 解析 TextBlock → TextLine → OcrResult
        error("ML Kit OCR not yet implemented")
    }
}
