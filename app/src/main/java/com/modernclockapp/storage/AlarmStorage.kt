package com.modernclockapp.storage

import android.content.Context
import android.content.SharedPreferences
import com.modernclockapp.models.Alarm
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages persistent storage of alarms using SharedPreferences.
 * Stores alarms as JSON strings for easy serialization.
 */
class AlarmStorage(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "alarm_storage"
        private const val KEY_ALARMS = "alarms"
        private const val KEY_NEXT_ID = "next_alarm_id"
        
        @Volatile
        private var instance: AlarmStorage? = null
        
        fun getInstance(context: Context): AlarmStorage {
            return instance ?: synchronized(this) {
                instance ?: AlarmStorage(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Save a new alarm or update existing one
     */
    fun saveAlarm(alarm: Alarm) {
        val alarms = getAllAlarms().toMutableList()
        
        // Remove existing alarm with same ID if it exists
        alarms.removeAll { it.id == alarm.id }
        
        // Add the new/updated alarm
        alarms.add(alarm)
        
        // Save all alarms
        saveAllAlarms(alarms)
    }
    
    /**
     * Get all saved alarms
     */
    fun getAllAlarms(): List<Alarm> {
        val alarmsJson = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        
        return try {
            val jsonArray = JSONArray(alarmsJson)
            val alarms = mutableListOf<Alarm>()
            
            for (i in 0 until jsonArray.length()) {
                val alarmJson = jsonArray.getJSONObject(i)
                alarms.add(Alarm.fromJson(alarmJson))
            }
            
            alarms
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get a specific alarm by ID
     */
    fun getAlarm(alarmId: Int): Alarm? {
        return getAllAlarms().find { it.id == alarmId }
    }
    
    /**
     * Delete an alarm
     */
    fun deleteAlarm(alarmId: Int) {
        val alarms = getAllAlarms().filter { it.id != alarmId }
        saveAllAlarms(alarms)
    }
    
    /**
     * Delete all alarms
     */
    fun deleteAllAlarms() {
        prefs.edit().remove(KEY_ALARMS).apply()
    }
    
    /**
     * Update alarm enabled state
     */
    fun setAlarmEnabled(alarmId: Int, enabled: Boolean) {
        val alarms = getAllAlarms().toMutableList()
        val index = alarms.indexOfFirst { it.id == alarmId }
        
        if (index != -1) {
            alarms[index] = alarms[index].copy(isEnabled = enabled)
            saveAllAlarms(alarms)
        }
    }
    
    /**
     * Generate next unique alarm ID
     */
    fun getNextAlarmId(): Int {
        val nextId = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, nextId + 1).apply()
        return nextId
    }
    
    /**
     * Save all alarms to storage
     */
    private fun saveAllAlarms(alarms: List<Alarm>) {
        val jsonArray = JSONArray()
        alarms.forEach { alarm ->
            jsonArray.put(alarm.toJson())
        }
        
        prefs.edit().putString(KEY_ALARMS, jsonArray.toString()).apply()
    }
    
    /**
     * Check if there are any enabled alarms
     */
    fun hasEnabledAlarms(): Boolean {
        return getAllAlarms().any { it.isEnabled }
    }
    
    /**
     * Get all enabled alarms
     */
    fun getEnabledAlarms(): List<Alarm> {
        return getAllAlarms().filter { it.isEnabled }
    }
}
