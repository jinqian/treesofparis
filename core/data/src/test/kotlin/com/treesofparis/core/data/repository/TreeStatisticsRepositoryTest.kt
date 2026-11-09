package com.treesofparis.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.treesofparis.core.data.db.TreeDatabase
import com.treesofparis.core.data.db.insert
import com.treesofparis.core.data.db.treeEntity
import com.treesofparis.domain.model.AreaCount
import com.treesofparis.domain.model.GenusCount
import com.treesofparis.domain.model.GenusSummary
import com.treesofparis.domain.model.StageCount
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeStatisticsRepositoryTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), TreeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private val repository = TreeStatisticsRepositoryImpl(db.treeStatisticsDao())

    @Before
    fun setUp() {
        db.insert(
            treeEntity(
                1,
                genus = "Platanus",
                species = "x hispanica",
                stage = "Adulte",
                arrondissement = "PARIS 15E ARRDT",
            ),
            treeEntity(
                2,
                genus = "Platanus",
                species = "x hispanica",
                stage = "Adulte",
                arrondissement = "PARIS 15E ARRDT",
            ),
            treeEntity(
                3,
                commonName = "Platane d'Orient",
                genus = "Platanus",
                species = "orientalis",
                stage = "Mature",
                remarkable = true,
                heightM = 45,
                circumferenceCm = 535,
                arrondissement = "BOIS DE BOULOGNE",
            ),
            treeEntity(
                4,
                genus = "Platanus",
                species = "x hispanica",
                remarkable = true,
                heightM = 30,
                circumferenceCm = 740,
                arrondissement = "PARIS 19E ARRDT",
            ),
            treeEntity(
                5,
                commonName = "Tilleul",
                genus = "Tilia",
                species = "cordata",
                stage = "Jeune",
                arrondissement = "PARIS 19E ARRDT",
            ),
            treeEntity(6, commonName = "Tilleul", genus = "Tilia", species = "cordata", stage = "Jeune", heightM = 112),
            treeEntity(
                7,
                commonName = "Chêne",
                genus = "Quercus",
                species = "robur",
                stage = "Adulte",
                remarkable = true,
                heightM = 20,
            ),
            treeEntity(8, genus = null),
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun computesTheFiguresOnce() =
        runTest {
            val first = repository.getStatistics(areaLimit = 3)
            db.insert(treeEntity(9, genus = "Tilia"))
            // The shipped data never changes, so later screens reuse the first figures.
            assertEquals(first, repository.getStatistics(areaLimit = 3))
        }

    @Test
    fun countsTreesGeneraAndTaxa() =
        runTest {
            val stats = repository.getStatistics(areaLimit = 3)
            assertEquals(8, stats.total)
            assertEquals(3, stats.genusCount)
            // Platanus x hispanica, Platanus orientalis, Tilia cordata, Quercus robur.
            assertEquals(4, stats.taxonCount)
        }

    @Test
    fun genera_mostPlantedFirst_withoutMissingGenus() =
        runTest {
            assertEquals(
                listOf(GenusCount("Platanus", 4), GenusCount("Tilia", 2), GenusCount("Quercus", 1)),
                repository.getStatistics(areaLimit = 3).genera,
            )
        }

    @Test
    fun stages_includeUnknown() =
        runTest {
            assertEquals(
                setOf(StageCount("Adulte", 3), StageCount("Mature", 1), StageCount("Jeune", 2), StageCount(null, 2)),
                repository.getStatistics(areaLimit = 3).stages.toSet(),
            )
        }

    @Test
    fun remarkable_countLeaderAndRecordsIgnoreOrdinaryTrees() =
        runTest {
            val remarkable = repository.getStatistics(areaLimit = 3).remarkable
            assertEquals(3, remarkable.count)
            assertEquals(GenusCount("Platanus", 2), remarkable.leadingGenus)
            // The 112 m lime is not remarkable (and is a data error): records only look at remarkable trees.
            assertEquals(3L, remarkable.tallest?.id)
            assertEquals(4L, remarkable.widest?.id)
        }

    @Test
    fun mostWoodedAreas_onlyParisArrondissements_limited() =
        runTest {
            // Trees 6-8 use the fixture's default area, Paris 5e. Bois de Boulogne is not an arrondissement.
            assertEquals(
                listOf(
                    AreaCount("PARIS 5E ARRDT", 3),
                    AreaCount("PARIS 15E ARRDT", 2),
                    AreaCount("PARIS 19E ARRDT", 2),
                ),
                repository.getStatistics(areaLimit = 5).mostWoodedAreas,
            )
            assertEquals(1, repository.getStatistics(areaLimit = 1).mostWoodedAreas.size)
        }

    @Test
    fun genera_mostPlantedFirst_namedByTheirMostCommonFrenchName_withRemarkableCounts() =
        runTest {
            assertEquals(
                listOf(
                    // 3 « Platane », 1 « Platane d'Orient »: the majority names the genus.
                    GenusSummary("Platanus", "Platane", treeCount = 4, remarkableCount = 2),
                    GenusSummary("Tilia", "Tilleul", treeCount = 2, remarkableCount = 0),
                    GenusSummary("Quercus", "Chêne", treeCount = 1, remarkableCount = 1),
                ),
                repository.getGenera(),
            )
        }

    @Test
    fun emptyDatabase_hasNoLeaderNorRecords() =
        runTest {
            db.clearAllTables()
            val stats = repository.getStatistics(areaLimit = 3)
            assertEquals(0, stats.total)
            assertNull(stats.remarkable.leadingGenus)
            assertNull(stats.remarkable.tallest)
        }
}
