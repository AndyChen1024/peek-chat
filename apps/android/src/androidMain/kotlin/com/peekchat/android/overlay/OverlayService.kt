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
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.peekchat.android.MainActivity

/**
 * 浮窗常驻服务 — 眯聊的核心入口。
 *
 * 两步触发交互（Iris spec）：
 * 1. 眯着 — 屏幕边缘半透明 pill，点击后展开
 * 2. 睁眼 — 展开小面板，含「开始采集」按钮
 * 3. 点「开始采集」→ 请求 MediaProjection 权限 → 采集
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var pillView: View? = null
    private var capturePanel: View? = null
    private var pillLayoutParams: WindowManager.LayoutParams? = null
    private var panelLayoutParams: WindowManager.LayoutParams? = null

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
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        } catch (e: RuntimeException) {
            // Notification permission may not be granted yet; service still runs.
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
        hideCapturePanel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Pill (眯着态) ──────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun showPill() {
        if (pillView != null && pillView?.parent != null) return

        val pill = View(this).apply {
            val drawable = android.graphics.drawable.ShapeDrawable(
                android.graphics.drawable.shapes.OvalShape()
            ).apply {
                paint.color = PILL_COLOR
                intrinsicWidth = PILL_SIZE_DP.dpToPx(this@OverlayService)
                intrinsicHeight = PILL_SIZE_DP.dpToPx(this@OverlayService)
            }
            background = drawable
        }

        pillView = pill

        pillLayoutParams = WindowManager.LayoutParams(
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

        pill.setOnTouchListener(PillTouchListener())
        windowManager.addView(pill, pillLayoutParams)
    }

    private fun hidePill() {
        pillView?.let { v ->
            try { windowManager.removeView(v) } catch (_: IllegalArgumentException) {}
        }
        pillView = null
    }

    // ── Capture panel (睁眼态 → 开始采集) ──────────────────────────

    private fun showCapturePanel() {
        if (capturePanel != null && capturePanel?.parent != null) return

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                PANEL_PADDING_DP.dpToPx(this@OverlayService),
                PANEL_PADDING_DP.dpToPx(this@OverlayService),
                PANEL_PADDING_DP.dpToPx(this@OverlayService),
                PANEL_PADDING_DP.dpToPx(this@OverlayService)
            )
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16.dpToPx(this@OverlayService).toFloat()
            }

            addView(TextView(this@OverlayService).apply {
                text = "眯聊"
                textSize = 14f
                setTextColor(Color.parseColor("#475569"))
            })

            addView(Button(this@OverlayService).apply {
                text = "开始采集"
                setBackgroundColor(Color.parseColor("#475569"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8.dpToPx(this@OverlayService) }
                layoutParams = lp
                setOnClickListener { onStartCapture() }
            })
        }

        capturePanel = panel

        panelLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(panel, panelLayoutParams)
    }

    private fun hideCapturePanel() {
        capturePanel?.let { v ->
            try { windowManager.removeView(v) } catch (_: IllegalArgumentException) {}
        }
        capturePanel = null
    }

    private fun onStartCapture() {
        hideCapturePanel()
        android.util.Log.i("OverlayService", "开始采集 → starting capture flow")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_START_CAPTURE
        }
        startActivity(intent)
    }

    // ── Touch handling (drag pill, tap to expand) ─────────────────

    private inner class PillTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = pillLayoutParams?.x ?: 0
                    initialY = pillLayoutParams?.y ?: 0
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
                        pillLayoutParams?.x = (initialX + dx.toInt())
                        pillLayoutParams?.y = (initialY + dy.toInt())
                        pillLayoutParams?.let { lp ->
                            windowManager.updateViewLayout(pillView, lp)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tap → expand capture panel (do NOT fire MediaProjection yet)
                        showCapturePanel()
                    } else {
                        snapToEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge() {
        val lp = pillLayoutParams ?: return
        val screenWidth = windowManager.currentWindowMetrics.bounds.width()
        val pillCenter = lp.x + PILL_SIZE_DP.dpToPx(this) / 2

        val targetX = if (pillCenter < screenWidth / 2) 0 else screenWidth - PILL_SIZE_DP.dpToPx(this)

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

        @Volatile
        var isRunning: Boolean = false
            private set

        const val ACTION_START_CAPTURE = "com.peekchat.android.START_CAPTURE"

        private const val PILL_SIZE_DP = 44
        private const val INITIAL_X_DP = 300
        private const val INITIAL_Y_DP = 200
        private const val TOUCH_SLOP_DP = 8
        private const val PANEL_PADDING_DP = 16

        private val PILL_COLOR = 0x99475569.toInt()
    }
}
