package com.treesofparis

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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.treesofparis.navigation.AboutKey
import com.treesofparis.navigation.FavoritesKey
import com.treesofparis.navigation.HerbierKey
import com.treesofparis.navigation.LibrariesKey
import com.treesofparis.navigation.MapKey
import com.treesofparis.navigation.TopLevelDestination
import com.treesofparis.navigation.TreesNavigator
import com.treesofparis.navigation.rememberTreesNavigationState

/**
 * The app shell: bottom bar + one back stack per tab. Screen contents are parameters so navigation can be tested
 * with stubs; [MainActivity] passes the real feature routes.
 */
@Composable
fun TreesApp(
    mapContent: @Composable (MapKey) -> Unit,
    favoritesContent: @Composable (onTreeClick: (Long) -> Unit, onExploreMap: () -> Unit) -> Unit,
    herbierContent: @Composable (onGenusClick: (String) -> Unit) -> Unit,
    aboutContent: @Composable (onOpenLibraries: () -> Unit) -> Unit,
    librariesContent: @Composable (onBack: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTreesNavigationState()
    val navigator = remember(state) { TreesNavigator(state) }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
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
        },
    ) { padding ->
        NavDisplay(
            entries =
                state.toDecoratedEntries(
                    entryProvider {
                        entry<MapKey> { key -> mapContent(key) }
                        entry<HerbierKey> { herbierContent(navigator::openGenusOnMap) }
                        entry<FavoritesKey> {
                            favoritesContent(navigator::openTreeOnMap) { navigator.selectTab(TopLevelDestination.MAP) }
                        }
                        entry<AboutKey> { aboutContent(navigator::openLibraries) }
                        entry<LibrariesKey> { librariesContent(navigator::goBack) }
                    },
                ),
            onBack = navigator::goBack,
            modifier = Modifier.padding(padding),
        )
    }
}
