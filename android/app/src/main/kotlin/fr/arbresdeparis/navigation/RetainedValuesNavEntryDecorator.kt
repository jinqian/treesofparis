package fr.arbresdeparis.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.LocalRetainedValuesStore
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Gives each back stack entry its own [LocalRetainedValuesStore]: what a screen keeps with [retain] survives while
 * another tab is shown (the screen leaves the composition but stays in its back stack) and is released when the entry
 * is popped. The map uses it so coming back to Carte does not rebuild the map.
 *
 * The registry is remembered, not retained, and disposed with this composition: retained values (the map's views)
 * never outlive the activity, so a configuration change cannot leak them.
 */
@Composable
fun <T : Any> rememberRetainedValuesNavEntryDecorator(): NavEntryDecorator<T> {
    val registry = remember { RetainedValuesStoreRegistry() }
    DisposableEffect(registry) { onDispose { registry.dispose() } }
    return remember(registry) {
        NavEntryDecorator(onPop = { contentKey -> registry.clearChild(contentKey) }) { entry ->
            registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
        }
    }
}
