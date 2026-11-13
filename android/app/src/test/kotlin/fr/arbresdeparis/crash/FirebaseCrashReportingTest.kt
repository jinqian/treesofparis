package fr.arbresdeparis.crash

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** No `google-services.json` in tests: no Firebase app, so only the saved choice is checked. */
@RunWith(RobolectricTestRunner::class)
class FirebaseCrashReportingTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun beforeTheAnswer_reportsAreOffAndTheQuestionIsOpen() =
        runTest {
            val crashReporting = FirebaseCrashReporting(context)
            assertFalse(crashReporting.observeEnabled().first())
            assertFalse(crashReporting.observeAnswered().first())
        }

    @Test
    fun anAnswer_isKeptAcrossLaunches() =
        runTest {
            FirebaseCrashReporting(context).setEnabled(true)
            val next = FirebaseCrashReporting(context)
            assertTrue(next.observeEnabled().first())
            assertTrue(next.observeAnswered().first())
        }

    @Test
    fun no_isAnAnswerToo() =
        runTest {
            FirebaseCrashReporting(context).setEnabled(false)
            val next = FirebaseCrashReporting(context)
            assertFalse(next.observeEnabled().first())
            assertTrue(next.observeAnswered().first())
        }
}
