package fr.arbresdeparis.core.map.osmdroid

import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MarkerStyle
import fr.arbresdeparis.domain.model.GeoPoint
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
    fun markerSizes_scaleWithDensity() {
        val sizes = markerSizes(density = 2.625f) // xxhdpi, e.g. Pixel 7
        assertEquals(15.75f, sizes.treeRadiusPx, 0.01f) // 6 dp
        assertEquals(23.625f, sizes.selectedRadiusPx, 0.01f) // 9 dp
        assertEquals(21f, sizes.userRadiusPx, 0.01f) // 8 dp
        assertEquals(21f, sizes.remarkableRadiusPx, 0.01f) // 8 dp
        assertEquals(5.25f, sizes.ringWidthPx, 0.01f) // 2 dp
    }

    @Test
    fun ringRadius_addsRingWidthAroundThePin() {
        val sizes = markerSizes(density = 2f)
        assertEquals(16f, sizes.ringRadius(sizes.treeRadiusPx), 0.01f) // (6 + 2) dp
    }

    @Test
    fun selectedIndex_findsSelectedMarker() {
        assertEquals(1, selectedIndex(listOf(markers[0], markers[1].copy(selected = true))))
        assertNull(selectedIndex(markers))
    }

    @Test
    fun markerLayers_drawRemarkableTreesAboveOrdinaryOnes() {
        val ordinary = MapMarker(1, GeoPoint(48.85, 2.35))
        val remarkable = MapMarker(2, GeoPoint(48.86, 2.36), style = MarkerStyle.REMARKABLE)
        val layers = markerLayers(listOf(remarkable, ordinary))
        assertEquals(listOf(MarkerStyle.TREE, MarkerStyle.REMARKABLE), layers.keys.toList())
        assertEquals(listOf(ordinary), layers[MarkerStyle.TREE])
        assertEquals(listOf(remarkable), layers[MarkerStyle.REMARKABLE])
    }

    @Test
    fun markerLayers_omitsEmptyLayers() {
        val layers = markerLayers(listOf(MapMarker(2, GeoPoint(48.86, 2.36), style = MarkerStyle.REMARKABLE)))
        assertEquals(listOf(MarkerStyle.REMARKABLE), layers.keys.toList())
    }
}
