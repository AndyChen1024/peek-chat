package com.peekchat.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.peekchat.model.OcrResult
import com.peekchat.model.TextLine
import java.io.File

/**
 * Android 平台 ML Kit Text Recognition v2 actual。
 *
 * Phase 1 默认 OCR 引擎。中文识别率 ~93%，离线运行。
 * 当实测识别率 <90% 时切换到 PaddleOcrEngine。
 */
class MlKitOcrEngine : OcrEngine {

    override suspend fun recognize(imagePath: String): OcrResult {
        val bitmap: Bitmap = BitmapFactory.decodeFile(imagePath)
            ?: return OcrResult(
                imageId = File(imagePath).nameWithoutExtension,
                lines = emptyList(),
                imageWidth = 0,
                imageHeight = 0,
                engineType = "mlkit"
            )

        // TODO: Integrate ML Kit Text Recognition v2
        // val inputImage = InputImage.fromBitmap(bitmap, 0)
        // val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        // val visionText = recognizer.process(inputImage).await()
        // val lines = visionText.textBlocks.flatMap { block ->
        //     block.lines.map { line ->
        //         TextLine(
        //             text = line.text,
        //             left = line.boundingBox?.left ?: 0,
        //             top = line.boundingBox?.top ?: 0,
        //             right = line.boundingBox?.right ?: 0,
        //             bottom = line.boundingBox?.bottom ?: 0,
        //             confidence = line.confidence ?: 1.0f
        //         )
        //     }
        // }

        return OcrResult(
            imageId = File(imagePath).nameWithoutExtension,
            lines = emptyList(),
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
            engineType = "mlkit"
        )
    }
}
