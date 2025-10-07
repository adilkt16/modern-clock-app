package com.modernclockapp

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
    private var mediaPlayer: MediaPlayer? = null
    private var alarmDialog: AlertDialog? = null
    private var isAlarmRinging = false
    private val notificationId = 1001
    private val channelId = "ALARM_CHANNEL"
    private val requestPostNotif = 2001
    // Puzzle gating state
    private var mathPuzzleSolved: Boolean = false
    private var currentMathAnswer: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Main container with gradient background
        val mainLayout = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_futuristic_gradient)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Scrollable content
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 60, 32, 60)
        }
        
        // Futuristic Title with neon glow
        val title = TextView(this).apply {
            text = "⚡ CYBER CLOCK"
            textSize = 36f
            setTextColor(Color.parseColor("#00D9FF"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
            setShadowLayer(20f, 0f, 0f, Color.parseColor("#00D9FF"))
        }
        
        // Glass card for clock display
        val clockCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_glass_card)
            setPadding(40, 50, 40, 50)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 30)
            }
        }
        
        // Time display with neon effect
        timeDisplay = TextView(this).apply {
            textSize = 56f
            setTextColor(Color.parseColor("#FFFFFF"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(30f, 0f, 0f, Color.parseColor("#6C5CE7"))
        }
        
        // Date display
        dateDisplay = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.parseColor("#B0B8D4"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 0)
            letterSpacing = 0.1f
        }
        
        clockCard.addView(timeDisplay)
        clockCard.addView(dateDisplay)
        
        // Alarm section title
        val alarmTitle = TextView(this).apply {
            text = "⏰ ALARM CONTROL"
            textSize = 20f
            setTextColor(Color.parseColor("#00D9FF"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 40, 0, 30)
            letterSpacing = 0.15f
        }
        
        // Glass card for alarm controls
        val alarmCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_glass_card)
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 30)
            }
        }
        
        // Time picker with futuristic styling
        timePicker = TimePicker(this).apply {
            setIs24HourView(true)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        
        // Buttons container
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 30, 0, 0)
            }
        }
        
        // Set alarm button with neon gradient
        setAlarmButton = Button(this).apply {
            text = "SET ALARM"
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundResource(R.drawable.bg_neon_button)
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(0, 0, 15, 0)
            }
        }
        
        // Clear alarm button with outline style
        clearAlarmButton = Button(this).apply {
            text = "CLEAR"
            textSize = 16f
            setTextColor(Color.parseColor("#00D9FF"))
            typeface = Typeface.DEFAULT_BOLD
            setBackgroundResource(R.drawable.bg_outline_button)
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(15, 0, 0, 0)
            }
        }
        
        buttonContainer.addView(setAlarmButton)
        buttonContainer.addView(clearAlarmButton)
        
        alarmCard.addView(timePicker)
        alarmCard.addView(buttonContainer)
        
        // Alarm status display
        alarmTimeText = TextView(this).apply {
            text = "NO ALARM SET"
            textSize = 16f
            setTextColor(Color.parseColor("#B0B8D4"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(20, 30, 20, 20)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.1f
        }
        
        // Add all views
        layout.addView(title)
        layout.addView(clockCard)
        layout.addView(alarmTitle)
        layout.addView(alarmCard)
        layout.addView(alarmTimeText)
        
        scrollView.addView(layout)
        mainLayout.addView(scrollView)
        setContentView(mainLayout)
        
        // Button listeners
        setAlarmButton.setOnClickListener {
            setAlarm()
        }
        
        clearAlarmButton.setOnClickListener {
            clearAlarm()
        }
        
        // Request notification permission if needed (Android 13+)
        maybeRequestNotificationPermission()
        
        // Start time updates
        startTimeUpdate()
        
        // Note: No direct dismiss via notification action; users must solve puzzle in-app
    }
    
    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), requestPostNotif)
            }
        }
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alarm ringing alerts"
                    enableVibration(true)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                mgr.createNotificationChannel(channel)
            }
        }
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
        
        val dateFormat = SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())
        dateDisplay.text = dateFormat.format(now.time).uppercase()
        
        // Check if alarm should trigger
        alarmCalendar?.let { alarm ->
            if (now.timeInMillis >= alarm.timeInMillis && !isAlarmRinging) {
                triggerAlarm()
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
            
            alarmTimeText.text = "⚡ ALARM: $alarmTimeStr"
            alarmTimeText.setTextColor(Color.parseColor("#00FF88"))
            
            Toast.makeText(this, "⚡ ALARM ARMED: $alarmTimeStr", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun clearAlarm() {
        alarmCalendar = null
        alarmTimeText.text = "NO ALARM SET"
        alarmTimeText.setTextColor(Color.parseColor("#B0B8D4"))
        stopAlarmSound()
        clearAlarmNotification()
        Toast.makeText(this, "🔕 ALARM DISARMED", Toast.LENGTH_SHORT).show()
    }
    
    private fun triggerAlarm() {
        isAlarmRinging = true
        
        showAlarmNotification()
        
        // Start playing alarm sound
        playAlarmSound()
        
        // Vibrate
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 1000, 500, 1000), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 1000, 500, 1000), 0)
            }
        } catch (e: Exception) {
            // Vibration not available
        }
        
        // Show alarm dialog
        showAlarmDialog()
    }
    
    private fun playAlarmSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.sound1)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not play alarm sound: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopAlarmSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // Sound was not playing
        }
        
        // Stop vibration
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.cancel()
        } catch (e: Exception) {
            // Vibration not available
        }
        
        isAlarmRinging = false
    }
    
    private fun showAlarmDialog() {
        mathPuzzleSolved = false

        // Generate easy mode puzzle with answer in [0..10]
        val (puzzleTextStr, answer) = generateMathPuzzle()
        currentMathAnswer = answer

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 30)
            // Dark background to ensure contrast with white puzzle text
            setBackgroundColor(Color.parseColor("#0A0E27"))
        }

        val title = TextView(this).apply {
            text = "⚡ ALARM ACTIVE!"
            textSize = 20f
            setTextColor(Color.parseColor("#00D9FF"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Solve this quick puzzle to dismiss"
            textSize = 16f
            setTextColor(Color.parseColor("#B0B8D4"))
            gravity = Gravity.CENTER
        }

        val puzzleText = TextView(this).apply {
            text = puzzleTextStr
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }

        val input = EditText(this).apply {
            hint = "?"
            textSize = 22f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#6C5CE7"))
            gravity = Gravity.CENTER
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val checkBtn = Button(this).apply {
            text = "CHECK"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#6C5CE7"))
        }

        val feedback = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 0)
        }

        // Dismiss button is created only after correct answer
        var dismissBtn: Button? = null

        checkBtn.setOnClickListener {
            val user = input.text.toString().toIntOrNull()
            if (user == currentMathAnswer) {
                mathPuzzleSolved = true
                feedback.text = "✅ Correct!"
                feedback.setTextColor(Color.parseColor("#00FF88"))
                input.isEnabled = false
                checkBtn.isEnabled = false

                if (dismissBtn == null) {
                    dismissBtn = Button(this).apply {
                        text = "DISMISS ALARM"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#00AA66"))
                        setOnClickListener {
                            dismissAlarm()
                            alarmDialog?.dismiss()
                        }
                    }
                    container.addView(dismissBtn)
                }
            } else {
                feedback.text = "❌ Try again. Hint: answer is between 0 and 10"
                feedback.setTextColor(Color.parseColor("#FF6B6B"))
                input.text.clear()
            }
        }

        container.addView(title)
        container.addView(subtitle)
        container.addView(puzzleText)
        container.addView(input)
        container.addView(checkBtn)
        container.addView(feedback)

        alarmDialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        // Make the dialog window background transparent so our container background is visible
        alarmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alarmDialog?.show()
    }
    
    private fun dismissAlarm() {
        stopAlarmSound()
        clearAlarm()
        Toast.makeText(this, "✅ DISMISSED", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAlarmNotification() {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⚡ CYBER ALARM")
            .setContentText("Alarm is ringing! Tap to open.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPending)
            .setFullScreenIntent(openPending, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(1000, 1000))
        
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(notificationId, builder.build())
    }

    // Generate an easy-mode math puzzle with answer in [0..10]
    private fun generateMathPuzzle(): Pair<String, Int> {
        val rand = Random()
        return if (rand.nextBoolean()) {
            // addition: a + b <= 10, with a,b in 0..10
            val a = rand.nextInt(11) // 0..10
            val bMax = 10 - a
            val b = if (bMax > 0) rand.nextInt(bMax + 1) else 0
            Pair("$a + $b = ?", a + b)
        } else {
            // subtraction: a - b in 0..10, with 0<=b<=a<=10
            val a = rand.nextInt(11) // 0..10
            val b = if (a > 0) rand.nextInt(a + 1) else 0
            Pair("$a - $b = ?", a - b)
        }
    }
    
    private fun clearAlarmNotification() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(notificationId)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timeUpdateRunnable?.let { handler.removeCallbacks(it) }
        stopAlarmSound()
        alarmDialog?.dismiss()
        clearAlarmNotification()
    }
    
    override fun onPause() {
        super.onPause()
        // Don't stop alarm sound when app goes to background
        // The alarm should keep ringing
    }
}
