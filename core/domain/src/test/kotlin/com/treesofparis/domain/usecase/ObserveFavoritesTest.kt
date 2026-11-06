package com.treesofparis.domain.usecase

import app.cash.turbine.test
import com.treesofparis.domain.fake.FakeFavoritesRepository
import com.treesofparis.domain.fake.FakeTreeRepository
import com.treesofparis.domain.fake.tree
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoritesTest {
    private val trees = FakeTreeRepository(listOf(tree(1), tree(2), tree(3)))

    @Test
    fun skipsIdsMissingFromTreeRepository() =
        runTest {
            val favorites = FakeFavoritesRepository(listOf(3, 99, 1))
            ObserveFavorites(favorites, trees)().test {
                assertEquals(listOf(3L, 1L), awaitItem().map { it.id })
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
                favorites.toggle(2)
                assertEquals(listOf(2L, 1L), awaitItem().map { it.id })
            }
        }
}
