package com.modernclockapp

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var alarmTimeText: TextView
    private lateinit var setAlarmButton: Button
    private lateinit var clearAlarmButton: Button
    private lateinit var timePicker: TimePicker
    private val handler = Handler(Looper.getMainLooper())
    private var timeUpdateRunnable: Runnable? = null
    private var alarmCalendar: Calendar? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create scrollable layout
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        
        // Title
        val title = TextView(this).apply {
            text = "Modern Clock App"
            textSize = 28f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 30)
        }
        
        // Time display
        timeDisplay = TextView(this).apply {
            textSize = 42f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        
        // Date display
        dateDisplay = TextView(this).apply {
            textSize = 20f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 10, 0, 30)
        }
        
        // Alarm section
        val alarmTitle = TextView(this).apply {
            text = "⏰ Set Alarm"
            textSize = 22f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 20)
        }
        
        // Time picker
        timePicker = TimePicker(this).apply {
            setIs24HourView(true)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Buttons container
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Set alarm button
        setAlarmButton = Button(this).apply {
            text = "Set Alarm"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 10, 0)
            }
        }
        
        // Clear alarm button
        clearAlarmButton = Button(this).apply {
            text = "Clear Alarm"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(10, 0, 0, 0)
            }
        }
        
        buttonContainer.addView(setAlarmButton)
        buttonContainer.addView(clearAlarmButton)
        
        // Alarm status
        alarmTimeText = TextView(this).apply {
            text = "No alarm set"
            textSize = 16f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 0)
        }
        
        // Add all views
        layout.addView(title)
        layout.addView(timeDisplay)
        layout.addView(dateDisplay)
        layout.addView(alarmTitle)
        layout.addView(timePicker)
        layout.addView(buttonContainer)
        layout.addView(alarmTimeText)
        
        scrollView.addView(layout)
        setContentView(scrollView)
        
        // Button listeners
        setAlarmButton.setOnClickListener {
            setAlarm()
        }
        
        clearAlarmButton.setOnClickListener {
            clearAlarm()
        }
        
        // Start time updates
        startTimeUpdate()
    }
    
    private fun startTimeUpdate() {
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timeUpdateRunnable!!)
    }
    
    private fun updateTime() {
        val now = Calendar.getInstance()
        
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeDisplay.text = timeFormat.format(now.time)
        
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateDisplay.text = dateFormat.format(now.time)
        
        // Check if alarm should trigger
        alarmCalendar?.let { alarm ->
            if (now.timeInMillis >= alarm.timeInMillis) {
                triggerAlarm()
                clearAlarm()
            }
        }
    }
    
    private fun setAlarm() {
        try {
            val hour = timePicker.hour
            val minute = timePicker.minute
            
            alarmCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If time has passed today, set for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val alarmTimeStr = timeFormat.format(alarmCalendar!!.time)
            
            alarmTimeText.text = "⏰ Alarm set for: $alarmTimeStr"
            
            Toast.makeText(this, "Alarm set for $alarmTimeStr", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting alarm: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun clearAlarm() {
        alarmCalendar = null
        alarmTimeText.text = "No alarm set"
        Toast.makeText(this, "Alarm cleared", Toast.LENGTH_SHORT).show()
    }
    
    private fun triggerAlarm() {
        // Simple alarm notification using Toast and vibration
        Toast.makeText(this, "⏰ ALARM! Time to wake up!", Toast.LENGTH_LONG).show()
        
        // You can add sound/vibration here if needed
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(2000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(2000)
            }
        } catch (e: Exception) {
            // Vibration not available
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timeUpdateRunnable?.let { handler.removeCallbacks(it) }
    }
}
