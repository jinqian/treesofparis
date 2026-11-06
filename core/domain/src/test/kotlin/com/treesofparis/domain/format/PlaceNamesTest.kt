package com.treesofparis.domain.format

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceNamesTest {
    @Test
    fun formatArea_arrondissementsAndDepartments() {
        assertEquals("Paris 19e", formatArea("PARIS 19E ARRDT"))
        assertEquals("Paris 1er", formatArea("PARIS 1ER ARRDT"))
        assertEquals("Seine-Saint-Denis", formatArea("SEINE-SAINT-DENIS"))
        assertEquals("Val-de-Marne", formatArea("VAL-DE-MARNE"))
        assertEquals("Bois de Boulogne", formatArea("BOIS DE BOULOGNE"))
    }

    @Test
    fun formatPlace_titleCasesWithFrenchParticles() {
        assertEquals("Quai de la Seine", formatPlace("QUAI DE LA SEINE"))
        assertEquals(
            "Cimetière / Avenue des Merisiers / Div 72",
            formatPlace("CIMETIÈRE / AVENUE DES MERISIERS / DIV 72"),
        )
    }
}
