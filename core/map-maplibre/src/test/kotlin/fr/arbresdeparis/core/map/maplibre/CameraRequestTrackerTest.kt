package fr.arbresdeparis.core.map.maplibre

import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraRequestTrackerTest {
    private val camera = MapCamera(GeoPoint.PARIS_CENTER, zoom = 16.0)

    @Test
    fun appliesEachIdOnce() {
        val tracker = CameraRequestTracker()
        assertTrue(tracker.shouldApply(CameraRequest(id = 1, camera = camera)))
        assertFalse(tracker.shouldApply(CameraRequest(id = 1, camera = camera)))
        assertTrue(tracker.shouldApply(CameraRequest(id = 2, camera = camera)))
    }

    @Test
    fun nullIsIgnored() {
        assertFalse(CameraRequestTracker().shouldApply(null))
    }
}
