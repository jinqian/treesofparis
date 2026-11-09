package com.treesofparis.feature.herbier

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.feature.herbier.mapper.toFloweringCalendarUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Month

class FloweringCalendarMapperTest {
    private val plantingOrder = PARIS.genera.map { it.genus }

    @Test
    fun rows_followTheYear_withTheirMonthsAndKind() {
        val ui = SEASONS.toFloweringCalendarUi(Month.NOVEMBER, plantingOrder)
        assertEquals(TreeLeaf.CORYLUS, ui.rows.first().leaf)
        assertEquals(
            listOf(2, 3),
            ui.rows
                .first()
                .months
                .withIndex()
                .filter { it.value }
                .map { it.index + 1 },
        )
        assertEquals("février – mars", ui.rows.first().range)
        assertEquals(true, ui.rows.single { it.leaf == TreeLeaf.PRUNUS }.showyFlowers)
        assertEquals(11, ui.currentMonth)
        assertEquals("novembre", ui.currentMonthName)
    }

    @Test
    fun november_restsUntilHazelAndElmInFebruary() {
        val ui = SEASONS.toFloweringCalendarUi(Month.NOVEMBER, plantingOrder)
        assertEquals(emptyList<TreeLeaf>(), ui.inFlower)
        assertEquals("février", ui.next?.monthName)
        assertEquals(listOf(TreeLeaf.CORYLUS, TreeLeaf.ULMUS), ui.next?.leaves)
    }

    @Test
    fun may_listsWhatFlowers_mostPlantedFirst() {
        val ui = SEASONS.toFloweringCalendarUi(Month.MAY, plantingOrder)
        assertEquals(listOf(TreeLeaf.PLATANUS, TreeLeaf.AESCULUS, TreeLeaf.TILIA), ui.inFlower)
        assertNull(ui.next)
    }
}
