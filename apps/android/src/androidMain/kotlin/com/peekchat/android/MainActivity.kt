package com.peekchat.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.peekchat.ai.DeepSeekProvider
import com.peekchat.android.capture.MediaProjectionService
import com.peekchat.android.capture.ScreenshotCapture
import com.peekchat.android.overlay.OverlayPermissionHelper
import com.peekchat.android.overlay.OverlayService
import com.peekchat.model.AnalysisReport
import com.peekchat.model.ChatMessage
import com.peekchat.model.Conversation
import com.peekchat.model.OcrResult
import com.peekchat.model.Speaker
import com.peekchat.ocr.BubbleClassifier
import com.peekchat.ocr.MlKitOcrEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var screenshotCapture: ScreenshotCapture
    private val ocrEngine = MlKitOcrEngine()
    private val bubbleClassifier = BubbleClassifier()
    private val aiProvider by lazy {
        DeepSeekProvider(
            httpClient = HttpClient(OkHttp),
            apiKey = BuildConfig.DEEPSEEK_API_KEY
        )
    }
    private val captureScope = CoroutineScope(Dispatchers.Main)

    // Mutable state: AI analysis result drives navigation to AnalysisScreen
    private var analysisReport by mutableStateOf<AnalysisReport?>(null)

    // ── MediaProjection result handler ─────────────────────────────

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            captureScope.launch {
                try {
                    val path = withContext(Dispatchers.IO) {
                        screenshotCapture.capture(
                            resultCode = result.resultCode,
                            data = result.data ?: return@withContext null
                        )
                    }
                    if (path != null) {
                        PeekLog.log(TAG, "Screenshot captured: $path")
                        // OCR
                        val ocrResult = withContext(Dispatchers.IO) {
                            ocrEngine.recognize(path)
                        }
                        PeekLog.log(TAG, "OCR: ${ocrResult.lines.size} lines, engine=${ocrResult.engineType}")

                        // Bubble classification
                        val messages = ocrResult.lines.map { line ->
                            val position = bubbleClassifier.classify(line, ocrResult.imageWidth)
                            ChatMessage(
                                speaker = when (position) {
                                    com.peekchat.model.BubblePosition.LEFT -> Speaker.OTHER
                                    com.peekchat.model.BubblePosition.RIGHT -> Speaker.SELF
                                },
                                content = line.text,
                                bubblePosition = position
                            )
                        }

                        // AI analysis
                        val conversation = Conversation(
                            id = ocrResult.imageId,
                            messages = messages
                        )
                        val result = aiProvider.analyze(conversation)
                        result.fold(
                            onSuccess = { report ->
                                PeekLog.log(TAG, "AI: summary=${report.summary.take(60)}..., todos=${report.todos.size}, decisions=${report.decisions.size}")
                                analysisReport = report
                            },
                            onFailure = { e ->
                                PeekLog.error(TAG, "AI analysis failed: ${e.message}", e)
                            }
                        )
                    }
                } catch (e: Exception) {
                    PeekLog.error(TAG, "Capture pipeline failed: ${e.message}", e)
                }
            }
        }
    }

    // ── Overlay permission result handler ──────────────────────────

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // User returned from system settings. Re-check and start overlay if granted.
        if (OverlayPermissionHelper.isGranted(this)) {
            startOverlayService()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PeekLog.init(this)
        PeekLog.log(TAG, "MainActivity created")
        screenshotCapture = ScreenshotCapture(this)

        setContent {
            PeekChatApp(
                onRequestOverlayPermission = { requestOverlayPermission() },
                onShareLog = { shareLog() },
                analysisReport = analysisReport,
                onDismissReport = { analysisReport = null }
            )
        }

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Only auto-start overlay if permission is granted and service not already running.
        // startService is idempotent, but we check first to avoid multiple intent deliveries.
        if (OverlayPermissionHelper.isGranted(this) && !OverlayService.isRunning) {
            startOverlayService()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // ── Intent handling ────────────────────────────────────────────

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == OverlayService.ACTION_START_CAPTURE) {
            // Post to next frame: Activity must be fully resumed before
            // registerForActivityResult launcher can fire.
            window?.decorView?.post {
                startCapture()
            }
        }
    }

    private fun startCapture() {
        PeekLog.log(TAG, "Starting MediaProjection capture flow")
        // Android 14+ requires the mediaProjection foreground service to be running
        // before getMediaProjection() — start it first.
        val serviceIntent = Intent(this, MediaProjectionService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        val permissionIntent = screenshotCapture.createPermissionIntent()
        mediaProjectionLauncher.launch(permissionIntent)
    }

    // ── Overlay permission flow ────────────────────────────────────
    // Atlas's 2-step: detect → guide card → system settings → return → start

    private fun requestOverlayPermission() {
        if (OverlayPermissionHelper.isGranted(this)) {
            startOverlayService()
        } else {
            val intent = OverlayPermissionHelper.createSettingsIntent(this)
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun shareLog() {
        val file = PeekLog.getLogFile()
        if (file == null || !file.exists()) {
            PeekLog.log(TAG, "No log file to share")
            return
        }
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "分享日志"))
        } catch (e: Exception) {
            PeekLog.error(TAG, "Failed to share log", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
