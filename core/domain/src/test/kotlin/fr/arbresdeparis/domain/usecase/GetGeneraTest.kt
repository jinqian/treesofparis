package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.GenusSummary
import fr.arbresdeparis.domain.model.TreeStatistics
import fr.arbresdeparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetGeneraTest {
    private val plane = GenusSummary("Platanus", "Platane", treeCount = 42_591, remarkableCount = 44)
    private val lime = GenusSummary("Tilia", "Tilleul", treeCount = 23_072, remarkableCount = 1)
    private val beech = GenusSummary("Fagus", "Hêtre", treeCount = 1_928, remarkableCount = 11)
    private val pear = GenusSummary("Pyrus", "Poirier à fleurs", treeCount = 4_801, remarkableCount = 0)

    private val repository =
        object : TreeStatisticsRepository {
            override suspend fun getStatistics(areaLimit: Int): TreeStatistics = error("not used")

            override suspend fun getGenera() = listOf(plane, lime, pear, beech)
        }

    @Test
    fun allTrees_everyGenusMostPlantedFirst() =
        runTest {
            assertEquals(listOf(plane, lime, pear, beech), GetGenera(repository)(remarkableOnly = false))
        }

    @Test
    fun remarkableOnly_theGeneraWithRemarkableTrees_mostRemarkableFirst() =
        runTest {
            assertEquals(listOf(plane, beech, lime), GetGenera(repository)(remarkableOnly = true))
        }
}
