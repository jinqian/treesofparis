package fr.arbresdeparis.domain.repository

import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.model.TreeCluster

interface TreeRepository {
    /** Trees inside [bounds], at most [limit]; only remarkable trees when [remarkableOnly], only [genus] when set. */
    suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
        remarkableOnly: Boolean,
        genus: String?,
    ): List<Tree>

    suspend fun getTree(id: Long): Tree?

    /** Every remarkable tree, in no particular order. */
    suspend fun getRemarkableTrees(): List<Tree>

    /** Returns the trees that exist among [ids], in no particular order. Unknown ids are ignored. */
    suspend fun getTreesByIds(ids: List<Long>): List<Tree>

    /**
     * The trees inside [bounds] counted per cell of a [cellLat] × [cellLon] degree grid anchored at 0°, 0°: one
     * [TreeCluster] per non-empty cell, at the mean position of its trees.
     */
    suspend fun getTreeClusters(
        bounds: GeoBounds,
        cellLat: Double,
        cellLon: Double,
    ): List<TreeCluster>
}
