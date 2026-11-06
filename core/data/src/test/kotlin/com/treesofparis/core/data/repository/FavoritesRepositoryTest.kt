package com.treesofparis.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.treesofparis.core.data.db.UserDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoritesRepositoryTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext<Context>(),
                UserDatabase::class.java,
            ).allowMainThreadQueries()
            .build()
    private var now = 1_000L
    private val repository = FavoritesRepositoryImpl(db.favoriteDao(), clock = { now++ })

    @After
    fun tearDown() = db.close()

    @Test
    fun toggleAddsThenRemoves() =
        runTest {
            repository.observeIsFavorite(5).test {
                assertEquals(false, awaitItem())
                repository.toggle(5)
                assertEquals(true, awaitItem())
                repository.toggle(5)
                assertEquals(false, awaitItem())
            }
        }

    @Test
    fun favorites_orderedByMostRecent() =
        runTest {
            repository.toggle(1)
            repository.toggle(2)
            repository.toggle(3)
            repository.observeFavoriteIds().test {
                assertEquals(listOf(3L, 2L, 1L), awaitItem())
            }
        }
}
