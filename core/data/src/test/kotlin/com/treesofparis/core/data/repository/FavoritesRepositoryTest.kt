package com.treesofparis.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.treesofparis.core.data.db.UserDatabase
import com.treesofparis.domain.model.Favorite
import com.treesofparis.domain.model.FavoriteSnapshot
import com.treesofparis.domain.model.GeoPoint
import kotlinx.coroutines.flow.first
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

    private val plane = FavoriteSnapshot("Platane", "QUAI DE LA SEINE", "PARIS 19E ARRDT", false, GeoPoint(48.88, 2.37))

    @After
    fun tearDown() = db.close()

    @Test
    fun toggleAddsThenRemoves() =
        runTest {
            repository.observeIsFavorite(5).test {
                assertEquals(false, awaitItem())
                repository.toggle(5, plane)
                assertEquals(true, awaitItem())
                repository.toggle(5, plane)
                assertEquals(false, awaitItem())
            }
        }

    @Test
    fun favorites_orderedByMostRecent_withTheirSnapshot() =
        runTest {
            repository.toggle(1, null)
            repository.toggle(2, plane)
            repository.toggle(3, null)
            assertEquals(
                listOf(Favorite(3, null), Favorite(2, plane), Favorite(1, null)),
                repository.observeFavorites().first(),
            )
        }

    @Test
    fun updateSnapshots_replacesThemForFavoritesOnly() =
        runTest {
            repository.toggle(1, null)
            repository.updateSnapshots(mapOf(1L to plane, 2L to plane))
            assertEquals(listOf(Favorite(1, plane)), repository.observeFavorites().first())
        }
}
