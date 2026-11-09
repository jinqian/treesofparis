package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.domain.model.WikipediaArticle
import fr.arbresdeparis.feature.map.MeasureUi
import fr.arbresdeparis.feature.map.WikipediaUi
import fr.arbresdeparis.feature.map.tree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Month

class TreeDetailMapperTest {
    @Test
    fun toDetailUi_buildsScientificName() {
        val detail = tree(1).copy(cultivar = "Kanzan").toDetailUi(isFavorite = true)
        assertEquals("Platanus x hispanica 'Kanzan'", detail.scientificName)
        assertEquals(MeasureUi("16", "m"), detail.height)
        assertEquals(MeasureUi("165", "cm"), detail.circumference)
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

    @Test
    fun flowering_marksTheMonthsAndCountsTheWait() {
        val cherry = FloweringSeason("Prunus", setOf(Month.MARCH, Month.APRIL), showyFlowers = true)
        val ui = cherry.toFloweringUi(now = Month.NOVEMBER)
        assertEquals(
            listOf(3, 4),
            ui.months
                .withIndex()
                .filter { it.value }
                .map { it.index + 1 },
        )
        assertEquals("mars – avril", ui.range)
        assertEquals(true, ui.showyFlowers)
        assertEquals(11, ui.currentMonth)
        assertEquals(4, ui.monthsUntilFlowering)
        assertEquals(0, cherry.toFloweringUi(now = Month.APRIL).monthsUntilFlowering)
    }

    @Test
    fun toDetailUi_picksTheGenusLeaf() {
        assertEquals(TreeLeaf.PLATANUS, tree(1).toDetailUi(isFavorite = false).leaf)
        assertEquals(TreeLeaf.GENERIC, tree(1).copy(genus = "Ginkgo").toDetailUi(isFavorite = false).leaf)
        assertEquals(TreeLeaf.GENERIC, tree(1).copy(genus = null).toDetailUi(isFavorite = false).leaf)
    }

    @Test
    fun toDetailUi_buildsTheWikipediaLink() {
        val species = tree(1).copy(wikipedia = WikipediaArticle("Platane commun", isGenusArticle = false))
        assertEquals(
            WikipediaUi(url = "https://fr.wikipedia.org/wiki/Platane_commun", title = "Platane commun", genus = null),
            species.toDetailUi(isFavorite = false).wikipedia,
        )
        val accents = tree(1).copy(wikipedia = WikipediaArticle("Févier d'Amérique", isGenusArticle = false))
        assertEquals(
            "https://fr.wikipedia.org/wiki/F%C3%A9vier_d%27Am%C3%A9rique",
            accents.toDetailUi(isFavorite = false).wikipedia?.url,
        )
    }

    @Test
    fun toDetailUi_genusArticle_namesTheGenus() {
        val genus = tree(1).copy(species = null, wikipedia = WikipediaArticle("Platane", isGenusArticle = true))
        assertEquals("Platanus", genus.toDetailUi(isFavorite = false).wikipedia?.genus)
        assertNull(tree(1).toDetailUi(isFavorite = false).wikipedia)
    }
}
