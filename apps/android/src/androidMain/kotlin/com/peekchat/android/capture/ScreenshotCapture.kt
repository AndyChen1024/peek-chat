package com.peekchat.android.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * MediaProjection 截图工具。
 *
 * 负责：
 * 1. 请求 MediaProjection 权限（通过 Activity result）
 * 2. 创建 VirtualDisplay + ImageReader
 * 3. 截图并保存到内部存储
 *
 * 注意：MediaProjection Intent 必须从 Activity 的 startActivityForResult
 * 或 registerForActivityResult 启动。本类提供 createIntent() 来生成 Intent，
 * 调用方（Activity/Fragment）负责启动并在 onActivityResult 回调中获取
 * MediaProjection data intent，传入 capture() 方法。
 */
class ScreenshotCapture(context: Context) {

    private val mediaProjectionManager: MediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val displayMetrics = DisplayMetrics().also {
        windowManager.defaultDisplay.getRealMetrics(it)
    }

    val screenWidth: Int get() = displayMetrics.widthPixels
    val screenHeight: Int get() = displayMetrics.heightPixels
    val screenDensity: Int get() = displayMetrics.densityDpi

    /**
     * 创建 MediaProjection 权限请求 Intent。
     * 调用方需用 startActivityForResult 或 registerForActivityResult 启动。
     */
    fun createPermissionIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    /**
     * 执行单次截图。
     *
     * @param resultCode Activity.onActivityResult 中的 resultCode
     * @param data Activity.onActivityResult 中的 data intent
     * @param outputDir 截图保存目录
     * @return 保存的截图文件路径
     */
    suspend fun capture(
        resultCode: Int,
        data: Intent,
        outputDir: File = File("/data/data/com.peekchat.android/files/captures")
    ): String = suspendCancellableCoroutine { continuation ->

        if (resultCode != android.app.Activity.RESULT_OK) {
            continuation.resumeWithException(
                IllegalStateException("MediaProjection permission denied (resultCode=$resultCode)")
            )
            return@suspendCancellableCoroutine
        }

        val projection: MediaProjection = mediaProjectionManager.getMediaProjection(
            resultCode, data
        ) ?: run {
            continuation.resumeWithException(
                IllegalStateException("Failed to get MediaProjection")
            )
            return@suspendCancellableCoroutine
        }

        outputDir.mkdirs()

        val imageReader = ImageReader.newInstance(
            screenWidth, screenHeight,
            PixelFormat.RGBA_8888,
            2 // max images buffered
        )

        var virtualDisplay: VirtualDisplay? = null
        var imagePath: String? = null

        imageReader.setOnImageAvailableListener({ reader ->
            var image: Image? = null
            try {
                image = reader.acquireLatestImage()
                val plane = image?.planes?.getOrNull(0) ?: return@setOnImageAvailableListener
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * screenWidth

                val bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                // Crop to actual screen size (remove padding)
                val cropped = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                bitmap.recycle()

                val file = File(outputDir, "capture_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { fos ->
                    cropped.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                cropped.recycle()

                imagePath = file.absolutePath
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            } finally {
                image?.close()
                virtualDisplay?.release()
                imageReader.close()
                projection.stop()

                if (continuation.isActive) {
                    val path = imagePath
                    if (path != null) {
                        continuation.resume(path)
                    } else {
                        continuation.resumeWithException(
                            RuntimeException("Screenshot failed: no image captured")
                        )
                    }
                }
            }
        }, Handler(Looper.getMainLooper()))

        virtualDisplay = projection.createVirtualDisplay(
            "peekchat-screenshot",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null, null
        )
    }
}
