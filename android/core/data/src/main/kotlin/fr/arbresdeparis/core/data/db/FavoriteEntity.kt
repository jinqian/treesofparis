package fr.arbresdeparis.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A favorite tree. [snapshot] is null until what the tree is has been recorded. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "tree_id") val treeId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @Embedded(prefix = "snapshot_") val snapshot: FavoriteSnapshotEntity? = null,
)

/** What the app remembers of a favorite tree, to name it after a data refresh removed it. */
data class FavoriteSnapshotEntity(
    @ColumnInfo(name = "common_name") val commonName: String,
    val address: String,
    val arrondissement: String,
    val remarkable: Boolean,
    val lat: Double,
    val lon: Double,
)
