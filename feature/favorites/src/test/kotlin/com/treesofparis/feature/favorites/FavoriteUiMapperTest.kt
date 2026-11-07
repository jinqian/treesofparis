package com.treesofparis.feature.favorites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUiMapperTest {
    @Test
    fun toFavoriteUi_keepsRemarkableFlag() {
        assertTrue(tree(1).copy(remarkable = true).toFavoriteUi().remarkable)
        assertFalse(tree(1).toFavoriteUi().remarkable)
    }

    @Test
    fun toFavoriteUi_formatsPlaceThenArea() {
        assertEquals("Quai de la Seine · Paris 19e", tree(1).toFavoriteUi().subtitle)
    }
}
