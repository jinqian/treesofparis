package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeFavoritesRepository
import com.treesofparis.domain.fake.FakeTreeRepository
import com.treesofparis.domain.fake.tree
import com.treesofparis.domain.model.toFavoriteSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteTest {
    private val plane = tree(7)
    private val favorites = FakeFavoritesRepository()
    private val toggle = ToggleFavorite(favorites, FakeTreeRepository(listOf(plane)))

    @Test
    fun addsThenRemoves() =
        runTest {
            toggle(7)
            assertEquals(listOf(7L), favorites.ids.value)
            toggle(7)
            assertEquals(emptyList<Long>(), favorites.ids.value)
        }

    @Test
    fun adding_remembersTheTree() =
        runTest {
            toggle(7)
            assertEquals(mapOf(7L to plane.toFavoriteSnapshot()), favorites.snapshots.value)
        }
}
