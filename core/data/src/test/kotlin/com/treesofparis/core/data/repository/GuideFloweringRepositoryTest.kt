package com.treesofparis.core.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Month

class GuideFloweringRepositoryTest {
    private val sheets = GuideFloweringRepository().sheets()

    @Test
    fun coversTheTwentyOneDrawnGenera() {
        val genera =
            setOf(
                "Acer",
                "Aesculus",
                "Betula",
                "Carpinus",
                "Celtis",
                "Corylus",
                "Fagus",
                "Fraxinus",
                "Gleditsia",
                "Malus",
                "Pinus",
                "Platanus",
                "Populus",
                "Prunus",
                "Pyrus",
                "Quercus",
                "Robinia",
                "Styphnolobium",
                "Taxus",
                "Tilia",
                "Ulmus",
            )
        assertEquals(genera, sheets.map { it.genus }.toSet())
        assertEquals(27, sheets.size)
    }

    @Test
    fun everySheetIsAGuidePdfWithContiguousMonths() {
        assertEquals(sheets.size, sheets.map { it.scientificName }.toSet().size)
        sheets.forEach { sheet ->
            assertTrue(sheet.url, sheet.url.startsWith(GUIDE_FILES_URL))
            val values = sheet.months.map(Month::getValue).sorted()
            assertTrue(sheet.scientificName, values.isNotEmpty() && values == (values.first()..values.last()).toList())
        }
    }

    @Test
    fun readsTheGuideCalendars() {
        val lime = sheets.single { it.scientificName == "Tilia tomentosa" }
        assertEquals(setOf(Month.JUNE), lime.months)
        assertEquals(false, lime.showyFlowers)
        val honeyLocust = sheets.single { it.scientificName == "Gleditsia triacanthos" }
        assertEquals(setOf(Month.JUNE, Month.JULY), honeyLocust.months)
        assertEquals(true, honeyLocust.showyFlowers)
    }
}
