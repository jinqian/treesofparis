package com.treesofparis.domain.usecase

import com.treesofparis.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavorite
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
    ) {
        suspend operator fun invoke(id: Long) = favorites.toggle(id)
    }
