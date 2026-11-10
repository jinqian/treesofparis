package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.TreeCluster
import org.junit.Assert.assertEquals
import org.junit.Test

class TreeClusterMapperTest {
    @Test
    fun toMapCluster_labelsTheCountInFrench_withAPlainNoBreakSpace() {
        val cluster = TreeCluster(GeoPoint(48.85, 2.35), count = 12_543).toMapCluster(id = 4)
        assertEquals(4L, cluster.id)
        assertEquals(12_543, cluster.count)
        assertEquals("12\u00A0543", cluster.label)
        assertEquals("7", TreeCluster(GeoPoint(48.85, 2.35), count = 7).toMapCluster(0).label)
    }
}
