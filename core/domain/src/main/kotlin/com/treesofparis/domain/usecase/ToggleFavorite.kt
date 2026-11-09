package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.toFavoriteSnapshot
import com.treesofparis.domain.repository.FavoritesRepository
import com.treesofparis.domain.repository.TreeRepository
import javax.inject.Inject

/** Adds a tree to the favorites, remembering what it is, or removes it. */
class ToggleFavorite
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(id: Long) = favorites.toggle(id, trees.getTree(id)?.toFavoriteSnapshot())
    }
