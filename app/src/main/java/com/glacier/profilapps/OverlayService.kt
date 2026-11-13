package com.glacier.profilapps

import android.animation.ValueAnimator
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
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlin.math.hypot

object OverlayState {
    var running by mutableStateOf(false)
    var gamePkg by mutableStateOf<String?>(null)
}

class OverlayService : Service() {
    private val owner = OverlayOwner()
    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var trash: View? = null
    private var panel: View? = null
    private var near = false
    private lateinit var bubbleLp: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Sh.pkg = packageName
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        owner.start()
        goForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        intent?.getStringExtra(EXTRA_GAME)?.let { OverlayState.gamePkg = it }
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (bubble == null) addBubble()
        OverlayState.running = true
        return START_STICKY
    }

    override fun onDestroy() {
        OverlayState.running = false
        try { bubble?.let { wm.removeView(it) } } catch (e: Exception) { }
        try { trash?.let { wm.removeView(it) } } catch (e: Exception) { }
        try { panel?.let { wm.removeView(it) } } catch (e: Exception) { }
        bubble = null; trash = null; panel = null
        owner.stop()
        super.onDestroy()
    }

    // ---------------- util ----------------
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun screen(): Pair<Int, Int> {
        val m = DisplayMetrics()
        @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(m)
        return m.widthPixels to m.heightPixels
    }

    private fun overlayType() =
        if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    private fun goForeground() {
        val ch = "glacier_overlay"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(ch, "Game Panel", NotificationManager.IMPORTANCE_MIN))
        }
        val stop = PendingIntent.getService(
            this, 0, Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n: Notification = NotificationCompat.Builder(this, ch)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Glacier Game Panel")
            .setContentText("Bubble aktif. Tap bubble untuk membuka panel.")
            .setOngoing(true)
            .addAction(0, "Hentikan", stop)
            .build()
        ServiceCompat.startForeground(
            this, 1, n,
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        )
    }

    // ---------------- bubble ----------------
    private fun addBubble() {
        val size = dp(56)
        val img = ImageView(this).apply {
            setImageResource(R.drawable.logo)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF0B1B3A.toInt())
                setStroke(dp(2), 0xFF7DD3FC.toInt())
            }
            clipToOutline = true
            outlineProvider = ViewOutlineProvider.BACKGROUND
            elevation = dp(8).toFloat()
        }
        val box = FrameLayout(this).apply {
            setPadding(dp(8), dp(8), dp(8), dp(8))
            addView(img, FrameLayout.LayoutParams(size, size))
        }
        bubbleLp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = screen().second / 3 }

        val slop = ViewConfiguration.get(this).scaledTouchSlop
        box.setOnTouchListener(object : View.OnTouchListener {
            var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        sx = e.rawX; sy = e.rawY; ox = bubbleLp.x; oy = bubbleLp.y; moved = false
                        img.animate().scaleX(0.88f).scaleY(0.88f).setDuration(120).start()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.rawX - sx; val dy = e.rawY - sy
                        if (!moved && hypot(dx, dy) > slop) { moved = true; showTrash() }
                        if (moved) {
                            bubbleLp.x = (ox + dx).toInt(); bubbleLp.y = (oy + dy).toInt()
                            try { wm.updateViewLayout(v, bubbleLp) } catch (ex: Exception) { }
                            checkNear(v)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        img.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
                        if (moved) {
                            val drop = near
                            hideTrash()
                            if (drop) stopSelf() else snapToEdge(v, size + dp(16))
                        } else if (e.action == MotionEvent.ACTION_UP) openPanel()
                    }
                }
                return true
            }
        })
        wm.addView(box, bubbleLp)
        bubble = box
    }

    private fun snapToEdge(v: View, w: Int) {
        val (sw, sh) = screen()
        val target = if (bubbleLp.x + w / 2 < sw / 2) 0 else sw - w
        bubbleLp.y = bubbleLp.y.coerceIn(0, sh - w)
        ValueAnimator.ofInt(bubbleLp.x, target).apply {
            duration = 220
            addUpdateListener {
                bubbleLp.x = it.animatedValue as Int
                try { wm.updateViewLayout(v, bubbleLp) } catch (ex: Exception) { }
            }
            start()
        }
    }

    // ---------------- trash ----------------
    private fun trashCenter(): Pair<Float, Float> {
        val (sw, sh) = screen()
        return sw / 2f to (sh - dp(56) - dp(36)).toFloat()
    }

    private fun checkNear(bubbleView: View) {
        val (cx, cy) = trashCenter()
        val bx = bubbleLp.x + bubbleView.width / 2f
        val by = bubbleLp.y + bubbleView.height / 2f
        val n = hypot(bx - cx, by - cy) < dp(90)
        if (n != near) {
            near = n
            trash?.let { t ->
                t.animate().scaleX(if (n) 1.3f else 1f).scaleY(if (n) 1.3f else 1f).setDuration(150).start()
                (t.background as? GradientDrawable)?.setColor(if (n) 0xF0E11D48.toInt() else 0xE60B1B3A.toInt())
            }
        }
    }

    private fun showTrash() {
        if (trash != null) return
        val size = dp(72)
        val t = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xE60B1B3A.toInt())
                setStroke(dp(2), 0x99BAE6FD.toInt())
            }
            elevation = dp(8).toFloat()
            val ic = ImageView(context).apply { setImageResource(R.drawable.ic_trash) }
            addView(ic, FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER))
            alpha = 0f; scaleX = 0.6f; scaleY = 0.6f
        }
        val lp = WindowManager.LayoutParams(
            size, size, overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = dp(56) }
        wm.addView(t, lp)
        t.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).start()
        trash = t
    }

    private fun hideTrash() {
        val t = trash ?: return
        trash = null; near = false
        t.animate().alpha(0f).scaleX(0.6f).scaleY(0.6f).setDuration(160).withEndAction {
            try { wm.removeView(t) } catch (e: Exception) { }
        }.start()
    }

    // ---------------- panel ----------------
    private fun openPanel() {
        if (panel != null) return
        bubble?.visibility = View.GONE
        val v = ComposeView(this).apply {
            owner.attachTo(this)
            setContent {
                GlacierTheme {
                    GamePanel(this@OverlayService, onClosed = { closePanel() }, openApp = { openApp() })
                }
            }
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        wm.addView(v, lp)
        panel = v
    }

    private fun closePanel() {
        panel?.let { try { wm.removeView(it) } catch (e: Exception) { } }
        panel = null
        bubble?.visibility = View.VISIBLE
    }

    private fun openApp() {
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
        closePanel()
    }

    companion object {
        const val EXTRA_GAME = "game"
        const val ACTION_STOP = "glacier.overlay.stop"

        fun start(ctx: Context, game: String?) {
            val i = Intent(ctx, OverlayService::class.java)
            if (game != null) i.putExtra(EXTRA_GAME, game)
            ContextCompat.startForegroundService(ctx, i)
        }

        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, OverlayService::class.java)) }
    }
}
