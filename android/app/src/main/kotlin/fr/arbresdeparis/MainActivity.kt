package fr.arbresdeparis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.LocalMapProvider
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.feature.about.AboutRoute
import fr.arbresdeparis.feature.about.FloweringSourcesRoute
import fr.arbresdeparis.feature.about.LibrariesRoute
import fr.arbresdeparis.feature.favorites.FavoritesRoute
import fr.arbresdeparis.feature.herbier.HerbierRoute
import fr.arbresdeparis.feature.herbier.HerbierSectionRoute
import fr.arbresdeparis.feature.map.MapRoute
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mapProvider: MapProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TreesTheme {
                CompositionLocalProvider(LocalMapProvider provides mapProvider) {
                    TreesApp(
                        mapContent = {
                            key,
                            inFront,
                            ->
                            MapRoute(treeId = key.treeId, genus = key.genus, inFront = inFront)
                        },
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
                }
            }
        }
    }
}
