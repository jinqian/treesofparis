package fr.arbresdeparis

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.LocalMapProvider
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.feature.about.AboutRoute
import fr.arbresdeparis.feature.about.CrashReportingQuestionRoute
import fr.arbresdeparis.feature.about.FloweringSourcesRoute
import fr.arbresdeparis.feature.about.LibrariesRoute
import fr.arbresdeparis.feature.favorites.FavoritesRoute
import fr.arbresdeparis.feature.herbier.HerbierRoute
import fr.arbresdeparis.feature.herbier.HerbierSectionRoute
import fr.arbresdeparis.feature.map.MapRoute
import fr.arbresdeparis.feature.map.RemarkableStoryRoute
import fr.arbresdeparis.theme.AppThemeSettings
import fr.arbresdeparis.theme.overrideUiMode
import fr.arbresdeparis.theme.savedThemeMode
import fr.arbresdeparis.theme.systemAppliesTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mapProvider: MapProvider

    @Inject
    lateinit var themeSettings: AppThemeSettings

    /** The theme this activity was created with; below Android 12, another choice recreates it. */
    private var appliedTheme = ThemeMode.SYSTEM

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        appliedTheme = savedThemeMode(newBase)
        val uiMode =
            if (systemAppliesTheme) {
                null
            } else {
                appliedTheme.overrideUiMode(
                    newBase.resources.configuration.uiMode,
                )
            }
        useAppConfiguration(uiMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (!systemAppliesTheme) recreateOnThemeChange()
        setContent {
            TreesTheme {
                CompositionLocalProvider(LocalMapProvider provides mapProvider) {
                    TreesApp(
                        mapContent = { key, inFront, onOpenStory ->
                            MapRoute(
                                treeId = key.treeId,
                                genus = key.genus,
                                inFront = inFront,
                                onOpenStory = onOpenStory,
                            )
                        },
                        remarkableStoryContent = { treeId, onBack -> RemarkableStoryRoute(treeId, onBack = onBack) },
                        favoritesContent = { onTreeClick, onExploreMap ->
                            FavoritesRoute(onTreeClick = onTreeClick, onExploreMap = onExploreMap)
                        },
                        herbierContent = { onOpenSection -> HerbierRoute(onOpenSection = onOpenSection) },
                        herbierSectionContent = { section, onBack, onGenusClick, onTreeClick ->
                            HerbierSectionRoute(
                                section = section,
                                onBack = onBack,
                                onGenusClick = onGenusClick,
                                onTreeClick = onTreeClick,
                            )
                        },
                        aboutContent = { onOpenLibraries, onOpenFloweringSources ->
                            AboutRoute(
                                onOpenLibraries = onOpenLibraries,
                                onOpenFloweringSources = onOpenFloweringSources,
                            )
                        },
                        librariesContent = { onBack ->
                            LibrariesRoute(librariesResId = R.raw.aboutlibraries, onBack = onBack)
                        },
                        floweringSourcesContent = { onBack -> FloweringSourcesRoute(onBack = onBack) },
                    )
                    // Asked once, over whatever screen opens first (ADR 0007).
                    CrashReportingQuestionRoute()
                }
            }
        }
    }

    private fun recreateOnThemeChange() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeSettings.observeThemeMode().collect { if (it != appliedTheme) recreate() }
            }
        }
    }
}
