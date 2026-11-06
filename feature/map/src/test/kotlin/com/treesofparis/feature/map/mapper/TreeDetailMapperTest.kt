package com.treesofparis.feature.map.mapper

import com.treesofparis.feature.map.tree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreeDetailMapperTest {
    @Test
    fun toDetailUi_formatsArea() {
        assertEquals("Paris 19e", formatArea("PARIS 19E ARRDT"))
        assertEquals("Paris 1er", formatArea("PARIS 1ER ARRDT"))
        assertEquals("Seine-Saint-Denis", formatArea("SEINE-SAINT-DENIS"))
        assertEquals("Val-de-Marne", formatArea("VAL-DE-MARNE"))
        assertEquals("Bois de Boulogne", formatArea("BOIS DE BOULOGNE"))
    }

    @Test
    fun toDetailUi_formatsAddress() {
        assertEquals("Quai de la Seine", formatPlace("QUAI DE LA SEINE"))
        assertEquals(
            "Cimetière / Avenue des Merisiers / Div 72",
            formatPlace("CIMETIÈRE / AVENUE DES MERISIERS / DIV 72"),
        )
    }

    @Test
    fun toDetailUi_buildsScientificName() {
        val detail = tree(1).copy(cultivar = "Kanzan").toDetailUi(isFavorite = true)
        assertEquals("Platanus x hispanica 'Kanzan'", detail.scientificName)
        assertEquals("16 m", detail.height)
        assertEquals("165 cm", detail.circumference)
        assertEquals(true, detail.isFavorite)
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
