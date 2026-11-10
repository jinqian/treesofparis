package fr.arbresdeparis.core.data.repository

import fr.arbresdeparis.core.data.db.TreeDao
import fr.arbresdeparis.core.data.mapper.toDomain
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.model.TreeCluster
import fr.arbresdeparis.domain.repository.TreeRepository
import javax.inject.Inject

internal class TreeRepositoryImpl
    @Inject
    constructor(
        private val dao: TreeDao,
    ) : TreeRepository {
        override suspend fun getTreesInBounds(
            bounds: GeoBounds,
            limit: Int,
            remarkableOnly: Boolean,
            genus: String?,
        ): List<Tree> =
            dao
                .inBounds(bounds.south, bounds.west, bounds.north, bounds.east, remarkableOnly, genus, limit)
                .map { it.toDomain() }

        override suspend fun getTree(id: Long): Tree? {
            val tree = dao.byId(id) ?: return null
            val link = tree.genus?.let { genus -> dao.taxonLink(genus, tree.species.orEmpty()) }
            return tree.toDomain(wikipedia = link?.toDomain())
        }

        override suspend fun getRemarkableTrees(): List<Tree> = dao.remarkable().map { it.toDomain() }

        override suspend fun getTreesByIds(ids: List<Long>): List<Tree> =
            ids.chunked(SQLITE_MAX_VARIABLES).flatMap { chunk -> dao.byIds(chunk) }.map { it.toDomain() }

        override suspend fun getTreeClusters(
            bounds: GeoBounds,
            cellLat: Double,
            cellLon: Double,
        ): List<TreeCluster> =
            dao
                .clusters(bounds.south, bounds.west, bounds.north, bounds.east, cellLat, cellLon)
                .map { TreeCluster(GeoPoint(it.lat, it.lon), it.count) }

        private companion object {
            /** SQLite's bound-parameter limit on older Android versions. */
            const val SQLITE_MAX_VARIABLES = 999
        }
    }
