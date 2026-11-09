package com.treesofparis.domain.repository

import com.treesofparis.domain.model.Favorite
import com.treesofparis.domain.model.FavoriteSnapshot
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Favorites, most recently added first. */
    fun observeFavorites(): Flow<List<Favorite>>

    fun observeIsFavorite(id: Long): Flow<Boolean>

    /** Adds [id] with its [snapshot], or removes it when it is already a favorite. */
    suspend fun toggle(
        id: Long,
        snapshot: FavoriteSnapshot?,
    )

    /** Replaces the snapshots of these favorites; ids that are not favorites are ignored. */
    suspend fun updateSnapshots(snapshots: Map<Long, FavoriteSnapshot>)
}
