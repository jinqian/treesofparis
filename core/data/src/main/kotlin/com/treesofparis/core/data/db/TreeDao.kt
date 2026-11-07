package com.treesofparis.core.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TreeDao {
    /**
     * Trees inside the box, only remarkable ones when [remarkableOnly]. When there are more than [limit], the ones
     * closest to the box centre win.
     */
    @Query(
        """
        SELECT * FROM trees
        WHERE lat BETWEEN :south AND :north AND lon BETWEEN :west AND :east
          AND (:remarkableOnly = 0 OR remarkable = 1)
        ORDER BY (lat - (:south + :north) / 2) * (lat - (:south + :north) / 2)
               + (lon - (:west + :east) / 2) * (lon - (:west + :east) / 2)
        LIMIT :limit
        """,
    )
    suspend fun inBounds(
        south: Double,
        west: Double,
        north: Double,
        east: Double,
        remarkableOnly: Boolean,
        limit: Int,
    ): List<TreeEntity>

    @Query("SELECT * FROM trees WHERE id = :id")
    suspend fun byId(id: Long): TreeEntity?

    @Query("SELECT * FROM trees WHERE id IN (:ids)")
    suspend fun byIds(ids: List<Long>): List<TreeEntity>
}
