package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.toFavoriteSnapshot
import fr.arbresdeparis.domain.repository.FavoritesRepository
import fr.arbresdeparis.domain.repository.TreeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Records what the app knows of each favorite tree still in the data: fills the snapshots of favorites added before
 * snapshots existed, and follows corrections of the data. Run at start-up, so a later data refresh that removes a tree
 * still finds it remembered.
 */
class RefreshFavoriteSnapshots
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke() {
            val stored = favorites.observeFavorites().first().associate { it.treeId to it.snapshot }
            if (stored.isEmpty()) return
            val changed =
                trees
                    .getTreesByIds(stored.keys.toList())
                    .associate { it.id to it.toFavoriteSnapshot() }
                    .filter { (id, snapshot) -> stored[id] != snapshot }
            if (changed.isNotEmpty()) favorites.updateSnapshots(changed)
        }
    }
