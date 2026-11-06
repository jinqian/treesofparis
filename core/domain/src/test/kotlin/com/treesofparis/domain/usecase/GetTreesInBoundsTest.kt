package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeTreeRepository
import com.treesofparis.domain.fake.tree
import com.treesofparis.domain.model.GeoBounds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTreesInBoundsTest {
    @Test
    fun returnsTreesInsideBoundsUpToLimit() =
        runTest {
            val repo = FakeTreeRepository(listOf(tree(1, 48.85, 2.35), tree(2, 48.86, 2.36), tree(3, 51.5, 0.0)))
            val result = GetTreesInBounds(repo)(GeoBounds.COVERAGE, limit = 1)
            assertEquals(1, result.size)
            assertEquals(true, result.single().id in listOf(1L, 2L))
        }
}
