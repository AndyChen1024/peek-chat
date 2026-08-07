package com.peekchat.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.peekchat.model.OcrResult
import com.peekchat.model.TextLine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android 平台 ML Kit Text Recognition v2 actual。
 *
 * Phase 1 默认 OCR 引擎。中文识别率 ~93%，离线运行。
 * 当实测识别率 <90% 时切换到 PaddleOcrEngine。
 */
class MlKitOcrEngine : OcrEngine {

    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    override suspend fun recognize(imagePath: String): OcrResult {
        val bitmap: Bitmap = BitmapFactory.decodeFile(imagePath)
            ?: return OcrResult(
                imageId = File(imagePath).nameWithoutExtension,
                lines = emptyList(),
                imageWidth = 0,
                imageHeight = 0,
                engineType = "mlkit"
            )

        val inputImage = InputImage.fromBitmap(bitmap, 0)

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val lines = visionText.textBlocks.flatMap { block ->
                        block.lines.map { line ->
                            TextLine(
                                text = line.text,
                                left = line.boundingBox?.left ?: 0,
                                top = line.boundingBox?.top ?: 0,
                                right = line.boundingBox?.right ?: 0,
                                bottom = line.boundingBox?.bottom ?: 0,
                                confidence = line.confidence ?: 1.0f
                            )
                        }
                    }
                    continuation.resume(
                        OcrResult(
                            imageId = File(imagePath).nameWithoutExtension,
                            lines = lines,
                            imageWidth = bitmap.width,
                            imageHeight = bitmap.height,
                            engineType = "mlkit"
                        )
                    )
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
}
