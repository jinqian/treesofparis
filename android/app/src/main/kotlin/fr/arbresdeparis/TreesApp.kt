package fr.arbresdeparis

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import fr.arbresdeparis.feature.herbier.HerbierSection
import fr.arbresdeparis.navigation.AboutKey
import fr.arbresdeparis.navigation.FavoritesKey
import fr.arbresdeparis.navigation.FloweringSourcesKey
import fr.arbresdeparis.navigation.HerbierKey
import fr.arbresdeparis.navigation.HerbierSectionKey
import fr.arbresdeparis.navigation.LibrariesKey
import fr.arbresdeparis.navigation.MapKey
import fr.arbresdeparis.navigation.TopLevelDestination
import fr.arbresdeparis.navigation.TreesNavigationState
import fr.arbresdeparis.navigation.TreesNavigator
import fr.arbresdeparis.navigation.rememberTreesNavigationState

/**
 * The app shell: bottom bar + one back stack per tab. Screen contents are parameters so navigation can be tested
 * with stubs; [MainActivity] passes the real feature routes.
 */
@Composable
fun TreesApp(
    mapContent: @Composable (key: MapKey, inFront: Boolean) -> Unit,
    favoritesContent: @Composable (onTreeClick: (Long) -> Unit, onExploreMap: () -> Unit) -> Unit,
    herbierContent: @Composable (onOpenSection: (HerbierSection) -> Unit) -> Unit,
    herbierSectionContent: @Composable (
        section: HerbierSection,
        onBack: () -> Unit,
        onGenusClick: (String) -> Unit,
        onTreeClick: (Long) -> Unit,
    ) -> Unit,
    aboutContent: @Composable (onOpenLibraries: () -> Unit, onOpenFloweringSources: () -> Unit) -> Unit,
    librariesContent: @Composable (onBack: () -> Unit) -> Unit,
    floweringSourcesContent: @Composable (onBack: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTreesNavigationState()
    val navigator = remember(state) { TreesNavigator(state) }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { TreesBottomBar(state, navigator) },
    ) { padding ->
        NavDisplay(
            entries =
                state.toDecoratedEntries(
                    entryProvider {
                        entry<MapKey>(metadata = tabOf(TopLevelDestination.MAP)) { key ->
                            mapContent(key, isSettledInFront())
                        }
                        entry<HerbierKey>(metadata = tabOf(TopLevelDestination.HERBIER)) {
                            herbierContent(navigator::openHerbierSection)
                        }
                        entry<HerbierSectionKey>(metadata = tabOf(TopLevelDestination.HERBIER)) { key ->
                            herbierSectionContent(
                                key.section,
                                navigator::goBack,
                                navigator::openGenusOnMap,
                                navigator::openTreeOnMap,
                            )
                        }
                        entry<FavoritesKey>(metadata = tabOf(TopLevelDestination.FAVORITES)) {
                            favoritesContent(navigator::openTreeOnMap) { navigator.selectTab(TopLevelDestination.MAP) }
                        }
                        entry<AboutKey>(metadata = tabOf(TopLevelDestination.ABOUT)) {
                            aboutContent(navigator::openLibraries, navigator::openFloweringSources)
                        }
                        entry<LibrariesKey>(metadata = tabOf(TopLevelDestination.ABOUT)) {
                            librariesContent(navigator::goBack)
                        }
                        entry<FloweringSourcesKey>(metadata = tabOf(TopLevelDestination.ABOUT)) {
                            floweringSourcesContent(navigator::goBack)
                        }
                    },
                ),
            onBack = navigator::goBack,
            modifier = Modifier.padding(padding),
            transitionSpec = { if (isTabSwitch()) NO_TRANSITION else defaultTransitionSpec<NavKey>()(this) },
            popTransitionSpec = { if (isTabSwitch()) NO_TRANSITION else defaultPopTransitionSpec<NavKey>()(this) },
            predictivePopTransitionSpec = { edge ->
                if (isTabSwitch()) NO_TRANSITION else defaultPredictivePopTransitionSpec<NavKey>()(this, edge)
            },
        )
    }
}

/** Which tab an entry belongs to, read back by [isTabSwitch]. */
private const val TAB_METADATA = "fr.arbresdeparis.tab"

private fun tabOf(tab: TopLevelDestination): Map<String, Any> = mapOf(TAB_METADATA to tab)

/**
 * Switching tabs is immediate. A cross-fade would also leave the map's system location buttons on screen: Android 17
 * draws them on a layer above the whole window, which Compose cannot fade (ADR 0006). Screens inside a tab keep the
 * default transitions.
 */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.isTabSwitch(): Boolean =
    initialState.entries
        .lastOrNull()
        ?.metadata
        ?.get(TAB_METADATA) !=
        targetState.entries
            .lastOrNull()
            ?.metadata
            ?.get(TAB_METADATA)

private val NO_TRANSITION = ContentTransform(EnterTransition.None, ExitTransition.None)

/** True once this entry has finished appearing and is not on its way out. */
@Composable
private fun isSettledInFront(): Boolean {
    val transition = LocalNavAnimatedContentScope.current.transition
    return transition.currentState == EnterExitState.Visible && transition.targetState == EnterExitState.Visible
}

/** The four tabs, the selected one in a sage pill. */
@Composable
private fun TreesBottomBar(
    state: TreesNavigationState,
    navigator: TreesNavigator,
) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 0.dp) {
            TopLevelDestination.entries.forEach { tab ->
                val selected = tab == state.selected
                NavigationBarItem(
                    selected = selected,
                    onClick = { navigator.selectTab(tab) },
                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                    label = {
                        Text(
                            text = stringResource(tab.label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    },
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
        }
    }
}
