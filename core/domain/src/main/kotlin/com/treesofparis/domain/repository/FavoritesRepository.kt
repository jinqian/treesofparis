package com.treesofparis.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Favorite tree ids, most recently added first. */
    fun observeFavoriteIds(): Flow<List<Long>>

    fun observeIsFavorite(id: Long): Flow<Boolean>

    suspend fun toggle(id: Long)
}
