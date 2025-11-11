package com.altrise.clockapp.alarm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import com.altrise.clockapp.R
import com.altrise.clockapp.storage.AlarmStorage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen activity that shows when alarm triggers.
 * Displays math puzzle that must be solved to dismiss the alarm.
 * Works even from locked screen.
 */
class AlarmDismissActivity : Activity() {
    
    private var alarmId: Int = -1
    private var currentMathAnswer: Int = 0
    private var mathPuzzleSolved: Boolean = false
    
    private val autoStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "BroadcastReceiver.onReceive called with action: ${intent?.action}")
            
            if (intent?.action == AlarmNotificationService.ACTION_AUTO_STOP) {
                Log.d(TAG, "Auto-stop broadcast received - auto-dismissing alarm $alarmId")
                
                // Delete the alarm from storage (auto-stopped alarm)
                val storage = AlarmStorage.getInstance(this@AlarmDismissActivity)
                storage.deleteAlarm(alarmId)
                Log.d(TAG, "Alarm $alarmId deleted from storage")
                
                Toast.makeText(this@AlarmDismissActivity, "⏱️ Alarm auto-stopped (end time reached)", Toast.LENGTH_SHORT).show()
                
                // Close the activity
                Log.d(TAG, "Closing AlarmDismissActivity")
                finish()
            }
        }
    }
    
    companion object {
        private const val TAG = "AlarmDismissActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "AlarmDismissActivity.onCreate called")
        
        // Register broadcast receiver for auto-stop
        val filter = IntentFilter(AlarmNotificationService.ACTION_AUTO_STOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(autoStopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(autoStopReceiver, filter)
        }
        Log.d(TAG, "Auto-stop broadcast receiver registered for action: ${AlarmNotificationService.ACTION_AUTO_STOP}")
        
        // Show activity on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        // Get alarm ID
        alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        
        if (alarmId == -1) {
            Log.e(TAG, "Invalid alarm ID")
            finish()
            return
        }
        
        // Get alarm details
        val storage = AlarmStorage.getInstance(this)
        val alarm = storage.getAlarm(alarmId)
        
        if (alarm == null) {
            Log.e(TAG, "Alarm $alarmId not found")
            finish()
            return
        }
        
        // Build UI
        setupUI(alarm)
    }
    
    private fun setupUI(alarm: com.altrise.clockapp.models.Alarm) {
        // Modern morning-friendly gradient background with deeper colors for better contrast
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
                colors = intArrayOf(
                    Color.parseColor("#F5D5A0"), // Deeper peachy beige
                    Color.parseColor("#F0C090"), // Richer warm apricot
                    Color.parseColor("#F5DA90")  // Stronger gentle yellow
                )
            }
            setPadding(60, 100, 60, 100)
            gravity = Gravity.CENTER
        }
        
        // Time display with elegant morning styling
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hourOfDay)
            set(Calendar.MINUTE, alarm.minute)
        }
        
        val timeText = TextView(this).apply {
            text = timeFormat.format(alarmTime.time)
            textSize = 80f
            setTextColor(Color.parseColor("#1A1F3A")) // Very dark for maximum readability
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
            setShadowLayer(50f, 0f, 0f, Color.parseColor("#405B8FDB")) // Richer blue glow
        }
        
        val title = TextView(this).apply {
            text = "⏰ Wake Up!"
            textSize = 26f
            setTextColor(Color.parseColor("#E67E22")) // Stronger warm orange
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setPadding(0, 48, 0, 12)
        }
        
        val subtitle = TextView(this).apply {
            text = "Solve the puzzle to dismiss"
            textSize = 16f
            setTextColor(Color.parseColor("#5A6580")) // Darker gray for readability
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 52)
        }
        
        // Generate math puzzle
        val (puzzleTextStr, answer) = generateMathPuzzle()
        currentMathAnswer = answer
        
        // Modern floating glassmorphic puzzle card with improved contrast
        val puzzleCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 28f * resources.displayMetrics.density
                setColor(Color.parseColor("#75FFFFFF")) // More opaque for better readability
                setStroke((2f * resources.displayMetrics.density).toInt(), Color.parseColor("#AAFFFFFF")) // Thicker border
            }
            elevation = 12f * resources.displayMetrics.density
            setPadding(40, 44, 40, 44)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }
        
        val puzzleText = TextView(this).apply {
            text = puzzleTextStr
            textSize = 56f
            setTextColor(Color.parseColor("#1A1F3A")) // Very dark for maximum readability
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.15f
            setPadding(20, 20, 20, 28)
        }
        
        val input = EditText(this).apply {
            hint = "Your answer"
            textSize = 28f
            setTextColor(Color.parseColor("#1A1F3A")) // Dark text for readability
            setHintTextColor(Color.parseColor("#8890A0")) // Darker hint
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            gravity = Gravity.CENTER
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 20f * resources.displayMetrics.density
                setColor(Color.parseColor("#EEF2F6")) // Slightly darker background
                setStroke((1.5f * resources.displayMetrics.density).toInt(), Color.parseColor("#C0C5CE")) // Darker border
            }
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        puzzleCard.addView(puzzleText)
        puzzleCard.addView(input)
        
        val feedback = TextView(this).apply {
            textSize = 15f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(20, 24, 20, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                100
            )
        }
        
        val checkBtn = Button(this).apply {
            text = "Check Answer"
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.04f
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 28f * resources.displayMetrics.density
                colors = intArrayOf(Color.parseColor("#5B8FDB"), Color.parseColor("#7BA8E8")) // Richer blue
            }
            elevation = 6f * resources.displayMetrics.density
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (64 * resources.displayMetrics.density).toInt()
            ).apply {
                setMargins(0, 16, 0, 12)
            }
        }
        
        val dismissBtn = Button(this).apply {
            text = "Dismiss Alarm"
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.04f
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 28f * resources.displayMetrics.density
                colors = intArrayOf(Color.parseColor("#10B981"), Color.parseColor("#34D399"))
            }
            elevation = 6f * resources.displayMetrics.density
            setPadding(0, 0, 0, 0)
            isEnabled = false
            alpha = 0.4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (64 * resources.displayMetrics.density).toInt()
            )
        }
        
        checkBtn.setOnClickListener {
            val userAnswer = input.text.toString().toIntOrNull()
            
            if (userAnswer == currentMathAnswer) {
                mathPuzzleSolved = true
                feedback.text = "✓ Correct! You can now dismiss the alarm"
                feedback.setTextColor(Color.parseColor("#10B981"))  // Success green
                input.isEnabled = false
                input.alpha = 0.6f
                checkBtn.isEnabled = false
                checkBtn.alpha = 0.4f
                dismissBtn.isEnabled = true
                dismissBtn.alpha = 1.0f
                
                // Success pulse animation
                dismissBtn.animate()
                    .scaleX(1.05f).scaleY(1.05f).setDuration(200)
                    .withEndAction {
                        dismissBtn.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    }.start()
                
                // Add vibration feedback
                val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            } else {
                feedback.text = "✗ Incorrect - Try again"
                feedback.setTextColor(Color.parseColor("#EF4444"))  // Error red
                input.text.clear()
                input.requestFocus()
                
                // Shake animation for wrong answer
                puzzleCard.animate()
                    .translationX(-20f).setDuration(50)
                    .withEndAction {
                        puzzleCard.animate().translationX(20f).setDuration(50)
                            .withEndAction {
                                puzzleCard.animate().translationX(-15f).setDuration(50)
                                    .withEndAction {
                                        puzzleCard.animate().translationX(15f).setDuration(50)
                                            .withEndAction {
                                                puzzleCard.animate().translationX(0f).setDuration(50).start()
                                            }.start()
                                    }.start()
                            }.start()
                    }.start()
                
                // Subtle vibration for wrong answer
                val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, 100))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        }
        
        dismissBtn.setOnClickListener {
            if (mathPuzzleSolved) {
                dismissAlarm()
            }
        }
        
        // Smooth button press animations
        checkBtn.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
            false
        }
        
        dismissBtn.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (v.isEnabled) v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    if (v.isEnabled) v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
            false
        }
        
        container.addView(timeText)
        container.addView(title)
        container.addView(subtitle)
        container.addView(puzzleCard)
        container.addView(feedback)
        container.addView(checkBtn)
        container.addView(dismissBtn)
        
        setContentView(container)
        
        // Make fullscreen - hide status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }
    
    private fun generateMathPuzzle(): Pair<String, Int> {
        val rand = Random()
        return if (rand.nextBoolean()) {
            // Addition: a + b <= 50
            val a = rand.nextInt(11)
            val bMax = 50 - a
            val b = if (bMax > 0) rand.nextInt(bMax + 1) else 0
            Pair("$a + $b = ?", a + b)
        } else {
            // Subtraction: a - b >= 0
            val a = rand.nextInt(11)
            val b = if (a > 0) rand.nextInt(a + 1) else 0
            Pair("$a - $b = ?", a - b)
        }
    }
    
    private fun dismissAlarm() {
        Log.d(TAG, "Dismissing alarm $alarmId")
        
        // Stop the alarm service
        val stopIntent = Intent(this, AlarmNotificationService::class.java).apply {
            action = AlarmNotificationService.ACTION_STOP_SERVICE
        }
        stopService(stopIntent)
        
        // Delete the alarm from storage (one-time alarm)
        val storage = AlarmStorage.getInstance(this)
        storage.deleteAlarm(alarmId)
        
        Toast.makeText(this, "✅ Alarm dismissed", Toast.LENGTH_SHORT).show()
        
        finish()
    }
    
    override fun onBackPressed() {
        // Prevent dismissing with back button - must solve puzzle
        Toast.makeText(this, "Solve the puzzle to dismiss!", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(autoStopReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
}
