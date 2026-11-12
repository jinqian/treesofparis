package fr.arbresdeparis.core.data.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Aggregate queries for the Herbier screen, over the Paris data only (`source = 'paris'`): its figures and wording
 * are about Paris. The other towns' trees are on the map.
 */
@Dao
internal interface TreeStatisticsDao {
    @Query("SELECT COUNT(*) FROM trees WHERE source = 'paris'")
    suspend fun count(): Int

    /** Trees per genus, most planted first; trees without a genus are left out. */
    @Query(
        """
        SELECT genus, COUNT(*) AS count FROM trees
        WHERE source = 'paris' AND genus IS NOT NULL AND genus != ''
        GROUP BY genus ORDER BY count DESC, genus
        """,
    )
    suspend fun countByGenus(): List<GenusCountRow>

    /** Trees per genus and French name (a few hundred rows), with their remarkable trees; no missing genus. */
    @Query(
        """
        SELECT genus, common_name, COUNT(*) AS count, SUM(remarkable) AS remarkable_count FROM trees
        WHERE source = 'paris' AND genus IS NOT NULL AND genus != ''
        GROUP BY genus, common_name
        """,
    )
    suspend fun countByGenusAndName(): List<GenusNameCountRow>

    /** Distinct genus + species pairs (species and varieties). */
    @Query(
        """
        SELECT COUNT(*) FROM (
          SELECT DISTINCT genus, IFNULL(species, '') FROM trees WHERE source = 'paris' AND genus IS NOT NULL AND genus != ''
        )
        """,
    )
    suspend fun countTaxa(): Int

    @Query("SELECT stage, COUNT(*) AS count FROM trees WHERE source = 'paris' GROUP BY stage")
    suspend fun countByStage(): List<StageCountRow>

    @Query("SELECT COUNT(*) FROM trees WHERE source = 'paris' AND remarkable = 1")
    suspend fun countRemarkable(): Int

    @Query(
        """
        SELECT genus, COUNT(*) AS count FROM trees
        WHERE source = 'paris' AND remarkable = 1 AND genus IS NOT NULL AND genus != ''
        GROUP BY genus ORDER BY count DESC, genus LIMIT 1
        """,
    )
    suspend fun leadingRemarkableGenus(): GenusCountRow?

    @Query(
        """
        SELECT * FROM trees WHERE source = 'paris' AND remarkable = 1 AND height_m IS NOT NULL
        ORDER BY height_m DESC, id LIMIT 1
        """,
    )
    suspend fun tallestRemarkable(): TreeEntity?

    @Query(
        """
        SELECT * FROM trees WHERE source = 'paris' AND remarkable = 1 AND circumference_cm IS NOT NULL
        ORDER BY circumference_cm DESC, id LIMIT 1
        """,
    )
    suspend fun widestRemarkable(): TreeEntity?

    /** Paris arrondissements (« PARIS 15E ARRDT ») with the most trees; woods and suburbs are left out. */
    @Query(
        """
        SELECT arrondissement, COUNT(*) AS count FROM trees
        WHERE source = 'paris' AND arrondissement LIKE 'PARIS %ARRDT'
        GROUP BY arrondissement ORDER BY count DESC, arrondissement LIMIT :limit
        """,
    )
    suspend fun mostWoodedAreas(limit: Int): List<AreaCountRow>
}
