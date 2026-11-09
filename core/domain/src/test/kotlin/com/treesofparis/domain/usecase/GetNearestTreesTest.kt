package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeTreeRepository
import com.treesofparis.domain.fake.tree
import com.treesofparis.domain.model.GeoPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNearestTreesTest {
    private val me = GeoPoint.PARIS_CENTER

    /** A tree [northM] metres north and [eastM] metres east of [me]. */
    private fun treeAt(
        id: Long,
        northM: Double,
        eastM: Double = 0.0,
    ) = tree(id, lat = me.lat + northM / 111_195.0, lon = me.lon + eastM / 73_130.0)

    @Test
    fun returnsTheClosestTreesFirstWithTheirDistance() =
        runTest {
            val repo = FakeTreeRepository(listOf(treeAt(1, 40.0), treeAt(2, 5.0), treeAt(3, 0.0, eastM = -20.0)))

            val result = GetNearestTrees(repo)(me, count = 2)

            assertEquals(listOf(2L, 3L), result.map { it.tree.id })
            assertEquals(5.0, result[0].distanceM, 0.5)
            assertEquals(20.0, result[1].distanceM, 0.5)
        }

    @Test
    fun widensTheSearchWhenNoTreeIsClose() =
        runTest {
            val repo = FakeTreeRepository(listOf(treeAt(1, 2_000.0)))

            val result = GetNearestTrees(repo)(me, count = 3)

            assertEquals(listOf(1L), result.map { it.tree.id })
            assertEquals(2_000.0, result.single().distanceM, 5.0)
        }

    @Test
    fun aTreeInTheSearchBoxCornerDoesNotBeatACloserOneOutsideTheBox() =
        runTest {
            // 85 m north and 85 m east: inside the first 100 m box, but 120 m away.
            val corner = treeAt(1, 85.0, eastM = 85.0)
            // 110 m east: outside the first box, yet closer.
            val east = treeAt(2, 0.0, eastM = 110.0)
            val repo = FakeTreeRepository(listOf(corner, east))

            val result = GetNearestTrees(repo)(me, count = 1)

            assertEquals(listOf(2L), result.map { it.tree.id })
        }

    @Test
    fun keepsOnlyRemarkableTreesOrTheGenusAsked() =
        runTest {
            val plane = treeAt(1, 5.0)
            val remarkable = treeAt(2, 30.0).copy(remarkable = true)
            val lime = treeAt(3, 60.0).copy(genus = "Tilia")
            val repo = FakeTreeRepository(listOf(plane, remarkable, lime))

            assertEquals(listOf(2L), GetNearestTrees(repo)(me, 3, remarkableOnly = true).map { it.tree.id })
            assertEquals(listOf(3L), GetNearestTrees(repo)(me, 3, genus = "Tilia").map { it.tree.id })
        }

    @Test
    fun noTreeWithinTheLargestRadius_returnsNothing() =
        runTest {
            val repo = FakeTreeRepository(listOf(treeAt(1, 50_000.0)))

            assertTrue(GetNearestTrees(repo)(me, count = 3).isEmpty())
        }
}
