package fr.arbresdeparis.theme

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The theme chosen in À propos, kept in preferences and applied to the whole app, not only to `TreesTheme`: the map
 * style, the system bars and the system location button read the configuration's night mode. Android 12+ keeps the
 * app's night mode itself ([applyThemeToSystem]), so the splash screen matches and the activity is recreated on a
 * change; older versions get it through the activity's configuration (see `MainActivity`).
 */
@Singleton
class AppThemeSettings
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ThemeRepository {
        private val mode = MutableStateFlow(savedThemeMode(context))

        override fun observeThemeMode(): Flow<ThemeMode> = mode.asStateFlow()

        override suspend fun setThemeMode(mode: ThemeMode) {
            context.themePreferences().edit { putString(KEY_MODE, mode.name) }
            this.mode.value = mode
            applyThemeToSystem(context, mode)
        }
    }

/** The saved choice, readable before injection (the activity needs it in `attachBaseContext`). */
internal fun savedThemeMode(context: Context): ThemeMode =
    context
        .themePreferences()
        .getString(KEY_MODE, null)
        ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
        ?: ThemeMode.SYSTEM

/** Android 12+: the system keeps this app's night mode and applies it from the splash screen on. */
internal fun applyThemeToSystem(
    context: Context,
    mode: ThemeMode,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    context.getSystemService(UiModeManager::class.java)?.setApplicationNightMode(mode.uiModeManagerNightMode())
}

/** Whether the system applies the theme itself; below Android 12 the activity overrides its configuration. */
internal val systemAppliesTheme: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

internal fun ThemeMode.uiModeManagerNightMode(): Int =
    when (this) {
        ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
    }

/** [baseUiMode] with its night bits set to this choice, keeping the device type; null to follow the phone. */
internal fun ThemeMode.overrideUiMode(baseUiMode: Int): Int? {
    val night =
        when (this) {
            ThemeMode.SYSTEM -> return null
            ThemeMode.LIGHT -> Configuration.UI_MODE_NIGHT_NO
            ThemeMode.DARK -> Configuration.UI_MODE_NIGHT_YES
        }
    return (baseUiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
}

private fun Context.themePreferences() = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

private const val PREFERENCES = "appearance"
private const val KEY_MODE = "theme_mode"
