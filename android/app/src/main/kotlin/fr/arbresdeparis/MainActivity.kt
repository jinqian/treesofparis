package fr.arbresdeparis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.LocalMapProvider
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.model.TreeLink
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

    /** A tree link (`arbresdeparis.fr/arbre/…`, issue #88) the app was opened with, until the map shows it. */
    private var sharedTree by mutableStateOf<TreeLink?>(null)

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
        // A recreated activity has already shown its link.
        if (savedInstanceState == null) sharedTree = intent.treeLink()
        setContent {
            TreesTheme {
                CompositionLocalProvider(LocalMapProvider provides mapProvider) {
                    TreesApp(
                        mapContent = { key, inFront, onOpenStory ->
                            MapRoute(
                                treeId = key.treeId,
                                fallback = key.fallback,
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
                        sharedTree = sharedTree,
                        onSharedTreeOpen = { sharedTree = null },
                    )
                    // Asked once, over whatever screen opens first (ADR 0007).
                    CrashReportingQuestionRoute()
                }
            }
        }
    }

    /** A link opened while the app is running (`singleTop`): the same activity shows it. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.treeLink()?.let { sharedTree = it }
    }

    private fun Intent.treeLink(): TreeLink? =
        data
            ?.takeIf {
                action == Intent.ACTION_VIEW
            }?.toString()
            ?.let(TreeLink::parse)

    private fun recreateOnThemeChange() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeSettings.observeThemeMode().collect { if (it != appliedTheme) recreate() }
            }
        }
    }
}
