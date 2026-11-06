package com.treesofparis.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "tree_id") val treeId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)
