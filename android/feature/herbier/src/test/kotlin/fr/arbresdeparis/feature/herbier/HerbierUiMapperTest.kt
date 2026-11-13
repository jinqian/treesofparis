package fr.arbresdeparis.feature.herbier

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.feature.herbier.mapper.toHerbierUi
import fr.arbresdeparis.feature.herbier.mapper.toRemarkableAreasUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HerbierUiMapperTest {
    private val ui = PARIS.toHerbierUi()

    @Test
    fun totals_areFormattedInFrench() {
        assertEquals("220 339", ui.totalTrees)
        assertEquals("202", ui.genusCount)
        assertEquals("1 004", ui.taxonCount)
    }

    @Test
    fun headline_oneTreeInFiveIsAPlane_aheadOfHorseChestnuts() {
        val headline = ui.headline!!
        assertEquals(TreeLeaf.PLATANUS, headline.leaf)
        assertEquals(5, headline.oneIn)
        assertEquals("42 591", headline.count)
        assertEquals(TreeLeaf.AESCULUS, headline.runnerUp?.leaf)
        assertEquals("24 795", headline.runnerUp?.count)
    }

    @Test
    fun genera_onlyDrawnOnes_mostPlantedFirst_withShareAndBarRelativeToTheFirst() {
        assertEquals(TreeLeaf.PLATANUS, ui.genera.first().leaf)
        // Ginkgo has no drawing: it is left out of the list.
        assertEquals(12, ui.genera.size)
        assertEquals(TreeLeaf.PRUNUS, ui.genera[5].leaf)
        assertEquals("19,3 %", ui.genera.first().share)
        assertEquals(1f, ui.genera.first().fraction)
        assertEquals(24_795f / 42_591f, ui.genera[1].fraction, 1e-4f)
        // The drawn genera hold 162 053 of the 220 339 trees.
        assertEquals("74\u202F%", ui.drawnShare)
    }

    @Test
    fun stages_inGrowthOrder_unknownCountedApart() {
        assertEquals(listOf("Jeune", "Jeune adulte", "Adulte", "Mature"), ui.stages.map { it.label })
        assertEquals(1f, ui.stages[2].fraction)
        assertEquals("36 239", ui.unknownStageCount)
    }

    @Test
    fun remarkable_countLeaderAndRecordsWithShortPlaces() {
        val remarkable = ui.remarkable
        assertEquals("181", remarkable.count)
        assertEquals(TreeLeaf.PLATANUS, remarkable.leading?.leaf)
        assertEquals(44, remarkable.leading?.count)
        assertEquals("45 m", remarkable.tallest?.value)
        assertEquals("Bois de Boulogne · Route du Lac à Bagatelle", remarkable.tallest?.place)
        assertEquals("7,40 m", remarkable.widest?.value)
        assertEquals("Paris 19e · Parc des Buttes Chaumont", remarkable.widest?.place)
    }

    @Test
    fun remarkableTrees_withAStory_tellItInOneSentence() {
        val luxembourg =
            REMARKABLE_TREES
                .toRemarkableAreasUi(STORIES)
                .first()
                .trees
                .single { it.id == 13L }
        // The botanical first sentence (« … appartient à la famille des … ») is skipped: the next one tells the tree.
        assertEquals(
            "Cet arbre est issu du semis du véritable marronnier qu’Anne Frank voyait depuis son grenier, à Amsterdam.",
            luxembourg.teaser,
        )
    }

    @Test
    fun remarkableTrees_byArrondissementThenOtherPlaces_withPlaceAndHeight() {
        val areas = REMARKABLE_TREES.toRemarkableAreasUi()
        assertEquals(listOf("Paris 6e", "Paris 19e", "Bois de Boulogne"), areas.map { it.name })
        // Within an area, by name: Érable before Marronnier.
        assertEquals(listOf(14L, 13L), areas.first().trees.map { it.id })
        assertEquals(
            "Parc Monceau · 22 m",
            areas
                .first()
                .trees
                .first()
                .detail,
        )
        assertEquals("Jardin du Luxembourg", areas.first().trees[1].detail)
        assertEquals("Parc des Buttes Chaumont · 30 m", areas[1].trees.single().detail)
    }

    @Test
    fun areas_rankedWithFrenchNames() {
        assertEquals(listOf(1, 2, 3), ui.areas.map { it.rank })
        assertEquals("Paris 15e", ui.areas.first().name)
        assertEquals("18 622", ui.areas.first().count)
    }

    @Test
    fun emptyData_hasNoHeadlineNorRecords() {
        val empty =
            PARIS
                .copy(
                    total = 0,
                    genera = emptyList(),
                    remarkable = PARIS.remarkable.copy(count = 0, leadingGenus = null, tallest = null, widest = null),
                ).toHerbierUi()
        assertNull(empty.headline)
        assertNull(empty.remarkable.tallest)
    }
}
