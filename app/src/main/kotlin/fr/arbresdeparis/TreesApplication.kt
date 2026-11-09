package fr.arbresdeparis

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.arbresdeparis.domain.usecase.RefreshFavoriteSnapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TreesApplication : Application() {
    @Inject
    lateinit var refreshFavoriteSnapshots: RefreshFavoriteSnapshots

    private val scope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Remember what each favorite tree is while it is still in the data, so a later data refresh that removes it
        // leaves its name in Favoris. Room runs the queries off the main thread.
        scope.launch { refreshFavoriteSnapshots() }
    }
}
