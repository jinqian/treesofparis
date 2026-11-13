package fr.arbresdeparis.theme

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppThemeSettingsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun byDefault_followsThePhone() =
        runTest {
            assertEquals(ThemeMode.SYSTEM, AppThemeSettings(context).observeThemeMode().first())
            assertEquals(ThemeMode.SYSTEM, savedThemeMode(context))
        }

    @Test
    fun aChoice_isKeptAcrossLaunches() =
        runTest {
            AppThemeSettings(context).setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, AppThemeSettings(context).observeThemeMode().first())
            assertEquals(ThemeMode.DARK, savedThemeMode(context))
        }

    @Test
    fun nightModes_forTheSystem() {
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, ThemeMode.SYSTEM.uiModeManagerNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_NO, ThemeMode.LIGHT.uiModeManagerNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_YES, ThemeMode.DARK.uiModeManagerNightMode())
    }

    @Test
    fun configurationOverride_keepsTheDeviceTypeAndOnlySetsNight() {
        val phoneInLight = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO
        assertEquals(
            Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES,
            ThemeMode.DARK.overrideUiMode(phoneInLight),
        )
        assertEquals(
            Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO,
            ThemeMode.LIGHT.overrideUiMode(Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES),
        )
        assertNull(ThemeMode.SYSTEM.overrideUiMode(phoneInLight))
    }
}
