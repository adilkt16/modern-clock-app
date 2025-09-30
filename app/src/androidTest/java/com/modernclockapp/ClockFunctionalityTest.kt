package com.modernclockapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClockFunctionalityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun stopwatch_startsAndStops() {
        // Navigate to stopwatch
        onView(withText("Stopwatch"))
            .perform(click())
        
        // Start the stopwatch
        onView(withId(R.id.startStopButton))
            .perform(click())
        
        // Check that the button text changes to "Stop"
        onView(withId(R.id.startStopButton))
            .check(matches(withText("Stop")))
        
        // Stop the stopwatch
        onView(withId(R.id.startStopButton))
            .perform(click())
        
        // Check that the button text changes back to "Start"
        onView(withId(R.id.startStopButton))
            .check(matches(withText("Start")))
    }

    @Test
    fun timer_displaysCorrectInitialValue() {
        // Navigate to timer
        onView(withText("Timer"))
            .perform(click())
        
        // Check that the timer display shows initial value
        onView(withId(R.id.timerDisplay))
            .check(matches(withText("00:00:00")))
    }

    @Test
    fun alarm_listDisplaysItems() {
        // Navigate to alarm
        onView(withText("Alarm"))
            .perform(click())
        
        // Check that the RecyclerView is displayed
        onView(withId(R.id.alarmRecyclerView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addAlarmFab_isClickable() {
        // Navigate to alarm
        onView(withText("Alarm"))
            .perform(click())
        
        // Click the FAB
        onView(withId(R.id.addAlarmFab))
            .perform(click())
    }
}