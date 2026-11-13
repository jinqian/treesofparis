package fr.arbresdeparis

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import java.util.Locale

/** The app is in French only, whatever the phone's language. */
internal val APP_LOCALE: Locale = Locale.FRANCE

/**
 * Gives this activity a French configuration and, when [uiMode] is given, the night mode the user chose (below
 * Android 12, where the system cannot apply it). Every other setting comes from the system (font size, density…).
 * Strings are French anyway; what changes is what reads the configuration: the system location button, which Android
 * draws from the configuration the app hands it (ADR 0006), the library's fallback button on Android 16 and lower,
 * and the map style, which follows the night mode. Call once, from `attachBaseContext`, after `super`.
 */
internal fun ContextThemeWrapper.useAppConfiguration(uiMode: Int? = null) {
    applyOverrideConfiguration(
        Configuration().apply {
            setLocale(APP_LOCALE)
            uiMode?.let { this.uiMode = it }
        },
    )
}
