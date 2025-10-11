package com.modernclockapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.modernclockapp.models.Alarm
import com.modernclockapp.storage.AlarmStorage

/**
 * Manages scheduling of alarms using Android's AlarmManager.
 * Ensures alarms trigger reliably even when app is killed.
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val storage = AlarmStorage.getInstance(context)
    
    companion object {
        private const val TAG = "AlarmScheduler"
        const val ACTION_ALARM_TRIGGER = "com.modernclockapp.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "alarm_id"
        
        @Volatile
        private var instance: AlarmScheduler? = null
        
        fun getInstance(context: Context): AlarmScheduler {
            return instance ?: synchronized(this) {
                instance ?: AlarmScheduler(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Schedule an alarm using AlarmManager
     */
    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.isEnabled) {
            Log.d(TAG, "Alarm ${alarm.id} is disabled, not scheduling")
            return
        }
        
        val triggerTime = alarm.getTriggerTimeMillis()
        val pendingIntent = createPendingIntent(alarm.id)
        
        try {
            // Use setExactAndAllowWhileIdle for precise timing even in Doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Alarm ${alarm.id} scheduled for ${java.util.Date(triggerTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to schedule exact alarm", e)
            // Fallback to inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancel a scheduled alarm
     */
    fun cancelAlarm(alarmId: Int) {
        val pendingIntent = createPendingIntent(alarmId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Alarm $alarmId cancelled")
    }
    
    /**
     * Cancel all scheduled alarms
     */
    fun cancelAllAlarms() {
        val alarms = storage.getAllAlarms()
        alarms.forEach { alarm ->
            cancelAlarm(alarm.id)
        }
        Log.d(TAG, "All alarms cancelled")
    }
    
    /**
     * Reschedule all enabled alarms (e.g., after boot)
     */
    fun rescheduleAllAlarms() {
        val alarms = storage.getEnabledAlarms()
        Log.d(TAG, "Rescheduling ${alarms.size} enabled alarms")
        
        alarms.forEach { alarm ->
            scheduleAlarm(alarm)
        }
    }
    
    /**
     * Create PendingIntent for alarm
     */
    private fun createPendingIntent(alarmId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getBroadcast(
            context,
            alarmId, // Use alarm ID as request code for uniqueness
            intent,
            flags
        )
    }
    
    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not required before Android 12
        }
    }
    
    /**
     * Request exact alarm permission (Android 12+)
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
    }
}
