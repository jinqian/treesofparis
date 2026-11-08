package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GenusCount
import com.treesofparis.domain.model.RemarkableStatistics
import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTreeStatisticsTest {
    private class FakeStatistics : TreeStatisticsRepository {
        var askedAreas: Int? = null

        override suspend fun getStatistics(areaLimit: Int): TreeStatistics {
            askedAreas = areaLimit
            return TreeStatistics(
                total = 10,
                genusCount = 1,
                taxonCount = 1,
                genera = listOf(GenusCount("Platanus", 10)),
                stages = emptyList(),
                remarkable = RemarkableStatistics(count = 0, leadingGenus = null, tallest = null, widest = null),
                mostWoodedAreas = emptyList(),
            )
        }
    }

    @Test
    fun asksForTheThreeMostWoodedArrondissements() =
        runTest {
            val repository = FakeStatistics()
            val stats = GetTreeStatistics(repository)()
            assertEquals(3, repository.askedAreas)
            assertEquals(10, stats.total)
        }

    @Test
    fun shareOfAGenus_isItsFractionOfAllTrees() {
        val stats =
            TreeStatistics(
                total = 200,
                genusCount = 2,
                taxonCount = 2,
                genera = listOf(GenusCount("Platanus", 50), GenusCount("Tilia", 10)),
                stages = emptyList(),
                remarkable = RemarkableStatistics(count = 0, leadingGenus = null, tallest = null, widest = null),
                mostWoodedAreas = emptyList(),
            )
        assertEquals(0.25, stats.shareOf(stats.genera[0]), 1e-9)
        assertEquals(0.0, stats.copy(total = 0).shareOf(stats.genera[0]), 1e-9)
    }
}
