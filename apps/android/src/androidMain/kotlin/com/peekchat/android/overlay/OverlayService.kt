package com.peekchat.android.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.peekchat.android.MainActivity

/**
 * 浮窗常驻服务 — 眯聊的核心入口。
 *
 * 三态交互模型（Iris spec）：
 * 1. 眯着 — 屏幕边缘半透明 pill，不遮挡微信内容
 * 2. 睁眼 — 点击展开，环形采集进度
 * 3. 看一眼 — 采集完成，红点 badge + 迷你摘要
 *
 * Phase 1: 先实现"眯着"态 — 可拖拽的半透明 pill
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var pillView: View
    private var layoutParams: WindowManager.LayoutParams? = null

    // ── Lifecycle ──────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        } catch (e: RuntimeException) {
            // startForeground may fail if notification permission not yet granted.
            // The service will still run; we retry in showPill if needed.
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            showPill()
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        hidePill()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Pill (眯着态) ──────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun showPill() {
        // Guard: if pill is already showing, don't add a second one
        if (::pillView.isInitialized && (pillView.parent != null)) return
        val pill = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                PILL_SIZE_DP.dpToPx(this@OverlayService),
                PILL_SIZE_DP.dpToPx(this@OverlayService)
            )
            setBackgroundResource(android.R.color.transparent)

            // Semi-transparent circle background
            addView(View(this@OverlayService).apply {
                val bgParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams = bgParams
                // Brand700 (#475569) at ~60% alpha — Iris spec for 眯着态
                setBackgroundColor(PILL_COLOR)
            })
        }

        pillView = pill

        layoutParams = WindowManager.LayoutParams(
            PILL_SIZE_DP.dpToPx(this),
            PILL_SIZE_DP.dpToPx(this),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = INITIAL_X_DP.dpToPx(this@OverlayService)
            y = INITIAL_Y_DP.dpToPx(this@OverlayService)
        }

        pillView.setOnTouchListener(PillTouchListener())
        windowManager.addView(pillView, layoutParams)
    }

    private fun hidePill() {
        try {
            windowManager.removeView(pillView)
        } catch (_: IllegalArgumentException) {
            // View not attached
        }
    }

    // ── Touch handling (drag to reposition) ────────────────────────

    private inner class PillTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (kotlin.math.abs(dx) > TOUCH_SLOP_DP.dpToPx(this@OverlayService) ||
                        kotlin.math.abs(dy) > TOUCH_SLOP_DP.dpToPx(this@OverlayService)
                    ) {
                        isDragging = true
                    }
                    if (isDragging) {
                        layoutParams?.x = (initialX + dx.toInt())
                        layoutParams?.y = (initialY + dy.toInt())
                        layoutParams?.let { lp ->
                            windowManager.updateViewLayout(pillView, lp)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tap → start capture flow
                        onPillTapped()
                    } else {
                        // Snap to nearest edge
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onPillTapped() {
        // Launch MainActivity with capture action.
        // MediaProjection permission must be requested from an Activity,
        // so we delegate to MainActivity which handles the result flow.
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_START_CAPTURE
        }
        startActivity(intent)
    }

    private fun snapToEdge() {
        val lp = layoutParams ?: return
        val screenWidth = windowManager.currentWindowMetrics.bounds.width()
        val pillCenter = lp.x + PILL_SIZE_DP.dpToPx(this) / 2

        val targetX = if (pillCenter < screenWidth / 2) {
            0 // snap to left edge
        } else {
            screenWidth - PILL_SIZE_DP.dpToPx(this) // snap to right edge
        }

        ValueAnimator.ofInt(lp.x, targetX).apply {
            duration = 200
            addUpdateListener { animator ->
                lp.x = animator.animatedValue as Int
                windowManager.updateViewLayout(pillView, lp)
            }
            start()
        }
    }

    // ── Foreground service notification ────────────────────────────

    private fun createNotification(): Notification {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "眯聊浮窗",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持浮窗常驻"
                setShowBadge(false)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("眯聊")
            .setContentText("浮窗运行中")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    // ── Utility ────────────────────────────────────────────────────

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val CHANNEL_ID = "peekchat_overlay"
        private const val NOTIFICATION_ID = 1001

        // Track whether the service is currently running (avoid duplicate pills)
        @Volatile
        var isRunning: Boolean = false
            private set

        // Intent action: pill tapped → start capture flow
        const val ACTION_START_CAPTURE = "com.peekchat.android.START_CAPTURE"

        // Iris design spec: 44dp semi-transparent pill
        private const val PILL_SIZE_DP = 44
        private const val INITIAL_X_DP = 300
        private const val INITIAL_Y_DP = 200
        private const val TOUCH_SLOP_DP = 8

        // Brand700 (#475569) at 60% alpha → 0x99475569
        private val PILL_COLOR = 0x99475569.toInt()
    }
}
