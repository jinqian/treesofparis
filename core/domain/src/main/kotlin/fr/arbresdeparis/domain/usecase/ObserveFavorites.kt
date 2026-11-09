package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.FavoriteTree
import fr.arbresdeparis.domain.repository.FavoritesRepository
import fr.arbresdeparis.domain.repository.TreeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

/**
 * Favorite trees in favorite order. A favorite whose tree is no longer in the data (removed by a data refresh) is kept
 * as [FavoriteTree.Gone], with what the app remembers of it.
 */
class ObserveFavorites
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
        private val trees: TreeRepository,
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        operator fun invoke(): Flow<List<FavoriteTree>> =
            favorites.observeFavorites().mapLatest { favorites ->
                val byId = trees.getTreesByIds(favorites.map { it.treeId }).associateBy { it.id }
                favorites.map { favorite ->
                    byId[favorite.treeId]?.let(FavoriteTree::Present)
                        ?: FavoriteTree.Gone(favorite.treeId, favorite.snapshot)
                }
            }
    }
