package fr.arbresdeparis.domain.format

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
    fun formatPlace_keepsRomanNumeralsUpperCase() {
        assertEquals("Square Jean XXIII", formatPlace("SQUARE JEAN XXIII"))
        assertEquals("Square Barye / 2 Boulevard Henri IV", formatPlace("SQUARE BARYE / 2 BOULEVARD HENRI IV"))
        assertEquals("Rue Vivienne", formatPlace("RUE VIVIENNE"))
    }

    @Test
    fun formatPlace_titleCasesWithFrenchParticles() {
        assertEquals("Quai de la Seine", formatPlace("QUAI DE LA SEINE"))
        assertEquals(
            "Cimetière / Avenue des Merisiers / Div 72",
            formatPlace("CIMETIÈRE / AVENUE DES MERISIERS / DIV 72"),
        )
    }

    @Test
    fun formatPlace_restoresTheAccentOfALoneA() {
        // The dataset is upper case without accents: « A » there is the preposition « à ».
        assertEquals("Route du Lac à Bagatelle", formatPlace("ROUTE DU LAC A BAGATELLE"))
        assertEquals("Avenue de la Porte de Vincennes", formatPlace("AVENUE DE LA PORTE DE VINCENNES"))
    }

    @Test
    fun formatPlace_keepsElidedArticlesLowerCase() {
        assertEquals("Rue d'Estienne d'Orves", formatPlace("Rue d'Estienne d'Orves"))
        assertEquals("Ville-d'Avray", formatPlace("VILLE-D'AVRAY"))
        assertEquals("Avenue de l'Est", formatPlace("AVENUE DE L''EST"))
        assertEquals("L'Haÿ-les-Roses", formatPlace("L'HAŸ-LES-ROSES"))
    }

    @Test
    fun formatArea_namesTheNeighbouringTowns() {
        assertEquals("Issy-les-Moulineaux", formatArea("Issy-les-Moulineaux"))
        assertEquals("Ville-d'Avray", formatArea("Ville-d'Avray"))
    }
}
