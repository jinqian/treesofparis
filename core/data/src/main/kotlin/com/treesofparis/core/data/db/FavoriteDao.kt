package com.treesofparis.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY added_at DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tree_id = :treeId)")
    fun observeIsFavorite(treeId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tree_id = :treeId)")
    suspend fun isFavorite(treeId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE tree_id = :treeId")
    suspend fun delete(treeId: Long)

    @Query("SELECT * FROM favorites WHERE tree_id IN (:treeIds)")
    suspend fun byIds(treeIds: List<Long>): List<FavoriteEntity>

    @Update
    suspend fun update(favorites: List<FavoriteEntity>)

    @Transaction
    suspend fun toggle(
        treeId: Long,
        now: Long,
        snapshot: FavoriteSnapshotEntity?,
    ) {
        if (isFavorite(treeId)) delete(treeId) else insert(FavoriteEntity(treeId, now, snapshot))
    }

    @Transaction
    suspend fun updateSnapshots(snapshots: Map<Long, FavoriteSnapshotEntity>) {
        update(byIds(snapshots.keys.toList()).map { it.copy(snapshot = snapshots[it.treeId]) })
    }
}
