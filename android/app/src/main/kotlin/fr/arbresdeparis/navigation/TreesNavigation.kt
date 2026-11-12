package fr.arbresdeparis.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import fr.arbresdeparis.feature.herbier.HerbierSection

/**
 * Navigation state: one back stack per tab (saved across configuration changes and process death) and the
 * selected tab. Follows "exit through home": the Carte stack is always below the selected tab's stack.
 */
class TreesNavigationState(
    val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
    private val selectedName: () -> String,
    private val select: (String) -> Unit,
) {
    var selected: TopLevelDestination
        get() = TopLevelDestination.valueOf(selectedName())
        set(value) = select(value.name)

    val start = TopLevelDestination.MAP

    /** The stacks shown by NavDisplay: Carte, plus the selected tab when it is another one. */
    val visibleTabs: List<TopLevelDestination>
        get() = if (selected == start) listOf(start) else listOf(start, selected)

    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decorated =
            backStacks.mapValues { (_, stack) ->
                rememberDecoratedNavEntries(
                    backStack = stack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberRetainedValuesNavEntryDecorator(),
                        ),
                    entryProvider = entryProvider,
                )
            }
        return visibleTabs.flatMap { decorated[it].orEmpty() }
    }
}

@Composable
fun rememberTreesNavigationState(): TreesNavigationState {
    var selectedName by rememberSaveable { mutableStateOf(TopLevelDestination.MAP.name) }
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.startKey) }
    return remember(backStacks) { TreesNavigationState(backStacks, { selectedName }, { selectedName = it }) }
}

/** Every navigation action of the app; the only code that changes [TreesNavigationState]. */
class TreesNavigator(
    private val state: TreesNavigationState,
) {
    fun selectTab(tab: TopLevelDestination) {
        state.selected = tab
    }

    /** Shows [treeId] on the map: the Carte stack restarts at a new map entry for that tree. */
    fun openTreeOnMap(treeId: Long) {
        state.backStacks.getValue(TopLevelDestination.MAP).apply {
            clear()
            add(MapKey(treeId = treeId, request = System.currentTimeMillis()))
        }
        state.selected = TopLevelDestination.MAP
    }

    /** Opens a level-2 Herbier screen on top of the Herbier home. */
    fun openHerbierSection(section: HerbierSection) {
        state.backStacks.getValue(TopLevelDestination.HERBIER).apply {
            val key = HerbierSectionKey(section)
            if (lastOrNull() != key) add(key)
        }
        state.selected = TopLevelDestination.HERBIER
    }

    /** Shows the open source libraries on top of À propos, so back returns there. */
    fun openLibraries() = openOnAbout(LibrariesKey)

    /** Shows the flowering sources on top of À propos, so back returns there. */
    fun openFloweringSources() = openOnAbout(FloweringSourcesKey)

    private fun openOnAbout(key: NavKey) {
        state.backStacks.getValue(TopLevelDestination.ABOUT).apply {
            if (lastOrNull() != key) add(key)
        }
        state.selected = TopLevelDestination.ABOUT
    }

    /** Shows only [genus] on the map (from Herbier): the Carte stack restarts at a new map entry for it. */
    fun openGenusOnMap(genus: String) {
        state.backStacks.getValue(TopLevelDestination.MAP).apply {
            clear()
            add(MapKey(genus = genus, request = System.currentTimeMillis()))
        }
        state.selected = TopLevelDestination.MAP
    }

    fun goBack() {
        val stack = state.backStacks.getValue(state.selected)
        when {
            stack.size > 1 -> stack.removeAt(stack.lastIndex)
            state.selected != state.start -> state.selected = state.start
        }
    }
}
