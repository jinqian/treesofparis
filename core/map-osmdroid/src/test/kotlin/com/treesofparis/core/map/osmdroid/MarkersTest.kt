package com.treesofparis.core.map.osmdroid

import com.treesofparis.core.map.MapMarker
import com.treesofparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarkersTest {
    private val markers = listOf(MapMarker(10, GeoPoint(48.85, 2.35)), MapMarker(20, GeoPoint(48.86, 2.36)))

    @Test
    fun markerIdAt_mapsPointIndexToId() {
        assertEquals(20L, markerIdAt(1, markers))
    }

    @Test
    fun markerIdAt_outOfRangeOrNull_returnsNull() {
        assertNull(markerIdAt(5, markers))
        assertNull(markerIdAt(null, markers))
    }

    @Test
    fun selectedIndex_findsSelectedMarker() {
        assertEquals(1, selectedIndex(listOf(markers[0], markers[1].copy(selected = true))))
        assertNull(selectedIndex(markers))
    }
}
