package com.altrise.clockapp

import com.altrise.clockapp.models.Alarm
import com.altrise.clockapp.models.WorldClock
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class ClockAppUnitTest {

    @Test
    fun alarm_timeFormatting_isCorrect() {
        val alarm = Alarm(1, 14, 30, true, "Test Alarm")
        assertEquals("2:30 PM", alarm.getFormattedTime())
        assertEquals("14:30", alarm.getTimeIn24HourFormat())
    }

    @Test
    fun alarm_morningTime_formatsCorrectly() {
        val alarm = Alarm(2, 9, 15, true, "Morning Alarm")
        assertEquals("9:15 AM", alarm.getFormattedTime())
    }

    @Test
    fun alarm_midnightTime_formatsCorrectly() {
        val alarm = Alarm(3, 0, 0, true, "Midnight Alarm")
        assertEquals("12:00 AM", alarm.getFormattedTime())
    }

    @Test
    fun alarm_noonTime_formatsCorrectly() {
        val alarm = Alarm(4, 12, 0, true, "Noon Alarm")
        assertEquals("12:00 PM", alarm.getFormattedTime())
    }

    @Test
    fun worldClock_newYork_hasCorrectTimeZone() {
        val worldClock = WorldClock("New York", "America/New_York")
        assertEquals("New York", worldClock.cityName)
        assertEquals("America/New_York", worldClock.timeZoneId)
        assertNotNull(worldClock.getCurrentTime())
        assertNotNull(worldClock.getCurrentDate())
    }

    @Test
    fun worldClock_london_hasCorrectTimeZone() {
        val worldClock = WorldClock("London", "Europe/London")
        assertEquals("London", worldClock.cityName)
        assertEquals("Europe/London", worldClock.timeZoneId)
    }

    @Test
    fun worldClock_timeDifference_calculatesCorrectly() {
        val worldClock = WorldClock("Tokyo", "Asia/Tokyo")
        val timeDiff = worldClock.getTimeDifference()
        assertTrue("Time difference should contain 'h' or 'Same'", 
            timeDiff.contains("h") || timeDiff == "Same")
    }

    @Test
    fun alarm_enabledState_togglesCorrectly() {
        val alarm = Alarm(5, 8, 0, true, "Toggle Test")
        assertTrue(alarm.isEnabled)
        
        val disabledAlarm = alarm.copy(isEnabled = false)
        assertFalse(disabledAlarm.isEnabled)
    }
}