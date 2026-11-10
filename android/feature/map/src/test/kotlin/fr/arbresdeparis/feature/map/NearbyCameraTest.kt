package fr.arbresdeparis.feature.map

import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyCameraTest {
    private val me = GeoPoint.PARIS_CENTER

    @Test
    fun treeAFewMetresAway_streetLevelBetweenUserAndTree() {
        val tree = me.copy(lat = me.lat + 0.0001) // 11 m north
        val camera = nearbyCamera(from = me, tree = tree)
        assertEquals(NEARBY_MAX_ZOOM, camera.zoom, 0.0)
        assertEquals(me.lat + 0.00005, camera.center.lat, 1e-9)
        assertEquals(me.lon, camera.center.lon, 1e-9)
    }

    @Test
    fun treeAHundredMetresAway_zoomsOutJustEnoughToShowBoth() {
        val tree = me.copy(lat = me.lat + 0.0009) // 100 m north
        val camera = nearbyCamera(from = me, tree = tree)
        assertTrue("was ${camera.zoom}", camera.zoom in 17.0..18.0)
    }

    @Test
    fun treeTooFarToShowBoth_centresOnTheTreeAtTheTreeZoom() {
        val tree = GeoPoint(lat = me.lat + 0.01, lon = me.lon) // 1.1 km north
        val camera = nearbyCamera(from = me, tree = tree)
        assertEquals(tree, camera.center)
        assertEquals(MapViewModel.MIN_TREE_ZOOM, camera.zoom, 0.0)
    }
}
