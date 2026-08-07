package com.peekchat.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.peekchat.android.capture.ScreenshotCapture
import com.peekchat.android.overlay.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var screenshotCapture: ScreenshotCapture
    private val captureScope = CoroutineScope(Dispatchers.Main)

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
                        // TODO(Phase 1): Trigger OCR on captured image
                        // ocrEngine.recognize(path)
                    }
                } catch (e: Exception) {
                    // TODO: Show error notification or toast
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        screenshotCapture = ScreenshotCapture(this)

        setContent {
            PeekChatApp()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == OverlayService.ACTION_START_CAPTURE) {
            startCapture()
        }
    }

    private fun startCapture() {
        val permissionIntent = screenshotCapture.createPermissionIntent()
        mediaProjectionLauncher.launch(permissionIntent)
    }
}
