package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.fake.FakeFavoritesRepository
import fr.arbresdeparis.domain.fake.FakeTreeRepository
import fr.arbresdeparis.domain.fake.tree
import fr.arbresdeparis.domain.model.toFavoriteSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshFavoriteSnapshotsTest {
    private val plane = tree(1)
    private val renamed = tree(2).copy(commonName = "Tilleul argenté")
    private val trees = FakeTreeRepository(listOf(plane, renamed))

    @Test
    fun fillsMissingSnapshotsAndFollowsCorrections_keepsThoseOfRemovedTrees() =
        runTest {
            val felled = tree(99).toFavoriteSnapshot()
            val favorites =
                FakeFavoritesRepository(
                    listOf(1, 2, 99),
                    snapshots = mapOf(2L to tree(2).copy(commonName = "Tilleul").toFavoriteSnapshot(), 99L to felled),
                )

            RefreshFavoriteSnapshots(favorites, trees)()

            assertEquals(
                mapOf(1L to plane.toFavoriteSnapshot(), 2L to renamed.toFavoriteSnapshot(), 99L to felled),
                favorites.snapshots.value,
            )
        }

    @Test
    fun upToDateSnapshots_writeNothing() =
        runTest {
            val favorites = FakeFavoritesRepository(listOf(1), snapshots = mapOf(1L to plane.toFavoriteSnapshot()))
            RefreshFavoriteSnapshots(favorites, trees)()
            assertEquals(0, favorites.snapshotUpdates)
        }
}
