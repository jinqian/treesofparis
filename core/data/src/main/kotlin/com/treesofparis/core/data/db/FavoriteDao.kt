package com.treesofparis.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT tree_id FROM favorites ORDER BY added_at DESC")
    fun observeIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tree_id = :treeId)")
    fun observeIsFavorite(treeId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tree_id = :treeId)")
    suspend fun isFavorite(treeId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE tree_id = :treeId")
    suspend fun delete(treeId: Long)

    @Transaction
    suspend fun toggle(
        treeId: Long,
        now: Long,
    ) {
        if (isFavorite(treeId)) delete(treeId) else insert(FavoriteEntity(treeId, now))
    }
}
