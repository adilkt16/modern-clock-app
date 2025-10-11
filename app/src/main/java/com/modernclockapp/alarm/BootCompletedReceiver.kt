package com.modernclockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that restores alarms after device reboot.
 * Reschedules all enabled alarms to ensure they persist across reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, restoring alarms")
            
            val scheduler = AlarmScheduler.getInstance(context)
            scheduler.rescheduleAllAlarms()
            
            Log.d(TAG, "Alarms restored successfully")
        }
    }
}
