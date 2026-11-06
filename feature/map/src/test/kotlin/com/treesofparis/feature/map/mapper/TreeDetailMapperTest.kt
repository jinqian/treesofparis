package com.treesofparis.feature.map.mapper

import com.treesofparis.feature.map.tree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreeDetailMapperTest {
    @Test
    fun toDetailUi_buildsScientificName() {
        val detail = tree(1).copy(cultivar = "Kanzan").toDetailUi(isFavorite = true)
        assertEquals("Platanus x hispanica 'Kanzan'", detail.scientificName)
        assertEquals("16 m", detail.height)
        assertEquals("165 cm", detail.circumference)
        assertEquals(true, detail.isFavorite)
        assertEquals("Paris 19e", detail.area)
        assertEquals("Quai de la Seine", detail.address)
    }

    @Test
    fun toDetailUi_missingFields_areNull() {
        val detail =
            tree(1)
                .copy(genus = null, species = null, heightM = null, circumferenceCm = null, stage = null)
                .toDetailUi(isFavorite = false)
        assertNull(detail.scientificName)
        assertNull(detail.height)
        assertNull(detail.circumference)
        assertNull(detail.stage)
    }
}
