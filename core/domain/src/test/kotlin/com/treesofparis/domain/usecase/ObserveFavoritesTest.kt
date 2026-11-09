package com.treesofparis.domain.usecase

import app.cash.turbine.test
import com.treesofparis.domain.fake.FakeFavoritesRepository
import com.treesofparis.domain.fake.FakeTreeRepository
import com.treesofparis.domain.fake.tree
import com.treesofparis.domain.model.FavoriteTree
import com.treesofparis.domain.model.toFavoriteSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoritesTest {
    private val trees = FakeTreeRepository(listOf(tree(1), tree(2), tree(3)))

    @Test
    fun aTreeMissingFromTheData_isKeptAsGoneWithWhatIsRemembered() =
        runTest {
            val felled = tree(99).copy(commonName = "Marronnier")
            val favorites =
                FakeFavoritesRepository(listOf(3, 99, 1), snapshots = mapOf(99L to felled.toFavoriteSnapshot()))
            ObserveFavorites(favorites, trees)().test {
                val items = awaitItem()
                assertEquals(listOf(3L, 99L, 1L), items.map { it.id })
                assertEquals(FavoriteTree.Gone(99, felled.toFavoriteSnapshot()), items[1])
                assertEquals(FavoriteTree.Present(tree(3)), items[0])
            }
        }

    @Test
    fun aMissingTreeNeverRemembered_isGoneWithoutSnapshot() =
        runTest {
            ObserveFavorites(FakeFavoritesRepository(listOf(99)), trees)().test {
                assertEquals(listOf(FavoriteTree.Gone(99, snapshot = null)), awaitItem())
            }
        }

    @Test
    fun preservesFavoriteOrder() =
        runTest {
            val favorites = FakeFavoritesRepository(listOf(2, 3, 1))
            ObserveFavorites(favorites, trees)().test {
                assertEquals(listOf(2L, 3L, 1L), awaitItem().map { it.id })
            }
        }

    @Test
    fun emitsAgainWhenFavoritesChange() =
        runTest {
            val favorites = FakeFavoritesRepository(listOf(1))
            ObserveFavorites(favorites, trees)().test {
                assertEquals(listOf(1L), awaitItem().map { it.id })
                favorites.toggle(2, snapshot = null)
                assertEquals(listOf(2L, 1L), awaitItem().map { it.id })
            }
        }
}
