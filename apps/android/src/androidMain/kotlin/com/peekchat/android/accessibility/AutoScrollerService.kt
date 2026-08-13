package com.peekchat.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.peekchat.android.PeekLog

/**
 * 自动滚动采集服务（AccessibilityService）。
 *
 * 用 dispatchGesture 模拟上滑来滚动微信聊天列表，配合截图实现多屏连续采集。
 * 关键（Atlas 调研）：用 dispatchGesture 而不是 ACTION_SCROLL_FORWARD，
 * 因为微信列表是自定义渲染，滚动 action 经常不生效；手势注入无法被区分。
 *
 * 滑动参数：起点屏幕中下 → 终点中上（屏高 60-70%），300ms 时长，间隔 400ms。
 */
class AutoScrollerService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!isScrolling) return
            performScroll {
                mainHandler.postDelayed(this, SCROLL_INTERVAL_MS)
            }
        }
    }

    private var isScrolling = false

    private val displayMetrics = DisplayMetrics().also {
        (getSystemService(WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getRealMetrics(it)
    }

    // ── Lifecycle ──────────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        PeekLog.log("AutoScroller", "AccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't read window content; only dispatch gestures on command.
    }

    override fun onInterrupt() {
        stopScrolling()
    }

    override fun onDestroy() {
        stopScrolling()
        super.onDestroy()
    }

    // ── Scroll control (called via service commands) ───────────────

    private fun startScrolling() {
        if (isScrolling) return
        isScrolling = true
        PeekLog.log("AutoScroller", "start auto-scroll")
        mainHandler.post(scrollRunnable)
    }

    private fun stopScrolling() {
        if (!isScrolling) return
        isScrolling = false
        PeekLog.log("AutoScroller", "stop auto-scroll")
        mainHandler.removeCallbacks(scrollRunnable)
    }

    private fun performScroll(callback: () -> Unit) {
        val path = Path().apply {
            moveTo(
                displayMetrics.widthPixels / 2f,
                displayMetrics.heightPixels * 0.75f
            )
            lineTo(
                displayMetrics.widthPixels / 2f,
                displayMetrics.heightPixels * 0.25f
            )
        }
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0, SCROLL_DURATION_MS)
            )
            .build()

        dispatchGesture(gesture, null, null)
        // dispatchGesture 是异步的；回调在有值时传，这里简单 sleep 后让上层截图，
        // 由外部采集循环配合间隔调度。
        mainHandler.postDelayed(callback, SCROLL_INTERVAL_MS)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_AUTO_SCROLL -> startScrolling()
            ACTION_STOP_SCROLL -> stopScrolling()
        }
        return START_STICKY
    }

    companion object {
        const val ACTION_AUTO_SCROLL = "com.peekchat.android.AUTO_SCROLL"
        const val ACTION_STOP_SCROLL = "com.peekchat.android.STOP_SCROLL"

        private const val SCROLL_DURATION_MS = 300L  // gesture 时长
        private const val SCROLL_INTERVAL_MS = 400L // 每次滚动间隔
    }
}
