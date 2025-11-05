package com.altrise.clockapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.altrise.clockapp.alarm.AlarmDismissActivity
import com.altrise.clockapp.alarm.AlarmScheduler
import com.altrise.clockapp.models.Alarm
import com.altrise.clockapp.storage.AlarmStorage
import java.text.SimpleDateFormat
import java.util.*

class AlarmOverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var alarmId: Int = -1
    private val handler = Handler(Looper.getMainLooper())
    private var endTimeCheckRunnable: Runnable? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        
        if (canDrawOverlays()) {
            showOverlay()
            startEndTimeCheck()
        } else {
            // If no permission, open dismiss activity directly
            openDismissActivity()
            stopSelf()
        }
        
        return START_NOT_STICKY
    }
    
    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    private fun showOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Create overlay layout
        overlayView = createOverlayView()
        
        // Set window parameters for fullscreen immersive overlay
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        }
        
        params.gravity = Gravity.CENTER
        
        // Hide status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Add view to window
        windowManager?.addView(overlayView, params)
    }
    
    private fun createOverlayView(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A0A")) // AltRise black
            gravity = Gravity.CENTER
            setPadding(80, 0, 80, 0)
        }
        
        // Alarm icon/emoji - larger and centered
        val alarmIcon = TextView(this).apply {
            text = "⏰"
            textSize = 120f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        
        // Time display
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val timeText = TextView(this).apply {
            text = currentTime
            textSize = 82f
            setTextColor(Color.parseColor("#31A82A")) // AltRise green
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setShadowLayer(45f, 0f, 0f, Color.parseColor("#31A82A"))
            letterSpacing = 0.15f
            setPadding(0, 20, 0, 30)
        }
        
        // Title
        val title = TextView(this).apply {
            text = "⚡ ALARM RINGING!"
            textSize = 28f
            setTextColor(Color.parseColor("#E87316")) // AltRise orange
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setShadowLayer(20f, 0f, 0f, Color.parseColor("#E87316"))
            letterSpacing = 0.08f
            setPadding(0, 20, 0, 15)
        }
        
        // Subtitle
        val subtitle = TextView(this).apply {
            text = "Tap below to solve puzzle"
            textSize = 16f
            setTextColor(Color.parseColor("#999999"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }
        
        // Solve Puzzle button - modern design
        val solvePuzzleButton = Button(this).apply {
            text = "SOLVE PUZZLE"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = android.graphics.drawable.GradientDrawable().apply {
                colors = intArrayOf(
                    Color.parseColor("#E87316"),  // AltRise orange
                    Color.parseColor("#FF9233")   // Lighter orange gradient
                )
                cornerRadius = 16f
                setStroke(3, Color.parseColor("#FFFFFF"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                180
            ).apply {
                setMargins(0, 0, 0, 0)
            }
            elevation = 16f
            elevation = 16f
            setPadding(60, 0, 60, 0)
            
            setOnClickListener {
                // Add haptic feedback
                val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
                
                openDismissActivity()
                removeOverlay()
            }
        }
        
        container.addView(alarmIcon)
        container.addView(timeText)
        container.addView(title)
        container.addView(subtitle)
        container.addView(solvePuzzleButton)
        
        return container
    }
    
    private fun openDismissActivity() {
        val intent = Intent(this, AlarmDismissActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
    
    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        stopEndTimeCheck()
        stopSelf()
    }
    
    private fun startEndTimeCheck() {
        // Check every second if end time is reached
        endTimeCheckRunnable = object : Runnable {
            override fun run() {
                if (shouldAutoDismiss()) {
                    removeOverlay()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(endTimeCheckRunnable!!)
    }
    
    private fun stopEndTimeCheck() {
        endTimeCheckRunnable?.let {
            handler.removeCallbacks(it)
            endTimeCheckRunnable = null
        }
    }
    
    private fun shouldAutoDismiss(): Boolean {
        if (alarmId == -1) return false
        
        val storage = AlarmStorage.getInstance(this)
        val alarm = storage.getAlarm(alarmId) ?: return false
        
        // Check if alarm has end time set
        if (!alarm.hasEndTime) return false
        
        val now = Calendar.getInstance()
        val endTimeMillis = alarm.getEndTimeMillis() ?: return false
        
        // Auto-dismiss if current time >= end time
        return now.timeInMillis >= endTimeMillis
    }
    
    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
