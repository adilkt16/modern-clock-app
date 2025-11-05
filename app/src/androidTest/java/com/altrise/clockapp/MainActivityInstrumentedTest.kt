package com.altrise.clockapp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivity_displaysCorrectly() {
        // Check that the main activity starts and displays the tab layout
        onView(withId(R.id.tabLayout))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.viewPager))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clockTab_isDisplayedByDefault() {
        // Check that the clock fragment is displayed by default
        onView(withId(R.id.digitalClock))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.analogClock))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigationTabs_workCorrectly() {
        // Test navigation to alarm tab
        onView(withText("Alarm"))
            .perform(click())
        
        onView(withId(R.id.alarmRecyclerView))
            .check(matches(isDisplayed()))
        
        // Test navigation to timer tab
        onView(withText("Timer"))
            .perform(click())
        
        onView(withId(R.id.timerDisplay))
            .check(matches(isDisplayed()))
        
        // Test navigation to stopwatch tab
        onView(withText("Stopwatch"))
            .perform(click())
        
        onView(withId(R.id.stopwatchDisplay))
            .check(matches(isDisplayed()))
    }

    @Test
    fun digitalClock_isClickable() {
        onView(withId(R.id.digitalClock))
            .check(matches(isClickable()))
            .perform(click())
    }

    @Test
    fun timerButtons_areDisplayed() {
        // Navigate to timer tab
        onView(withText("Timer"))
            .perform(click())
        
        // Check that all timer buttons are displayed
        onView(withId(R.id.startButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.pauseButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.resetButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun stopwatchButtons_areDisplayed() {
        // Navigate to stopwatch tab
        onView(withText("Stopwatch"))
            .perform(click())
        
        // Check that all stopwatch buttons are displayed
        onView(withId(R.id.startStopButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.lapButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.resetStopwatchButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun alarmFab_isDisplayed() {
        // Navigate to alarm tab
        onView(withText("Alarm"))
            .perform(click())
        
        // Check that the floating action button is displayed
        onView(withId(R.id.addAlarmFab))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }
}