package com.treesofparis.core.data.repository

import com.treesofparis.core.data.db.TreeDao
import com.treesofparis.core.data.mapper.toDomain
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.TreeRepository
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
        ): List<Tree> =
            dao
                .inBounds(bounds.south, bounds.west, bounds.north, bounds.east, remarkableOnly, limit)
                .map { it.toDomain() }

        override suspend fun getTree(id: Long): Tree? = dao.byId(id)?.toDomain()

        override suspend fun getTreesByIds(ids: List<Long>): List<Tree> =
            ids.chunked(SQLITE_MAX_VARIABLES).flatMap { chunk -> dao.byIds(chunk) }.map { it.toDomain() }

        private companion object {
            /** SQLite's bound-parameter limit on older Android versions. */
            const val SQLITE_MAX_VARIABLES = 999
        }
    }
