package fr.arbresdeparis

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A phone set to English still gets the app in French, including what the system location button is given. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class AppLocaleTest {
    class LocalizedActivity : ComponentActivity() {
        var composeConfiguration: Configuration? = null

        override fun attachBaseContext(newBase: Context) {
            super.attachBaseContext(newBase)
            useAppConfiguration()
        }

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            setContent { composeConfiguration = LocalConfiguration.current }
        }
    }

    @Test
    fun englishPhone_activityIsInFrench() {
        val activity = Robolectric.buildActivity(LocalizedActivity::class.java).setup().get()
        assertEquals(
            "fr",
            activity.resources.configuration.locales[0]
                .language,
        )
        assertEquals(
            "fr",
            activity.composeConfiguration
                ?.locales
                ?.get(0)
                ?.language,
        )
    }

    @Test
    fun englishPhone_otherSettingsAreKept() {
        val activity = Robolectric.buildActivity(LocalizedActivity::class.java).setup().get()
        val system = activity.applicationContext.resources.configuration
        assertEquals(system.uiMode, activity.resources.configuration.uiMode)
        assertEquals(system.fontScale, activity.resources.configuration.fontScale)
    }
}
