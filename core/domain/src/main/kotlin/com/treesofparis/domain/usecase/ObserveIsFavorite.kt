package com.treesofparis.domain.usecase

import com.treesofparis.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsFavorite
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
    ) {
        operator fun invoke(id: Long): Flow<Boolean> = favorites.observeIsFavorite(id)
    }
