package com.peekchat.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.peekchat.ai.DeepSeekProvider
import com.peekchat.android.capture.ScreenshotCapture
import com.peekchat.android.overlay.OverlayPermissionHelper
import com.peekchat.android.overlay.OverlayService
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
        // API key is read from BuildConfig (set via gradle property or env var).
        // For local dev: add DEEPSEEK_API_KEY to ~/.gradle/gradle.properties
        DeepSeekProvider(
            httpClient = HttpClient(OkHttp),
            apiKey = deepseekApiKey()
        )
    }
    private val captureScope = CoroutineScope(Dispatchers.Main)

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
                        // OCR
                        val ocrResult = withContext(Dispatchers.IO) {
                            ocrEngine.recognize(path)
                        }
                        Log.i(TAG, "OCR: ${ocrResult.lines.size} lines, engine=${ocrResult.engineType}")

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
                                Log.i(TAG, "AI: summary=${report.summary.take(60)}..., todos=${report.todos.size}, decisions=${report.decisions.size}")
                            },
                            onFailure = { e ->
                                Log.e(TAG, "AI analysis failed: ${e.message}", e)
                            }
                        )
                    }
                } catch (e: Exception) {
                    // TODO: Show error notification or toast
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
        screenshotCapture = ScreenshotCapture(this)

        setContent {
            PeekChatApp(
                onRequestOverlayPermission = { requestOverlayPermission() }
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
            startCapture()
        }
    }

    private fun startCapture() {
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

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Read DeepSeek API key from system property or gradle property.
         * Set via: ./gradlew -Ddeepseek.api.key=sk-xxx ... or add to gradle.properties.
         */
        private fun deepseekApiKey(): String {
            return System.getProperty("deepseek.api.key")
                ?: System.getenv("DEEPSEEK_API_KEY")
                ?: "sk-placeholder" // Replace with your key
        }
    }
}
