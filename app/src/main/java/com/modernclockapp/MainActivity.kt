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
import android.view.HapticFeedbackConstants
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.modernclockapp.alarm.AlarmScheduler
import com.modernclockapp.models.Alarm
import com.modernclockapp.storage.AlarmStorage
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    companion object {
        const val EXTRA_SHOW_PUZZLE = "com.modernclockapp.EXTRA_SHOW_PUZZLE"
    }
    
    // Storage and scheduler for persistent alarms
    private lateinit var alarmStorage: AlarmStorage
    private lateinit var alarmScheduler: AlarmScheduler
    private var currentAlarmId: Int? = null
    
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var alarmTimeText: TextView
    private lateinit var setAlarmButton: Button
    private lateinit var clearAlarmButton: Button
    // New compact time selectors
    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var ampmToggle: ToggleButton
    // Optional end time controls
    private lateinit var endTimeSwitch: Switch
    private lateinit var endHourPicker: NumberPicker
    private lateinit var endMinutePicker: NumberPicker
    private lateinit var endAmpmToggle: ToggleButton
    private var isEndTimeEnabled: Boolean = false
    private var is24hFormat: Boolean = true
    private var isPm: Boolean = false
    private var isEndPm: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var timeUpdateRunnable: Runnable? = null
    private var alarmCalendar: Calendar? = null
    private var endTimeCalendar: Calendar? = null
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
        
        // Initialize storage and scheduler
        alarmStorage = AlarmStorage.getInstance(this)
        alarmScheduler = AlarmScheduler.getInstance(this)
        
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
        
        // Compact time selection row
        val timeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        hourPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 23
            value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            setFormatter { String.format("%02d", it) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 12, 0) }
        }
        val colon = TextView(this).apply {
            text = ":"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(4, 0, 4, 0)
        }
        minutePicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 59
            value = Calendar.getInstance().get(Calendar.MINUTE)
            setFormatter { String.format("%02d", it) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(12, 0, 12, 0) }
        }
        ampmToggle = ToggleButton(this).apply {
            textOn = "PM"
            textOff = "AM"
            text = textOff
            isChecked = false
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(12, 0, 0, 0) }
        }
        timeRow.addView(hourPicker)
        timeRow.addView(colon)
        timeRow.addView(minutePicker)
        timeRow.addView(ampmToggle)

        // 24H format toggle row
        val formatRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 8) }
        }
        val formatLabel = TextView(this).apply {
            text = "24H FORMAT"
            textSize = 14f
            setTextColor(Color.parseColor("#B0B8D4"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val formatSwitch = Switch(this).apply { isChecked = true }
        formatSwitch.setOnCheckedChangeListener { _, isChecked ->
            is24hFormat = isChecked
            // Update hour picker range and AM/PM visibility
            if (is24hFormat) {
                hourPicker.minValue = 0
                hourPicker.maxValue = 23
                ampmToggle.visibility = View.GONE
                // convert 12h selection to 24h
                if (isPm && hourPicker.value < 12) hourPicker.value += 12
                if (!isPm && hourPicker.value == 12) hourPicker.value = 0
                // End-time 24h handling
                endHourPicker.minValue = 0
                endHourPicker.maxValue = 23
                endAmpmToggle.visibility = View.GONE
                if (isEndPm && endHourPicker.value < 12) endHourPicker.value += 12
                if (!isEndPm && endHourPicker.value == 12) endHourPicker.value = 0
            } else {
                ampmToggle.visibility = View.VISIBLE
                // convert 24h to 12h display
                val h24 = hourPicker.value
                isPm = h24 >= 12
                ampmToggle.isChecked = isPm
                hourPicker.minValue = 1
                hourPicker.maxValue = 12
                hourPicker.value = when {
                    h24 == 0 -> 12
                    h24 > 12 -> h24 - 12
                    else -> h24
                }
                // End-time 12h handling
                endAmpmToggle.visibility = View.VISIBLE
                val eh24 = endHourPicker.value
                isEndPm = eh24 >= 12
                endAmpmToggle.isChecked = isEndPm
                endHourPicker.minValue = 1
                endHourPicker.maxValue = 12
                endHourPicker.value = when {
                    eh24 == 0 -> 12
                    eh24 > 12 -> eh24 - 12
                    else -> eh24
                }
            }
        }
        formatRow.addView(formatLabel)
        formatRow.addView(formatSwitch)

        // End time (optional) UI
        val endRowContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        val endHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val endLabel = TextView(this).apply {
            text = "END TIME (OPTIONAL)"
            textSize = 14f
            setTextColor(Color.parseColor("#B0B8D4"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        endTimeSwitch = Switch(this).apply { isChecked = false }
        endTimeSwitch.setOnCheckedChangeListener { _, checked ->
            isEndTimeEnabled = checked
            setEndTimeControlsEnabled(checked)
        }
        endHeaderRow.addView(endLabel)
        endHeaderRow.addView(endTimeSwitch)

        val endTimeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        endHourPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 23
            value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            setFormatter { String.format("%02d", it) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 12, 0) }
        }
        val endColon = TextView(this).apply {
            text = ":"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(4, 0, 4, 0)
        }
        endMinutePicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 59
            value = Calendar.getInstance().get(Calendar.MINUTE)
            setFormatter { String.format("%02d", it) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(12, 0, 12, 0) }
        }
        endAmpmToggle = ToggleButton(this).apply {
            textOn = "PM"
            textOff = "AM"
            text = textOff
            isChecked = false
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(12, 0, 0, 0) }
        }
        endTimeRow.addView(endHourPicker)
        endTimeRow.addView(endColon)
        endTimeRow.addView(endMinutePicker)
        endTimeRow.addView(endAmpmToggle)

        endRowContainer.addView(endHeaderRow)
        endRowContainer.addView(endTimeRow)

        
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
        
    alarmCard.addView(timeRow)
    alarmCard.addView(endRowContainer)
    alarmCard.addView(formatRow)
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
        
        // Load any existing alarms from storage
        loadExistingAlarm()
        
    // Start time updates and run once to avoid delay
    startTimeUpdate()
    updateTime()
        
        // Note: No direct dismiss via notification action; users must solve puzzle in-app

        // Initialize end time controls to disabled by default and reflect 24h/12h visibility
        setEndTimeControlsEnabled(false)
        endAmpmToggle.visibility = if (is24hFormat) View.GONE else View.VISIBLE

        // If launched with intent to show puzzle, handle it now
        handleIntentForPuzzle(intent)
    }

    override fun onResume() {
        super.onResume()
        // If alarm is currently ringing and the dialog isn't visible, show it again
        if (isAlarmRinging) {
            val showing = alarmDialog?.isShowing == true
            if (!showing) {
                showAlarmDialog()
            }
        } else {
            // Ensure we evaluate due alarms immediately on resume
            updateTime()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) handleIntentForPuzzle(intent)
    }

    private fun handleIntentForPuzzle(intent: Intent) {
        val force = intent.getBooleanExtra(EXTRA_SHOW_PUZZLE, false)
        if ((isAlarmRinging || force)) {
            if (alarmDialog?.isShowing != true) {
                showAlarmDialog()
            }
        }
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

    private fun setEndTimeControlsEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1.0f else 0.4f
        endHourPicker.isEnabled = enabled
        endMinutePicker.isEnabled = enabled
        endAmpmToggle.isEnabled = enabled
        endHourPicker.alpha = alpha
        endMinutePicker.alpha = alpha
        endAmpmToggle.alpha = alpha
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

        // Auto-stop when end time reached
        if (isAlarmRinging && endTimeCalendar != null) {
            endTimeCalendar?.let { endCal ->
                if (now.timeInMillis >= endCal.timeInMillis) {
                    autoStopAlarm()
                }
            }
        }
    }
    
    private fun setAlarm() {
        try {
            val minute = minutePicker.value
            val hour = if (is24hFormat) {
                hourPicker.value
            } else {
                var h = hourPicker.value % 12
                if (ampmToggle.isChecked) h += 12 // PM
                h
            }
            applyAlarm(hour, minute)
            
        } catch (e: Exception) {
            Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyAlarm(hour: Int, minute: Int) {
        // Create alarm object
        val alarmId = currentAlarmId ?: alarmStorage.getNextAlarmId()
        
        val alarm = Alarm(
            id = alarmId,
            hourOfDay = hour,
            minute = minute,
            isEnabled = true,
            label = "",
            hasEndTime = isEndTimeEnabled,
            endHourOfDay = if (isEndTimeEnabled) {
                if (is24hFormat) endHourPicker.value
                else {
                    var h = endHourPicker.value % 12
                    if (endAmpmToggle.isChecked) h += 12
                    h
                }
            } else null,
            endMinute = if (isEndTimeEnabled) endMinutePicker.value else null
        )
        
        // Save to storage
        alarmStorage.saveAlarm(alarm)
        currentAlarmId = alarmId
        
        // Schedule with AlarmManager
        alarmScheduler.scheduleAlarm(alarm)
        
        // Update legacy alarm calendar for in-app checking
        alarmCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        // Compute end time if enabled
        endTimeCalendar = if (isEndTimeEnabled && alarm.endHourOfDay != null && alarm.endMinute != null) {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.endHourOfDay)
                set(Calendar.MINUTE, alarm.endMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                val now = Calendar.getInstance()
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
                alarmCalendar?.let { startCal ->
                    if (timeInMillis <= startCal.timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        } else null

        val timeFormat = if (is24hFormat) SimpleDateFormat("HH:mm", Locale.getDefault())
        else SimpleDateFormat("hh:mm a", Locale.getDefault())
        val alarmTimeStr = timeFormat.format(alarmCalendar!!.time)

        val friendly = friendlyInDuration(Calendar.getInstance(), alarmCalendar!!)
        val endSuffix = endTimeCalendar?.let { "  → auto-stop at ${timeFormat.format(it.time)}" } ?: ""
        alarmTimeText.text = "⚡ ALARM: $alarmTimeStr  ($friendly)$endSuffix"
        alarmTimeText.setTextColor(Color.parseColor("#00FF88"))
        alarmTimeText.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

        Toast.makeText(this, "⚡ ALARM SAVED & SCHEDULED: $alarmTimeStr — $friendly$endSuffix", Toast.LENGTH_LONG).show()
    }

    private fun setAlarmAfter(minutes: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutes)
        applyAlarm(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun friendlyInDuration(now: Calendar, target: Calendar): String {
        var diffMs = target.timeInMillis - now.timeInMillis
        if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000 // safety wrap
        val totalMinutes = (diffMs / 60000L).toInt()
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "in ${hours}h ${mins}m"
            hours > 0 -> "in ${hours}h"
            else -> "in ${mins}m"
        }
    }
    
    private fun clearAlarm() {
        // Cancel scheduled alarm
        currentAlarmId?.let { id ->
            alarmScheduler.cancelAlarm(id)
            alarmStorage.deleteAlarm(id)
        }
        
        currentAlarmId = null
        alarmCalendar = null
        endTimeCalendar = null
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

    private fun autoStopAlarm() {
        // Stop ringing and clear state
        stopAlarmSound()
        clearAlarmNotification()
        alarmDialog?.dismiss()
        isAlarmRinging = false
        val fmt = if (is24hFormat) SimpleDateFormat("HH:mm", Locale.getDefault()) else SimpleDateFormat("hh:mm a", Locale.getDefault())
        val endStr = endTimeCalendar?.let { fmt.format(it.time) } ?: "END"
        alarmTimeText.text = "🔕 AUTO-STOPPED AT $endStr"
        alarmTimeText.setTextColor(Color.parseColor("#B0B8D4"))
        alarmCalendar = null
        endTimeCalendar = null
        Toast.makeText(this, "⏹️ Alarm auto-stopped", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAlarmNotification() {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_SHOW_PUZZLE, true)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
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
    
    /**
     * Load existing alarm from storage on app start
     */
    private fun loadExistingAlarm() {
        val alarms = alarmStorage.getEnabledAlarms()
        
        if (alarms.isNotEmpty()) {
            // Get the first enabled alarm (for simplicity, this app handles one alarm at a time)
            val alarm = alarms.first()
            currentAlarmId = alarm.id
            
            // Update UI to reflect the loaded alarm
            val timeFormat = if (is24hFormat) SimpleDateFormat("HH:mm", Locale.getDefault())
            else SimpleDateFormat("hh:mm a", Locale.getDefault())
            
            val alarmCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hourOfDay)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
            }
            
            alarmCalendar = alarmCal
            
            // Set end time if configured
            if (alarm.hasEndTime && alarm.endHourOfDay != null && alarm.endMinute != null) {
                endTimeCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarm.endHourOfDay)
                    set(Calendar.MINUTE, alarm.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
                    if (timeInMillis <= alarmCal.timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val alarmTimeStr = timeFormat.format(alarmCal.time)
            val friendly = friendlyInDuration(Calendar.getInstance(), alarmCal)
            val endSuffix = endTimeCalendar?.let { "  → auto-stop at ${timeFormat.format(it.time)}" } ?: ""
            
            alarmTimeText.text = "⚡ ALARM: $alarmTimeStr  ($friendly)$endSuffix"
            alarmTimeText.setTextColor(Color.parseColor("#00FF88"))
        }
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
