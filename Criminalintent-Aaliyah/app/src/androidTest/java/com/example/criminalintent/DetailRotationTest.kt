package com.example.criminalintent

import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DetailRotationTest {
    @Test fun unsavedTitleAndSolvedStateSurviveActivityRecreation() {
        val intent = CrimeDetailActivity.newIntent(ApplicationProvider.getApplicationContext(),
            UUID.randomUUID().toString(), true)
        ActivityScenario.launch<CrimeDetailActivity>(intent).use { scenario ->
            val deadline = System.currentTimeMillis() + 5000
            var ready = false
            while (!ready && System.currentTimeMillis() < deadline) {
                scenario.onActivity { ready = it.findViewById<EditText>(R.id.crimeTitle)?.isEnabled == true }
                if (!ready) Thread.sleep(25)
            }
            check(ready) { "Detail did not load" }
            onView(withId(R.id.crimeTitle)).perform(replaceText("Missing stapler"), closeSoftKeyboard())
            onView(withId(R.id.solved)).perform(click())
            scenario.recreate()
            onView(withId(R.id.crimeTitle)).check(matches(withText("Missing stapler")))
            onView(withId(R.id.solved)).check(matches(isChecked()))
        }
    }
}
