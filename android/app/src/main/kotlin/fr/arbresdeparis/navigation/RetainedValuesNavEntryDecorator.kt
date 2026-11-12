package fr.arbresdeparis.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.LocalRetainedValuesStore
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Gives each back stack entry its own [LocalRetainedValuesStore]: what a screen keeps with [retain] survives while
 * another tab is shown (the screen leaves the composition but stays in its back stack) and is released when the entry
 * is popped. The map uses it so coming back to Carte does not rebuild the map.
 */
@Composable
fun <T : Any> rememberRetainedValuesNavEntryDecorator(): NavEntryDecorator<T> {
    val registry = retainRetainedValuesStoreRegistry()
    return remember(registry) {
        NavEntryDecorator(onPop = { contentKey -> registry.clearChild(contentKey) }) { entry ->
            registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
        }
    }
}
