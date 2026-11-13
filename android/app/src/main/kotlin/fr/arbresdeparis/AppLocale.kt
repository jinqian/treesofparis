package fr.arbresdeparis

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import java.util.Locale

/** The app is in French only, whatever the phone's language. */
internal val APP_LOCALE: Locale = Locale.FRANCE

/**
 * Gives this activity a French configuration and keeps every other setting from the system (dark mode, font size…).
 * Its strings are French anyway; what changes is what reads the configuration: the system location button, which
 * Android draws from the configuration the app hands it (ADR 0006), and the library's fallback button on Android 16
 * and lower. Call from `attachBaseContext`, after `super`.
 */
internal fun ContextThemeWrapper.useAppLocale() {
    applyOverrideConfiguration(Configuration().apply { setLocale(APP_LOCALE) })
}
