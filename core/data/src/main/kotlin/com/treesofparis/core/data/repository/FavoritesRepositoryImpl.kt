package com.treesofparis.core.data.repository

import com.treesofparis.core.data.db.FavoriteDao
import com.treesofparis.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
    private val clock: () -> Long,
) : FavoritesRepository {
    override fun observeFavoriteIds(): Flow<List<Long>> = dao.observeIds().distinctUntilChanged()

    override fun observeIsFavorite(id: Long): Flow<Boolean> = dao.observeIsFavorite(id).distinctUntilChanged()

    override suspend fun toggle(id: Long) = dao.toggle(id, clock())
}
