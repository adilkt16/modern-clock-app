package com.modernclockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
// import android.util.Log (removed for release)
import com.modernclockapp.storage.AlarmStorage

/**
 * BroadcastReceiver that handles alarm triggers from AlarmManager.
 * Starts the AlarmNotificationService when an alarm goes off.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
    // Log.d(TAG, "Alarm received: ${intent.action}") // removed for release
        
        when (intent.action) {
            AlarmScheduler.ACTION_ALARM_TRIGGER -> {
                val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
                
                if (alarmId == -1) {
                    // Log.e(TAG, "Invalid alarm ID received") // removed for release
                    return
                }
                
                // Log.d(TAG, "Triggering alarm $alarmId") // removed for release
                
                // Verify alarm still exists and is enabled
                val storage = AlarmStorage.getInstance(context)
                val alarm = storage.getAlarm(alarmId)
                
                if (alarm == null) {
                    // Log.w(TAG, "Alarm $alarmId not found in storage") // removed for release
                    return
                }
                
                if (!alarm.isEnabled) {
                    // Log.d(TAG, "Alarm $alarmId is disabled, ignoring") // removed for release
                    return
                }
                
                // Start the alarm notification service
                val serviceIntent = Intent(context, AlarmNotificationService::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                }
                
                // Start as foreground service for reliability
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Log.d(TAG, "AlarmNotificationService started for alarm $alarmId") // removed for release
            }
        }
    }
}
