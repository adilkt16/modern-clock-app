package com.modernclockapp.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.modernclockapp.R
import com.modernclockapp.storage.AlarmStorage
import java.util.*

/**
 * Foreground service that handles alarm ringing.
 * Plays alarm sound, vibrates, and shows full-screen notification.
 */
class AlarmNotificationService : Service() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var alarmId: Int = -1
    private var endTimeMillis: Long? = null
    private var endTimeTimer: Timer? = null
    
    companion object {
        private const val TAG = "AlarmNotificationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ALARM_CHANNEL"
        const val ACTION_DISMISS = "com.modernclockapp.ACTION_DISMISS_ALARM"
        const val ACTION_STOP_SERVICE = "com.modernclockapp.ACTION_STOP_SERVICE"
        const val ACTION_AUTO_STOP = "com.modernclockapp.ACTION_AUTO_STOP"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with intent: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_DISMISS -> {
                Log.d(TAG, "Dismiss action received")
                stopAlarmAndService()
                return START_NOT_STICKY
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stop service action received")
                stopAlarmAndService()
                return START_NOT_STICKY
            }
            else -> {
                alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
                
                if (alarmId == -1) {
                    Log.e(TAG, "Invalid alarm ID")
                    stopSelf()
                    return START_NOT_STICKY
                }
                
                // Get alarm details
                val storage = AlarmStorage.getInstance(this)
                val alarm = storage.getAlarm(alarmId)
                
                if (alarm == null) {
                    Log.e(TAG, "Alarm $alarmId not found")
                    stopSelf()
                    return START_NOT_STICKY
                }
                
                // Set up end time auto-stop if configured
                endTimeMillis = alarm.getEndTimeMillis()
                if (endTimeMillis != null) {
                    setupEndTimeAutoStop(endTimeMillis!!)
                }
                
                // Mark alarm active so launcher can route to puzzle
                AlarmState.setActiveAlarmId(this, alarmId)

                // Start foreground service with notification
                startForeground(NOTIFICATION_ID, createNotification())
                
                // Start alarm sound and vibration
                startAlarmSound()
                startVibration()
                
                // Launch full-screen activity
                launchFullScreenActivity()
            }
        }
        
        return START_STICKY
    }
    
    private fun setupEndTimeAutoStop(endTime: Long) {
        endTimeTimer?.cancel()
        endTimeTimer = Timer()
        
        val now = System.currentTimeMillis()
        val delay = endTime - now
        
        Log.d(TAG, "Setting up auto-stop: current time = $now, end time = $endTime, delay = ${delay / 1000} seconds")
        
        if (delay > 0) {
            Log.d(TAG, "Auto-stop scheduled in ${delay / 1000} seconds")
            endTimeTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Log.d(TAG, "Auto-stop triggered at ${System.currentTimeMillis()}")
                    
                    // Stop sound and vibration immediately
                    stopAlarmSound()
                    stopVibration()
                    Log.d(TAG, "Sound and vibration stopped")
                    
                    // Clear active alarm state
                    AlarmState.clearActiveAlarm(this@AlarmNotificationService)

                    // Broadcast to close the AlarmDismissActivity
                    val broadcastIntent = Intent(ACTION_AUTO_STOP)
                    sendBroadcast(broadcastIntent)
                    Log.d(TAG, "Auto-stop broadcast sent")
                    
                    // Give broadcast time to be received, then stop service
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        stopForeground(true)
                        stopSelf()
                        Log.d(TAG, "Service stopped")
                    }, 500)
                }
            }, delay)
        } else {
            Log.w(TAG, "End time has already passed, delay = ${delay / 1000} seconds - not scheduling auto-stop")
        }
    }
    
    private fun startAlarmSound() {
        try {
            mediaPlayer?.release()
            
            // Create audio attributes for alarm
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            // Try to use custom alarm sound first, fallback to default
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(this@AlarmNotificationService, 
                        android.net.Uri.parse("android.resource://" + packageName + "/" + R.raw.sound1))
                } catch (e: Exception) {
                    Log.w(TAG, "Custom sound not found, using default alarm sound")
                    setDataSource(this@AlarmNotificationService, 
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                }
                
                setAudioAttributes(audioAttributes)
                isLooping = true
                prepare()
            }
            
            mediaPlayer?.start()
            Log.d(TAG, "Alarm sound started with USAGE_ALARM")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm sound", e)
        }
    }
    
    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 1000, 500, 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
            Log.d(TAG, "Vibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration", e)
        }
    }
    
    private fun stopAlarmSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            Log.d(TAG, "Alarm sound stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop alarm sound", e)
        }
    }
    
    private fun stopVibration() {
        try {
            vibrator?.cancel()
            Log.d(TAG, "Vibration stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop vibration", e)
        }
    }
    
    private fun launchFullScreenActivity() {
        val fullScreenIntent = Intent(this, AlarmDismissActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        try {
            startActivity(fullScreenIntent)
            Log.d(TAG, "Full-screen activity launched")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch full-screen activity", e)
        }
    }
    
    private fun createNotification(): Notification {
        val fullScreenIntent = Intent(this, AlarmDismissActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.altrise)
            .setContentTitle("⚡ AltRise Alarm")
            .setContentText("Alarm is ringing! Tap to dismiss.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for active alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun stopAlarmAndService() {
        Log.d(TAG, "Stopping alarm and service")
        endTimeTimer?.cancel()
        stopAlarmSound()
        stopVibration()
        // Clear active alarm state on manual dismiss or stop
        AlarmState.clearActiveAlarm(this)
        stopForeground(true)
        stopSelf()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        endTimeTimer?.cancel()
        stopAlarmSound()
        stopVibration()
        // Ensure state cleared if service dies unexpectedly
        AlarmState.clearActiveAlarm(this)
    }
}
