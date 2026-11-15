package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.feature.map.TreeDetailUi
import org.junit.Assert.assertEquals
import org.junit.Test

class TreeShareMapperTest {
    private val tree =
        TreeDetailUi(
            id = 2002389,
            location = GeoPoint(48.888, 2.37),
            name = "Platane commun",
            scientificName = "Platanus × hispanica",
            area = "Paris 19e",
            address = "Quai de la Seine",
            height = null,
            circumference = null,
            stage = null,
            remarkable = false,
            isFavorite = false,
        )

    @Test
    fun shareText_isTheNameAndPlaceThenTheLink() {
        assertEquals(
            "Platane commun · Quai de la Seine, Paris 19e\n" +
                "https://arbresdeparis.fr/arbre/?id=2002389&ll=48.88800,2.37000",
            tree.toShareText(),
        )
    }

    @Test
    fun shareText_withoutAnAddress_keepsTheArea() {
        assertEquals(
            "Platane commun · Paris 19e\nhttps://arbresdeparis.fr/arbre/?id=2002389&ll=48.88800,2.37000",
            tree.copy(address = " ").toShareText(),
        )
    }
}
