package com.treesofparis.core.data.db

import androidx.room.Dao
import androidx.room.Query

/** Aggregate queries over the whole tree table, for the Herbier screen. */
@Dao
internal interface TreeStatisticsDao {
    @Query("SELECT COUNT(*) FROM trees")
    suspend fun count(): Int

    /** Trees per genus, most planted first; trees without a genus are left out. */
    @Query(
        """
        SELECT genus, COUNT(*) AS count FROM trees
        WHERE genus IS NOT NULL AND genus != ''
        GROUP BY genus ORDER BY count DESC, genus
        """,
    )
    suspend fun countByGenus(): List<GenusCountRow>

    /** Distinct genus + species pairs (species and varieties). */
    @Query(
        """
        SELECT COUNT(*) FROM (
          SELECT DISTINCT genus, IFNULL(species, '') FROM trees WHERE genus IS NOT NULL AND genus != ''
        )
        """,
    )
    suspend fun countTaxa(): Int

    @Query("SELECT stage, COUNT(*) AS count FROM trees GROUP BY stage")
    suspend fun countByStage(): List<StageCountRow>

    @Query("SELECT COUNT(*) FROM trees WHERE remarkable = 1")
    suspend fun countRemarkable(): Int

    @Query(
        """
        SELECT genus, COUNT(*) AS count FROM trees
        WHERE remarkable = 1 AND genus IS NOT NULL AND genus != ''
        GROUP BY genus ORDER BY count DESC, genus LIMIT 1
        """,
    )
    suspend fun leadingRemarkableGenus(): GenusCountRow?

    @Query("SELECT * FROM trees WHERE remarkable = 1 AND height_m IS NOT NULL ORDER BY height_m DESC, id LIMIT 1")
    suspend fun tallestRemarkable(): TreeEntity?

    @Query(
        """
        SELECT * FROM trees WHERE remarkable = 1 AND circumference_cm IS NOT NULL
        ORDER BY circumference_cm DESC, id LIMIT 1
        """,
    )
    suspend fun widestRemarkable(): TreeEntity?

    /** Paris arrondissements (« PARIS 15E ARRDT ») with the most trees; woods and suburbs are left out. */
    @Query(
        """
        SELECT arrondissement, COUNT(*) AS count FROM trees
        WHERE arrondissement LIKE 'PARIS %ARRDT'
        GROUP BY arrondissement ORDER BY count DESC, arrondissement LIMIT :limit
        """,
    )
    suspend fun mostWoodedAreas(limit: Int): List<AreaCountRow>
}
