package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.fake.FakeTreeRepository
import fr.arbresdeparis.domain.fake.tree
import fr.arbresdeparis.domain.model.GeoBounds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTreesInBoundsTest {
    @Test
    fun returnsTreesInsideBoundsUpToLimit() =
        runTest {
            val repo = FakeTreeRepository(listOf(tree(1, 48.85, 2.35), tree(2, 48.86, 2.36), tree(3, 51.5, 0.0)))
            val result = GetTreesInBounds(repo)(GeoBounds.COVERAGE, limit = 1, remarkableOnly = false)
            assertEquals(1, result.size)
            assertEquals(true, result.single().id in listOf(1L, 2L))
        }

    @Test
    fun remarkableOnly_returnsOnlyRemarkableTrees() =
        runTest {
            val repo = FakeTreeRepository(listOf(tree(1), tree(2).copy(remarkable = true)))
            val result = GetTreesInBounds(repo)(GeoBounds.COVERAGE, limit = 10, remarkableOnly = true)
            assertEquals(listOf(2L), result.map { it.id })
        }
}
