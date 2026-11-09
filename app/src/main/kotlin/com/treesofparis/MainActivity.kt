package com.treesofparis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.core.map.LocalMapProvider
import com.treesofparis.core.map.MapProvider
import com.treesofparis.feature.about.AboutRoute
import com.treesofparis.feature.about.FloweringSourcesRoute
import com.treesofparis.feature.about.LibrariesRoute
import com.treesofparis.feature.favorites.FavoritesRoute
import com.treesofparis.feature.herbier.HerbierRoute
import com.treesofparis.feature.herbier.HerbierSectionRoute
import com.treesofparis.feature.map.MapRoute
import dagger.hilt.android.AndroidEntryPoint
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
                        mapContent = { key -> MapRoute(treeId = key.treeId, genus = key.genus) },
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
