package fr.arbresdeparis.domain.model

/** A favorite as stored: the tree's id and what the app remembers of the tree, when it has been recorded. */
data class Favorite(
    val treeId: Long,
    val snapshot: FavoriteSnapshot?,
)

/**
 * What the app remembers of a favorite tree, so it can still name it after a data refresh removed the tree (felled or
 * replaced trees leave the City's dataset).
 */
data class FavoriteSnapshot(
    val commonName: String,
    val address: String,
    val arrondissement: String,
    val remarkable: Boolean,
    val location: GeoPoint,
)

fun Tree.toFavoriteSnapshot() =
    FavoriteSnapshot(
        commonName = commonName,
        address = address,
        arrondissement = arrondissement,
        remarkable = remarkable,
        location = location,
    )

/** A favorite as shown: its tree, or what is remembered of a tree that is no longer in the data. */
sealed interface FavoriteTree {
    val id: Long

    data class Present(
        val tree: Tree,
    ) : FavoriteTree {
        override val id: Long get() = tree.id
    }

    /** [snapshot] is null for a favorite added before snapshots existed and removed before one was recorded. */
    data class Gone(
        override val id: Long,
        val snapshot: FavoriteSnapshot?,
    ) : FavoriteTree
}
