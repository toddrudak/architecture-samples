package com.example.android.architecture.blueprints.todoapp.torquelift

import android.app.Application
import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoNavGraph
import com.example.android.architecture.blueprints.todoapp.TodoTheme
import com.example.android.architecture.blueprints.todoapp.TorqueLiftAnalytics
import com.segment.analytics.kotlin.core.Analytics
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Live check, not a regression test: starts the Torque Lift wrapper the way TodoApplication does,
 * adds a task and checks it off through the real screens, then waits for the SDK's flush so the
 * two events (Task Created, Task Completed) leave the emulator for ingest.torquelift.ai. The
 * workflow reads logcat for the SDK's upload lines afterwards.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class TorqueLiveEventsTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun addAndCompleteTask_eventsAreSent() {
        Analytics.debugLogsEnabled = true
        // HiltTestApplication replaces TodoApplication here, so its onCreate never ran.
        TorqueLiftAnalytics.start(ApplicationProvider.getApplicationContext<Application>(), consent = true)
        Log.i("TorqueProof", "started, anonymousId=${TorqueLiftAnalytics.anonymousId()}")

        composeTestRule.setContent { TodoTheme { TodoNavGraph() } }

        // Task Created: add a task on the add task screen and save it.
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_task)).performClick()
        field(R.string.title_hint).performTextInput("TORQUE LIVE PROOF")
        field(R.string.description_hint).performTextInput("sent from CI")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task)).performClick()
        composeTestRule.onNodeWithText("TORQUE LIVE PROOF").assertIsDisplayed()
        Log.i("TorqueProof", "task saved")

        // Task Completed: tick the checkbox in the task list.
        composeTestRule.onNode(isToggleable()).performClick()
        composeTestRule.waitForIdle()
        Log.i("TorqueProof", "task checked off")

        // The SDK flushes every 30 seconds; stay alive for one full interval plus the upload.
        Thread.sleep(45_000)
        Log.i("TorqueProof", "done waiting")
    }

    private fun field(textId: Int) =
        composeTestRule.onNode(hasSetTextAction() and hasText(activity.getString(textId)))
}
