package fr.arbresdeparis.core.designsystem.action

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ComposeEmailTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun opensAMailtoIntentWithAddressSubjectAndBody() {
        assertTrue(app.composeEmail("arbresdeparis@gmail.com", "Arbres de Paris", "Bonjour,"))

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto:", intent.dataString)
        assertArrayEquals(arrayOf("arbresdeparis@gmail.com"), intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Arbres de Paris", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Bonjour,", intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}
