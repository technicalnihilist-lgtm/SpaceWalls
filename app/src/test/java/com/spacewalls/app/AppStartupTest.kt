package com.spacewalls.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Startup smoke tests.
 *
 * These exist because v1.0.0 shipped a window background that pointed a <bitmap>
 * at a vector drawable: the theme failed to inflate and the app crashed before
 * the first frame. Both of these tests fail loudly if that regresses.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = SpaceWallsApp::class)
class AppStartupTest {

    @Test
    fun themeWindowBackgroundInflates() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val attrs = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        try {
            // Inflating is the actual regression check: v1.0.0 pointed a <bitmap>
            // at a vector here and threw before the first frame.
            val drawable = attrs.getDrawable(0)
            assertNotNull("Theme has no windowBackground drawable", drawable)
            assertNotNull("windowBackground could not be inflated", drawable!!.constantState)
        } finally {
            attrs.recycle()
        }
    }

    @Test
    fun mainActivityLaunchesWithoutCrashing() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
        assertTrue("Activity was destroyed during startup", !activity.isFinishing)
    }
}
