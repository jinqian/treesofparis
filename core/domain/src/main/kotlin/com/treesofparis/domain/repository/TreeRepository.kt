package com.treesofparis.domain.repository

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.Tree

interface TreeRepository {
    suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
    ): List<Tree>

    suspend fun getTree(id: Long): Tree?

    /** Returns the trees that exist among [ids], in no particular order. Unknown ids are ignored. */
    suspend fun getTreesByIds(ids: List<Long>): List<Tree>
}
