package fr.arbresdeparis.core.data.repository

import fr.arbresdeparis.core.data.db.FavoriteDao
import fr.arbresdeparis.core.data.mapper.toDomain
import fr.arbresdeparis.core.data.mapper.toEntity
import fr.arbresdeparis.domain.model.Favorite
import fr.arbresdeparis.domain.model.FavoriteSnapshot
import fr.arbresdeparis.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
    private val clock: () -> Long,
) : FavoritesRepository {
    override fun observeFavorites(): Flow<List<Favorite>> =
        dao.observeAll().map { favorites -> favorites.map { it.toDomain() } }.distinctUntilChanged()

    override fun observeIsFavorite(id: Long): Flow<Boolean> = dao.observeIsFavorite(id).distinctUntilChanged()

    override suspend fun toggle(
        id: Long,
        snapshot: FavoriteSnapshot?,
    ) = dao.toggle(id, clock(), snapshot?.toEntity())

    override suspend fun updateSnapshots(snapshots: Map<Long, FavoriteSnapshot>) =
        dao.updateSnapshots(snapshots.mapValues { it.value.toEntity() })
}
