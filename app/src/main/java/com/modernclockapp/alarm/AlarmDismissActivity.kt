package com.modernclockapp.alarm

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
import com.modernclockapp.R
import com.modernclockapp.storage.AlarmStorage
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
    
    private fun setupUI(alarm: com.modernclockapp.models.Alarm) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0E27"))
            setPadding(60, 80, 60, 80)
            gravity = Gravity.CENTER
        }
        
        // Time display
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hourOfDay)
            set(Calendar.MINUTE, alarm.minute)
        }
        
        val timeText = TextView(this).apply {
            text = timeFormat.format(alarmTime.time)
            textSize = 64f
            setTextColor(Color.parseColor("#00D9FF"))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setShadowLayer(30f, 0f, 0f, Color.parseColor("#00D9FF"))
        }
        
        val title = TextView(this).apply {
            text = "⚡ ALARM ACTIVE!"
            textSize = 28f
            setTextColor(Color.parseColor("#00D9FF"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 20)
        }
        
        val subtitle = TextView(this).apply {
            text = "Solve the puzzle to dismiss"
            textSize = 18f
            setTextColor(Color.parseColor("#B0B8D4"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        
        // Generate math puzzle
        val (puzzleTextStr, answer) = generateMathPuzzle()
        currentMathAnswer = answer
        
        val puzzleText = TextView(this).apply {
            text = puzzleTextStr
            textSize = 36f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }
        
        val input = EditText(this).apply {
            hint = "Enter answer"
            textSize = 28f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#6C5CE7"))
            gravity = Gravity.CENTER
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            setBackgroundColor(Color.parseColor("#1A1E3C"))
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 20)
            }
        }
        
        val feedback = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
        }
        
        val checkBtn = Button(this).apply {
            text = "CHECK ANSWER"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#6C5CE7"))
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
            ).apply {
                setMargins(0, 10, 0, 10)
            }
        }
        
        val dismissBtn = Button(this).apply {
            text = "DISMISS ALARM"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00AA66"))
            typeface = Typeface.DEFAULT_BOLD
            isEnabled = false
            alpha = 0.5f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
            )
        }
        
        checkBtn.setOnClickListener {
            val userAnswer = input.text.toString().toIntOrNull()
            
            if (userAnswer == currentMathAnswer) {
                mathPuzzleSolved = true
                feedback.text = "✅ Correct! You can now dismiss the alarm."
                feedback.setTextColor(Color.parseColor("#00FF88"))
                input.isEnabled = false
                checkBtn.isEnabled = false
                dismissBtn.isEnabled = true
                dismissBtn.alpha = 1.0f
            } else {
                feedback.text = "❌ Incorrect. Try again!\nHint: Answer is between 0 and 50"
                feedback.setTextColor(Color.parseColor("#FF6B6B"))
                input.text.clear()
            }
        }
        
        dismissBtn.setOnClickListener {
            if (mathPuzzleSolved) {
                dismissAlarm()
            }
        }
        
        container.addView(timeText)
        container.addView(title)
        container.addView(subtitle)
        container.addView(puzzleText)
        container.addView(input)
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
