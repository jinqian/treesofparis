package com.treesofparis.domain.usecase

import com.treesofparis.domain.format.formatMonthRange
import com.treesofparis.domain.model.FloweringSheet
import com.treesofparis.domain.repository.FloweringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Month

class GetFloweringSeasonsTest {
    private fun sheet(
        name: String,
        months: IntRange,
        showy: Boolean = false,
    ) = FloweringSheet(
        genus = name.substringBefore(' '),
        scientificName = name,
        commonName = name,
        months = months.map(Month::of).toSet(),
        showyFlowers = showy,
        url = "https://example.org/$name.pdf",
    )

    private val repository =
        object : FloweringRepository {
            override fun sheets() =
                listOf(
                    sheet("Tilia cordata", 5..6),
                    sheet("Tilia tomentosa", 6..6),
                    sheet("Corylus colurna", 2..3),
                    sheet("Prunus avium", 3..4, showy = true),
                    sheet("Acer campestre", 5..5),
                )
        }

    @Test
    fun genusSeason_isTheUnionOfItsSheets() {
        val lime = GetFloweringSeason(repository)("Tilia")!!
        assertEquals(setOf(Month.MAY, Month.JUNE), lime.months)
        assertEquals(false, lime.showyFlowers)
        assertEquals(true, GetFloweringSeason(repository)("Prunus")!!.showyFlowers)
        assertNull(GetFloweringSeason(repository)("Ginkgo"))
    }

    @Test
    fun seasons_followTheYear() {
        val genera = GetFloweringSeasons(repository)().map { it.genus }
        assertEquals(listOf("Corylus", "Prunus", "Acer", "Tilia"), genera)
    }

    @Test
    fun monthRange_isWrittenInFrench() {
        assertEquals("mai", formatMonthRange(setOf(Month.MAY)))
        assertEquals("février – avril", formatMonthRange(setOf(Month.APRIL, Month.FEBRUARY, Month.MARCH)))
    }
}
