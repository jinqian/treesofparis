package com.treesofparis.feature.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.feature.map.mapper.toDetailUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeDetailContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun missingFields_showFrenchPlaceholders() {
        val detail = tree(1).copy(genus = null, species = null, heightM = null).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("Espèce inconnue").assertIsDisplayed()
        compose.onNodeWithText("Hauteur inconnue").assertIsDisplayed()
        compose.onNodeWithText("Paris 19e").assertIsDisplayed()
    }

    @Test
    fun favoriteButton_invokesToggle() {
        var toggles = 0
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = { toggles++ }) } }
        compose.onNodeWithContentDescription("Ajouter aux favoris").performClick()
        assertEquals(1, toggles)
    }
}
