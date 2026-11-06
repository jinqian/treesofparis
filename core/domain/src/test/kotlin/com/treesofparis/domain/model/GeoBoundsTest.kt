package com.treesofparis.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoBoundsTest {
    @Test
    fun contains_pointInsideParis_true() {
        val notreDame = GeoPoint(lat = 48.8530, lon = 2.3499)
        assertTrue(GeoBounds.PARIS.contains(notreDame))
    }

    @Test
    fun contains_pointInBoisDeVincennes_true() {
        assertTrue(GeoBounds.PARIS.contains(GeoPoint(lat = 48.8330, lon = 2.4330)))
    }

    @Test
    fun contains_pointInLondon_false() {
        assertFalse(GeoBounds.PARIS.contains(GeoPoint(lat = 51.5072, lon = -0.1276)))
    }

    @Test
    fun contains_pointOnEdge_true() {
        val bounds = GeoBounds(south = 1.0, west = 1.0, north = 2.0, east = 2.0)
        assertTrue(bounds.contains(GeoPoint(lat = 1.0, lon = 2.0)))
    }
}
