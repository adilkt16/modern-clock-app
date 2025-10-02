package com.modernclockapp

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            text = "🕒 Modern Clock App"
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
            text = "🔔 Set Alarm"
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
            text = "🔕 Clear Alarm"
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
        
        // Request notification permission if needed (Android 13+)
        maybeRequestNotificationPermission()

        // Start time updates
        startTimeUpdate()

        // If launched from notification action (dismiss)
        intent?.action?.let { action ->
            if (action == "DISMISS_ALARM") {
                dismissAlarm()
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
    
    private fun updateTime() {
        val now = Calendar.getInstance()
        
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeDisplay.text = timeFormat.format(now.time)
        
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateDisplay.text = dateFormat.format(now.time)
        
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
            
            alarmTimeText.text = "⏰ Alarm set for: $alarmTimeStr"
            
            Toast.makeText(this, "🔔 Alarm set for $alarmTimeStr", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting alarm: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun clearAlarm() {
        alarmCalendar = null
        alarmTimeText.text = "No alarm set"
        stopAlarmSound()
        Toast.makeText(this, "🔕 Alarm cleared", Toast.LENGTH_SHORT).show()
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
        alarmDialog = AlertDialog.Builder(this)
            .setTitle("⏰ ALARM!")
            .setMessage("Time to wake up!")
            .setCancelable(false)
            .setPositiveButton("Dismiss") { dialog, _ ->
                dismissAlarm()
                dialog.dismiss()
            }
            .setNegativeButton("Snooze (5 min)") { dialog, _ ->
                snoozeAlarm()
                dialog.dismiss()
            }
            .create()
        
        alarmDialog?.show()
    }
    
    private fun dismissAlarm() {
        stopAlarmSound()
        clearAlarm()
        Toast.makeText(this, "✅ Alarm dismissed", Toast.LENGTH_SHORT).show()
    }
    
    private fun snoozeAlarm() {
        stopAlarmSound()
        
        // Set alarm for 5 minutes from now
        alarmCalendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
        }
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val snoozeTimeStr = timeFormat.format(alarmCalendar!!.time)
        
        alarmTimeText.text = "😴 Snoozed until: $snoozeTimeStr"
        Toast.makeText(this, "😴 Alarm snoozed for 5 minutes", Toast.LENGTH_LONG).show()
    }

    private fun showAlarmNotification() {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, MainActivity::class.java).apply { action = "DISMISS_ALARM" }
        val dismissPending = PendingIntent.getActivity(
            this, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Alarm Ringing")
            .setContentText("Tap to open. Dismiss or Snooze from dialog.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPending)
            .setFullScreenIntent(openPending, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(1000, 1000))

        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(notificationId, builder.build())
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
