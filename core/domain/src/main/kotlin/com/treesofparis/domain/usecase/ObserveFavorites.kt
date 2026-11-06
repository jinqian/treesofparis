package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.FavoritesRepository
import com.treesofparis.domain.repository.TreeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

/**
 * Favorite trees in favorite order. Favorites whose tree no longer exists (removed by a data refresh)
 * are skipped.
 */
class ObserveFavorites
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
        private val trees: TreeRepository,
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        operator fun invoke(): Flow<List<Tree>> =
            favorites.observeFavoriteIds().mapLatest { ids ->
                val byId = trees.getTreesByIds(ids).associateBy { it.id }
                ids.mapNotNull { byId[it] }
            }
    }
