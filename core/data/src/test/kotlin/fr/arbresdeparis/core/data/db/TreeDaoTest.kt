package fr.arbresdeparis.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeDaoTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext<Context>(),
                TreeDatabase::class.java,
            ).allowMainThreadQueries()
            .build()
    private val dao = db.treeDao()

    @After
    fun tearDown() = db.close()

    @Test
    fun inBounds_returnsOnlyTreesInside() =
        runTest {
            db.insert(treeEntity(1, 48.850, 2.350), treeEntity(2, 48.851, 2.351), treeEntity(3, 48.900, 2.400))
            val ids =
                dao
                    .inBounds(
                        south = 48.84,
                        west = 2.34,
                        north = 48.86,
                        east = 2.36,
                        remarkableOnly = false,
                        genus = null,
                        limit = 10,
                    ).map {
                        it.id
                    }
            assertEquals(setOf(1L, 2L), ids.toSet())
        }

    @Test
    fun inBounds_respectsLimit() =
        runTest {
            db.insert(*(1L..20L).map { treeEntity(it, 48.85 + it / 10_000.0, 2.35) }.toTypedArray())
            assertEquals(
                5,
                dao.inBounds(48.84, 2.34, 48.86, 2.36, remarkableOnly = false, genus = null, limit = 5).size,
            )
        }

    @Test
    fun inBounds_overLimit_keepsTreesClosestToCentre() =
        runTest {
            db.insert(treeEntity(1, 48.841, 2.341), treeEntity(2, 48.850, 2.350), treeEntity(3, 48.859, 2.359))
            assertEquals(
                listOf(2L),
                dao.inBounds(48.84, 2.34, 48.86, 2.36, remarkableOnly = false, genus = null, limit = 1).map { it.id },
            )
        }

    @Test
    fun inBounds_remarkableOnly_returnsOnlyRemarkableTrees() =
        runTest {
            db.insert(treeEntity(1, 48.850, 2.350), treeEntity(2, 48.851, 2.351).copy(remarkable = true))
            val ids =
                dao
                    .inBounds(
                        48.84,
                        2.34,
                        48.86,
                        2.36,
                        remarkableOnly = true,
                        genus = null,
                        limit = 10,
                    ).map { it.id }
            assertEquals(listOf(2L), ids)
        }

    @Test
    fun remarkable_returnsEveryRemarkableTree() =
        runTest {
            db.insert(
                treeEntity(1, 48.850, 2.350),
                treeEntity(2, 48.851, 2.351).copy(remarkable = true),
                treeEntity(3, 48.900, 2.400).copy(remarkable = true),
            )
            assertEquals(setOf(2L, 3L), dao.remarkable().map { it.id }.toSet())
        }

    @Test
    fun clusters_countTheTreesOfEachCell_atTheirMeanPosition() =
        runTest {
            db.insert(
                treeEntity(1, 48.8501, 2.3501),
                treeEntity(2, 48.8503, 2.3503),
                treeEntity(3, 48.8800, 2.3000),
                treeEntity(4, 48.9500, 2.3000), // outside the box
            )
            val cells = dao.clusters(48.80, 2.25, 48.90, 2.40, cellLat = 0.01, cellLon = 0.01).sortedBy { it.count }
            assertEquals(listOf(1, 2), cells.map { it.count })
            assertEquals(48.8502, cells[1].lat, 1e-9)
            assertEquals(2.3502, cells[1].lon, 1e-9)
        }

    @Test
    fun remarkable_leavesOutTheOtherTownsTrees() =
        runTest {
            db.insert(
                treeEntity(1, 48.850, 2.350).copy(remarkable = true),
                treeEntity(2, 48.824, 2.271, city = "Issy-les-Moulineaux", source = "gpso").copy(remarkable = true),
            )
            assertEquals(listOf(1L), dao.remarkable().map { it.id })
            // The map still shows both.
            assertEquals(2, dao.inBounds(48.8, 2.2, 48.9, 2.4, remarkableOnly = true, genus = null, limit = 10).size)
        }

    @Test
    fun getByIds_ignoresUnknownIds() =
        runTest {
            db.insert(treeEntity(1), treeEntity(2))
            assertEquals(setOf(1L, 2L), dao.byIds(listOf(1, 2, 99)).map { it.id }.toSet())
        }

    @Test
    fun byId_unknown_returnsNull() =
        runTest {
            assertNull(dao.byId(42))
        }

    @Test
    fun inBounds_genus_keepsOnlyThatGenus() =
        runTest {
            db.insert(treeEntity(1, genus = "Tilia"), treeEntity(2, genus = "Platanus"), treeEntity(3, genus = "Tilia"))
            val ids =
                dao
                    .inBounds(
                        48.84,
                        2.34,
                        48.86,
                        2.36,
                        remarkableOnly = false,
                        genus = "Tilia",
                        limit = 10,
                    ).map { it.id }
            assertEquals(setOf(1L, 3L), ids.toSet())
        }
}
