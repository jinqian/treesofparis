package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.toFavoriteSnapshot
import fr.arbresdeparis.domain.repository.FavoritesRepository
import fr.arbresdeparis.domain.repository.TreeRepository
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
