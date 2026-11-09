package com.treesofparis.feature.map.mapper

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.NearbyTree
import com.treesofparis.feature.map.tree
import org.junit.Assert.assertEquals
import org.junit.Test

class NearbyTreeMapperTest {
    @Test
    fun toNearbyUi_namesTheTreeAndItsDistance() {
        val ui = NearbyTree(tree(7, lat = 48.85, lon = 2.35), distanceM = 4.6).toNearbyUi()

        assertEquals(7L, ui.id)
        assertEquals("Platane", ui.name)
        assertEquals("Platanus x hispanica", ui.scientificName)
        assertEquals("5 m", ui.distance)
        assertEquals(GeoPoint(48.85, 2.35), ui.location)
        assertEquals(TreeLeaf.forGenus("Platanus"), ui.leaf)
    }

    @Test
    fun formatDistance_metresThenKilometresWithAFrenchComma() {
        assertEquals("0 m", formatDistance(0.2))
        assertEquals("999 m", formatDistance(999.0))
        assertEquals("1,0 km", formatDistance(999.6))
        assertEquals("2,4 km", formatDistance(2_420.0))
    }
}
