package com.altrise.clockapp.models

import org.json.JSONObject

/**
 * Data class representing a single alarm.
 * Stores all necessary information for alarm persistence and scheduling.
 */
data class Alarm(
    val id: Int,                    // Unique identifier for the alarm
    val hourOfDay: Int,             // Hour in 24-hour format (0-23)
    val minute: Int,                // Minute (0-59)
    val isEnabled: Boolean = true,  // Whether alarm is active
    val label: String = "",         // Optional label for the alarm
    val endHourOfDay: Int? = null,  // Optional end time hour (24-hour format)
    val endMinute: Int? = null,     // Optional end time minute
    val hasEndTime: Boolean = false // Whether end time is enabled
) {
    /**
     * Get the trigger time in milliseconds from epoch
     */
    fun getTriggerTimeMillis(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If time has passed today, schedule for tomorrow
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }
    
    /**
     * Get the end time in milliseconds from epoch (if enabled)
     * Calculates end time relative to current moment (when alarm is ringing)
     */
    fun getEndTimeMillis(): Long? {
        if (!hasEndTime || endHourOfDay == null || endMinute == null) return null
        
        val now = java.util.Calendar.getInstance()
        val endTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, endHourOfDay)
            set(java.util.Calendar.MINUTE, endMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val alarmTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        // If end time is before alarm time in the same day, it must be for tomorrow
        if (endTime.before(alarmTime) || endTime.equals(alarmTime)) {
            endTime.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        
        // If end time is in the past (before now), add a day
        // This handles the case where alarm was set for yesterday but triggered today
        if (endTime.before(now)) {
            endTime.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        
        return endTime.timeInMillis
    }
    
    /**
     * Convert alarm to JSON for storage
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("hourOfDay", hourOfDay)
            put("minute", minute)
            put("isEnabled", isEnabled)
            put("label", label)
            put("hasEndTime", hasEndTime)
            if (hasEndTime && endHourOfDay != null && endMinute != null) {
                put("endHourOfDay", endHourOfDay)
                put("endMinute", endMinute)
            }
        }
    }
    
    companion object {
        /**
         * Create alarm from JSON
         */
        fun fromJson(json: JSONObject): Alarm {
            return Alarm(
                id = json.getInt("id"),
                hourOfDay = json.getInt("hourOfDay"),
                minute = json.getInt("minute"),
                isEnabled = json.optBoolean("isEnabled", true),
                label = json.optString("label", ""),
                hasEndTime = json.optBoolean("hasEndTime", false),
                endHourOfDay = if (json.has("endHourOfDay")) json.getInt("endHourOfDay") else null,
                endMinute = if (json.has("endMinute")) json.getInt("endMinute") else null
            )
        }
    }
}
