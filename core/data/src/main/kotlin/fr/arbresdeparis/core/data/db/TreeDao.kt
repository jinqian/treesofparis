package fr.arbresdeparis.core.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TreeDao {
    /**
     * Trees inside the box, only remarkable ones when [remarkableOnly], only one [genus] when set. When there are more
     * than [limit], the ones closest to the box centre win.
     */
    @Query(
        """
        SELECT * FROM trees
        WHERE lat BETWEEN :south AND :north AND lon BETWEEN :west AND :east
          AND (:remarkableOnly = 0 OR remarkable = 1)
          AND (:genus IS NULL OR genus = :genus)
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
        genus: String?,
        limit: Int,
    ): List<TreeEntity>

    @Query("SELECT * FROM trees WHERE id = :id")
    suspend fun byId(id: Long): TreeEntity?

    /** The remarkable trees: a couple of hundred, so all of them. */
    @Query("SELECT * FROM trees WHERE remarkable = 1")
    suspend fun remarkable(): List<TreeEntity>

    @Query("SELECT * FROM trees WHERE id IN (:ids)")
    suspend fun byIds(ids: List<Long>): List<TreeEntity>

    /** The Wikipedia link for a tree's genus and species ([species] empty when the tree has none). */
    @Query("SELECT * FROM taxon_links WHERE genus = :genus AND species = :species")
    suspend fun taxonLink(
        genus: String,
        species: String,
    ): TaxonLinkEntity?
}
