package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.repository.FloweringRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month

class GetFloweringSourcesTest {
    private fun sheet(name: String) =
        FloweringSheet(
            genus = name.substringBefore(' '),
            scientificName = name,
            commonName = name,
            months = setOf(Month.MAY),
            showyFlowers = false,
            url = "https://example.org/$name.pdf",
        )

    @Test
    fun returnsTheSheetsByScientificName() {
        val repository =
            object : FloweringRepository {
                override fun sheets() =
                    listOf(sheet("Tilia cordata"), sheet("Acer campestre"), sheet("Fagus sylvatica"))
            }
        val names = GetFloweringSources(repository)().map { it.scientificName }
        assertEquals(listOf("Acer campestre", "Fagus sylvatica", "Tilia cordata"), names)
    }
}
