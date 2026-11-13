package fr.arbresdeparis.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RemarkableStoryDao {
    @Query("SELECT * FROM remarkable_trees WHERE tree_id = :treeId")
    suspend fun byTreeId(treeId: Long): RemarkableTreeEntity?

    @Query("SELECT * FROM remarkable_trees")
    suspend fun all(): List<RemarkableTreeEntity>

    /** Tests only: the shipped data comes from the asset. */
    @Insert
    suspend fun insert(vararg stories: RemarkableTreeEntity)
}
