package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.domain.model.GenusSummary
import fr.arbresdeparis.feature.map.GenusChoiceUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenusChoiceMapperTest {
    private val genera =
        listOf(
            GenusSummary("Platanus", "Platane", treeCount = 42_591, remarkableCount = 44),
            GenusSummary("Ginkgo", "Arbre aux quarante écus", treeCount = 1_154, remarkableCount = 8),
            GenusSummary("Acer", "Erable", treeCount = 21_157, remarkableCount = 5),
        )

    @Test
    fun toGenusPicker_drawnGeneraApartFromTheOthers_inTheGivenOrder() {
        val picker = genera.toGenusPicker(selected = "Acer", remarkableOnly = false)
        assertEquals(listOf("Platanus", "Acer"), picker.drawn.map { it.genus })
        assertEquals(listOf("Ginkgo"), picker.others.map { it.genus })
        assertEquals(TreeLeaf.ACER, picker.drawn[1].leaf)
        assertEquals(listOf(false, true), picker.drawn.map { it.selected })
        assertEquals("42 591", picker.drawn[0].countLabel)
    }

    @Test
    fun toGenusPicker_remarkableOnly_countsRemarkableTrees() {
        val picker = genera.toGenusPicker(selected = null, remarkableOnly = true)
        assertEquals(listOf(44, 5), picker.drawn.map { it.count })
        assertTrue(picker.remarkableOnly)
    }

    private val ginkgo = GenusChoiceUi("Ginkgo", "Arbre aux quarante écus", TreeLeaf.GENERIC, 1, "1", false)
    private val maple = GenusChoiceUi("Acer", "Erable", TreeLeaf.ACER, 1, "1", false)

    @Test
    fun matches_wordStartsOfTheShownNameFrenchNameOrLatinGenus_ignoringCaseAndAccents() {
        assertTrue(maple.matches("éra", name = "Érable"))
        assertTrue(maple.matches("ACER", name = "Érable"))
        assertTrue(ginkgo.matches("gink", name = "Arbre aux quarante écus"))
        assertTrue(ginkgo.matches("quarante", name = "Arbre aux quarante écus"))
        assertTrue(ginkgo.matches("arbre aux", name = "Arbre aux quarante écus"))
        assertFalse(maple.matches("rable", name = "Érable"))
        assertFalse(ginkgo.matches("chêne", name = "Arbre aux quarante écus"))
    }

    @Test
    fun matches_blankQuery_findsEverything() {
        assertTrue(maple.matches("  ", name = "Érable"))
    }
}
