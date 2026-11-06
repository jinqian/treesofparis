package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeFavoritesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteTest {
    @Test
    fun delegatesToRepository() =
        runTest {
            val favorites = FakeFavoritesRepository()
            val toggle = ToggleFavorite(favorites)

            toggle(7)
            assertEquals(listOf(7L), favorites.ids.value)
            toggle(7)
            assertEquals(emptyList<Long>(), favorites.ids.value)
        }
}
