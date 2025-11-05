package com.altrise.clockapp.alarm

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple shared state holder to track whether an alarm is currently ringing.
 * Backed by SharedPreferences so any Activity can check and route accordingly.
 */
object AlarmState {
    private const val PREFS = "alarm_state_prefs"
    private const val KEY_ACTIVE_ALARM_ID = "active_alarm_id"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setActiveAlarmId(ctx: Context, alarmId: Int) {
        prefs(ctx).edit().putInt(KEY_ACTIVE_ALARM_ID, alarmId).apply()
    }

    fun clearActiveAlarm(ctx: Context) {
        prefs(ctx).edit().remove(KEY_ACTIVE_ALARM_ID).apply()
    }

    fun getActiveAlarmId(ctx: Context): Int? {
        val id = prefs(ctx).getInt(KEY_ACTIVE_ALARM_ID, -1)
        return if (id != -1) id else null
    }

    fun isAlarmActive(ctx: Context): Boolean = getActiveAlarmId(ctx) != null
}
