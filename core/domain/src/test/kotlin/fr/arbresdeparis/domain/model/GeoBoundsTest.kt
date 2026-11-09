package fr.arbresdeparis.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoBoundsTest {
    @Test
    fun coverage_containsCentralParis() {
        assertTrue(GeoBounds.COVERAGE.contains(GeoPoint.PARIS_CENTER))
        assertTrue(GeoBounds.COVERAGE.contains(GeoPoint(lat = 48.8330, lon = 2.4330))) // Bois de Vincennes
    }

    @Test
    fun coverage_containsPantinAndThiaisCemeteries() {
        assertTrue(GeoBounds.COVERAGE.contains(GeoPoint(lat = 48.9059, lon = 2.4101))) // Pantin
        assertTrue(GeoBounds.COVERAGE.contains(GeoPoint(lat = 48.7423, lon = 2.3600))) // Thiais
    }

    @Test
    fun coverage_excludesLondon() {
        assertFalse(GeoBounds.COVERAGE.contains(GeoPoint(lat = 51.5072, lon = -0.1276)))
    }

    @Test
    fun around_containsEveryPointWithinTheRadius() {
        val center = GeoPoint.PARIS_CENTER
        val box = GeoBounds.around(center, radiusM = 100.0)
        assertTrue(box.contains(GeoPoint(center.lat + 0.00089, center.lon))) // 99 m north
        assertTrue(box.contains(GeoPoint(center.lat, center.lon - 0.00135))) // 99 m west
        assertFalse(box.contains(GeoPoint(center.lat - 0.00091, center.lon))) // 101 m south
        assertFalse(box.contains(GeoPoint(center.lat, center.lon + 0.00138))) // 101 m east
    }

    @Test
    fun contains_pointOnEdge_true() {
        val bounds = GeoBounds(south = 1.0, west = 1.0, north = 2.0, east = 2.0)
        assertTrue(bounds.contains(GeoPoint(lat = 1.0, lon = 2.0)))
    }
}
