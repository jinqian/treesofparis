package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.fake.FakeTreeRepository
import fr.arbresdeparis.domain.fake.tree
import fr.arbresdeparis.domain.model.GeoBounds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTreeClustersTest {
    @Test
    fun cellSize_spansAbout70ScreenPointsAndHalvesWithEachZoomLevel() {
        // At zoom 13 a screen point covers about 12.6 m in Paris: 70 points ≈ 880 m ≈ 0.0079° of latitude.
        val (lat13, lon13) = GetTreeClusters.cellSize(zoom = 13.0, lat = 48.85)
        assertEquals(0.0079, lat13, 0.0002)
        assertEquals(lat13 / kotlin.math.cos(Math.toRadians(48.85)), lon13, 1e-9)
        val (lat14, _) = GetTreeClusters.cellSize(zoom = 14.0, lat = 48.85)
        assertEquals(lat13 / 2, lat14, 1e-9)
    }

    @Test
    fun cellSize_onlyChangesAtWholeZoomLevels() {
        assertEquals(GetTreeClusters.cellSize(13.0, 48.85), GetTreeClusters.cellSize(13.9, 48.85))
    }

    @Test
    fun groupsTheTreesInsideTheBounds() =
        runTest {
            val trees =
                FakeTreeRepository(
                    listOf(
                        tree(1, 48.8501, 2.3501),
                        tree(2, 48.8502, 2.3502),
                        tree(3, 48.8800, 2.3000),
                        tree(4, 48.9500, 2.3000), // outside the bounds
                    ),
                )
            val clusters =
                GetTreeClusters(trees)(GeoBounds(48.80, 2.25, 48.90, 2.40), zoom = 13.0).sortedBy { it.count }
            assertEquals(listOf(1, 2), clusters.map { it.count })
            assertEquals(48.85015, clusters[1].center.lat, 1e-9)
        }
}
