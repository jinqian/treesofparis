package fr.arbresdeparis.feature.favorites

import fr.arbresdeparis.domain.model.FavoriteTree
import fr.arbresdeparis.domain.model.toFavoriteSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUiMapperTest {
    @Test
    fun present_keepsRemarkableFlag() {
        assertTrue(FavoriteTree.Present(tree(1).copy(remarkable = true)).toFavoriteUi().remarkable)
        assertFalse(FavoriteTree.Present(tree(1)).toFavoriteUi().remarkable)
    }

    @Test
    fun present_formatsAreaThenPlace() {
        val ui = FavoriteTree.Present(tree(1)).toFavoriteUi()
        assertEquals("Paris 19e · Quai de la Seine", ui.subtitle)
        assertFalse(ui.gone)
    }

    @Test
    fun gone_namesTheTreeFromWhatWasRemembered() {
        val felled = tree(9, "Marronnier", "PARIS 6E ARRDT").copy(remarkable = true)
        assertEquals(
            FavoriteUiModel(9, "Marronnier", "Paris 6e · Quai de la Seine", remarkable = true, gone = true),
            FavoriteTree.Gone(9, felled.toFavoriteSnapshot()).toFavoriteUi(),
        )
    }

    @Test
    fun gone_neverRemembered_hasNoName() {
        assertEquals(
            FavoriteUiModel(9, title = null, subtitle = null, gone = true),
            FavoriteTree.Gone(9, snapshot = null).toFavoriteUi(),
        )
    }
}
