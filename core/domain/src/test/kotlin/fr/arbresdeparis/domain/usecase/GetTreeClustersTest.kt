package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.fake.FakeTreeRepository
import fr.arbresdeparis.domain.fake.tree
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.TreeCluster
import fr.arbresdeparis.domain.repository.TreeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sqrt

class GetTreeClustersTest {
    @Test
    fun degrees_followTheScreenScale_andHalveWithEachZoomLevel() {
        // At zoom 13 a screen point covers about 12.6 m in Paris: 60 points ≈ 755 m ≈ 0.0068° of latitude.
        val (lat13, lon13) = GetTreeClusters.degrees(60.0, level = 13, lat = 48.85)
        assertEquals(0.0068, lat13, 0.0002)
        assertEquals(lat13 / cos(Math.toRadians(48.85)), lon13, 1e-9)
        assertEquals(lat13 / 2, GetTreeClusters.degrees(60.0, level = 14, lat = 48.85).first, 1e-9)
    }

    @Test
    fun gather_mergesTheCellsAroundTheDensest_atTheMeanOfTheirTrees() {
        val groups =
            GetTreeClusters.gather(
                listOf(
                    cell(48.8500, 2.3500, count = 30), // the densest: a seed
                    cell(48.8504, 2.3500, count = 10), // within the radius: joins it
                    cell(48.8600, 2.3500, count = 5), // far: its own group
                ),
                radiusLat = 0.001,
                radiusLon = 0.0015,
            )
        assertEquals(listOf(40, 5), groups.map { it.count })
        assertEquals(48.8501, groups[0].center.lat, 1e-9) // (48.85 × 30 + 48.8504 × 10) / 40
        assertEquals(listOf(1.0, sqrt(5.0 / 40)), groups.map { it.weight })
    }

    @Test
    fun gather_neverPutsTwoGroupsWithinTheRadius() {
        val cells =
            (0 until 20).flatMap { row ->
                (0 until 20).map { column ->
                    cell(
                        48.80 + row * 0.0004,
                        2.30 + column * 0.0006,
                        count = 1 + (row * column) % 7,
                    )
                }
            }
        val groups = GetTreeClusters.gather(cells, radiusLat = 0.001, radiusLon = 0.0015)
        assertEquals(cells.sumOf { it.count }, groups.sumOf { it.count })
        val seeds = groups.map { it.center }
        // Group centres are means, so allow them a little inside the radius of each other.
        for (a in seeds.indices) {
            for (b in a + 1 until seeds.size) {
                val dLat = (seeds[a].lat - seeds[b].lat) / 0.001
                val dLon = (seeds[a].lon - seeds[b].lon) / 0.0015
                assertTrue(dLat * dLat + dLon * dLon > 0.25)
            }
        }
    }

    @Test
    fun aLevelIsBuiltOnce_soPanningKeepsEveryGroupAsItIs() =
        runTest {
            val repository = CountingRepository(FakeTreeRepository(parisTrees()))
            val clusters = GetTreeClusters(repository)
            val west = clusters(GeoBounds(48.84, 2.30, 48.87, 2.35), zoom = 13.2)
            val east = clusters(GeoBounds(48.84, 2.32, 48.87, 2.37), zoom = 13.7)
            assertEquals(listOf(GeoBounds.COVERAGE), repository.calls)
            // A group on both screens has the same count and place on both.
            val shared = west.filter { it in east }
            assertTrue(shared.isNotEmpty())
            clusters(GeoBounds(48.84, 2.30, 48.87, 2.35), zoom = 14.0)
            assertEquals(2, repository.calls.size) // the next level is built once too
        }

    @Test
    fun onlyTheGroupsAroundTheScreenComeBack() =
        runTest {
            val clusters = GetTreeClusters(FakeTreeRepository(parisTrees()))
            val all = clusters(GeoBounds.COVERAGE, zoom = 13.0)
            val some = clusters(GeoBounds(48.85, 2.33, 48.86, 2.35), zoom = 13.0)
            assertTrue(some.isNotEmpty())
            assertTrue(some.size < all.size)
            assertTrue(all.containsAll(some))
        }

    /** Trees every 300 m or so across central Paris, denser near the Seine. */
    private fun parisTrees() =
        (0 until 40).flatMap { row ->
            (0 until 40).flatMap { column ->
                val lat = 48.83 + row * 0.0015
                val lon = 2.29 + column * 0.002
                List(if (row in 15..20) 3 else 1) { i -> tree(row * 1_000L + column * 10 + i, lat, lon) }
            }
        }

    private fun cell(
        lat: Double,
        lon: Double,
        count: Int,
    ) = TreeCluster(GeoPoint(lat, lon), count)

    private class CountingRepository(
        private val delegate: TreeRepository,
    ) : TreeRepository by delegate {
        val calls = mutableListOf<GeoBounds>()

        override suspend fun getTreeClusters(
            bounds: GeoBounds,
            cellLat: Double,
            cellLon: Double,
        ): List<TreeCluster> {
            calls += bounds
            return delegate.getTreeClusters(bounds, cellLat, cellLon)
        }
    }
}
