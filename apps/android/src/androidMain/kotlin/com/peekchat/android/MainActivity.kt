package com.peekchat.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.peekchat.android.capture.ScreenshotCapture
import com.peekchat.android.overlay.OverlayPermissionHelper
import com.peekchat.android.overlay.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var screenshotCapture: ScreenshotCapture
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
                        // TODO(Phase 1): Trigger OCR on captured image
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
}
