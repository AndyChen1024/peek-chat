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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.peekchat.android.MainActivity

/**
 * 浮窗常驻服务 — 眯聊的核心入口。
 *
 * 三态交互（Iris spec）：
 * 1. 眯着 — 屏幕边缘半透明 pill
 * 2. 睁眼·待命 — FloatStandbyPanel「开始采集」面板（不截图）
 * 3. 睁眼·采集中 — FloatCapturingPanel「采集中…」+「停止」（截图进行中）
 *
 * 关键：待命面板不做任何截图，点「开始采集」才触发 MediaProjection 授权 + 截图。
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var pillView: View? = null
    private var panelView: View? = null
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
        when (intent?.action) {
            ACTION_SHOW_CAPTURING -> {
                showCapturingPanel()
            }
            ACTION_SHOW_ANALYZING -> {
                showAnalyzingPanel()
            }
            ACTION_SHOW_ANALYZE_FAILED -> {
                showAnalyzeFailedPanel()
            }
            ACTION_HIDE_PANEL -> {
                hidePanel()
            }
            else -> {
                try {
                    showPill()
                } catch (e: Exception) {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        hidePill()
        hidePanel()
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

    // ── Panels (睁眼态：待命 / 采集中) ──────────────────────────────

    private fun showStandbyPanel() {
        if (panelView != null && panelView?.parent != null) return

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

            // Header: title (采集这段对话) + close ✕
            addView(LinearLayout(this@OverlayService).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(TextView(this@OverlayService).apply {
                    text = "采集这段对话"
                    textSize = 14f
                    setTextColor(Color.parseColor("#475569"))
                })

                addView(TextView(this@OverlayService).apply {
                    text = "✕"
                    textSize = 16f
                    setTextColor(Color.parseColor("#94A3B8"))
                    setPadding(24.dpToPx(this@OverlayService), 0, 0, 0)
                    setOnClickListener { hidePanel() }
                })
            })

            // Description
            addView(TextView(this@OverlayService).apply {
                text = "滑到你想开始的位置，再点开始"
                textSize = 11f
                setTextColor(Color.parseColor("#94A3B8"))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4.dpToPx(this@OverlayService) }
                layoutParams = lp
            })

            // Primary button: 开始采集 (brand-700 filled)
            addView(Button(this@OverlayService).apply {
                text = "开始采集"
                setBackgroundColor(Color.parseColor("#475569"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8.dpToPx(this@OverlayService) }
                layoutParams = lp
                setOnClickListener { onStartCaptureClicked() }
            })

            // Tap anywhere else on panel (not on buttons) dismisses to 眯着.
            setOnClickListener { hidePanel() }
        }

        panelView = panel

        panelLayoutParams = newPanelLayoutParams()
        windowManager.addView(panel, panelLayoutParams)
    }

    private fun showCapturingPanel() {
        hidePanel()

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

            // Circular progress ring (Iris spec: no percentage)
            addView(ProgressBar(this@OverlayService).apply {
                isIndeterminate = true
                val lp = LinearLayout.LayoutParams(
                    48.dpToPx(this@OverlayService),
                    48.dpToPx(this@OverlayService)
                ).apply { gravity = Gravity.CENTER_HORIZONTAL }
                layoutParams = lp
            })

            // Status text
            addView(TextView(this@OverlayService).apply {
                text = "采集中…"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#475569"))
            })

            // Stop button (outlined brand-700 pill)
            addView(Button(this@OverlayService).apply {
                text = "停止"
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.parseColor("#475569"))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8.dpToPx(this@OverlayService) }
                layoutParams = lp
                setOnClickListener { onStopCaptureClicked() }
            })
        }

        panelView = panel

        panelLayoutParams = newPanelLayoutParams()
        windowManager.addView(panel, panelLayoutParams)
    }

    private fun showAnalyzingPanel() {
        hidePanel()

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

            // Circular progress ring
            addView(ProgressBar(this@OverlayService).apply {
                isIndeterminate = true
                val lp = LinearLayout.LayoutParams(
                    48.dpToPx(this@OverlayService),
                    48.dpToPx(this@OverlayService)
                ).apply { gravity = Gravity.CENTER_HORIZONTAL }
                layoutParams = lp
            })

            addView(TextView(this@OverlayService).apply {
                text = "正在分析对话…"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#475569"))
            })
        }

        panelView = panel

        panelLayoutParams = newPanelLayoutParams()
        windowManager.addView(panel, panelLayoutParams)
    }

    private fun showAnalyzeFailedPanel() {
        hidePanel()

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
                text = "分析失败"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#F97316"))
            })

            addView(Button(this@OverlayService).apply {
                text = "重试"
                setBackgroundColor(Color.parseColor("#475569"))
                setTextColor(Color.WHITE)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8.dpToPx(this@OverlayService) }
                layoutParams = lp
                setOnClickListener { onRetryAnalysis() }
            })
        }

        panelView = panel

        panelLayoutParams = newPanelLayoutParams()
        windowManager.addView(panel, panelLayoutParams)
    }

    private fun onRetryAnalysis() {
        // Re-run the capture pipeline (re-trigger start capture from phone state).
        hidePanel()
        android.util.Log.i("OverlayService", "重试 → re-trigger analysis")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_RETRY_ANALYSIS
        }
        startActivity(intent)
    }

    private fun newPanelLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            120.dpToPx(this), // Iris spec: 120dp wide rounded card
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
    }

    private fun hidePanel() {
        panelView?.let { v ->
            try { windowManager.removeView(v) } catch (_: IllegalArgumentException) {}
        }
        panelView = null
    }

    private fun onStartCaptureClicked() {
        // "开始采集" → request MediaProjection permission via MainActivity.
        // The actual screenshot is delayed until permission granted (capturing panel).
        hidePanel()
        android.util.Log.i("OverlayService", "开始采集 → requesting MediaProjection")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_START_CAPTURE
        }
        startActivity(intent)
    }

    private fun onStopCaptureClicked() {
        // "停止" → end scroll capture, run OCR+AI on existing screenshots.
        hidePanel()
        android.util.Log.i("OverlayService", "停止 → finalize capture")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = ACTION_STOP_CAPTURE
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
                        // Tap → expand standby panel (do NOT screenshot yet)
                        showStandbyPanel()
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
        const val ACTION_STOP_CAPTURE = "com.peekchat.android.STOP_CAPTURE"
        const val ACTION_RETRY_ANALYSIS = "com.peekchat.android.RETRY_ANALYSIS"
        const val ACTION_SHOW_CAPTURING = "com.peekchat.android.SHOW_CAPTURING"
        const val ACTION_SHOW_ANALYZING = "com.peekchat.android.SHOW_ANALYZING"
        const val ACTION_SHOW_ANALYZE_FAILED = "com.peekchat.android.SHOW_ANALYZE_FAILED"
        const val ACTION_HIDE_PANEL = "com.peekchat.android.HIDE_PANEL"

        private const val PILL_SIZE_DP = 44
        private const val INITIAL_X_DP = 300
        private const val INITIAL_Y_DP = 200
        private const val TOUCH_SLOP_DP = 8
        private const val PANEL_PADDING_DP = 16

        private val PILL_COLOR = 0x99475569.toInt()
    }
}
