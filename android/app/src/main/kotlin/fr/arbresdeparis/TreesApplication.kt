package fr.arbresdeparis

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.arbresdeparis.crash.FirebaseCrashReporting
import fr.arbresdeparis.domain.usecase.RefreshFavoriteSnapshots
import fr.arbresdeparis.theme.applyThemeToSystem
import fr.arbresdeparis.theme.savedThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TreesApplication : Application() {
    @Inject
    lateinit var refreshFavoriteSnapshots: RefreshFavoriteSnapshots

    @Inject
    lateinit var crashReporting: FirebaseCrashReporting

    private val scope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        crashReporting.applyToCrashlytics()
        // Android 12+ keeps the app's night mode: make sure it matches the saved choice (after a restore, say).
        applyThemeToSystem(this, savedThemeMode(this))
        // Remember what each favorite tree is while it is still in the data, so a later data refresh that removes it
        // leaves its name in Favoris. Room runs the queries off the main thread.
        scope.launch { refreshFavoriteSnapshots() }
    }
}
