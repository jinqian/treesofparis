package com.treesofparis.feature.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.domain.model.WikipediaArticle
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
        compose.onNodeWithText("Quai de la Seine · Paris 19e").assertIsDisplayed()
    }

    @Test
    fun knownFields_showFiguresUnitsAndSource() {
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("Platanus x hispanica").assertIsDisplayed()
        compose.onNodeWithText("16").assertIsDisplayed()
        compose.onNodeWithText("165").assertIsDisplayed()
        compose.onNodeWithText("Adulte").assertIsDisplayed()
        compose.onNodeWithText("Source : « Les arbres », Paris Data").assertIsDisplayed()
        compose.onNodeWithText("ARBRE REMARQUABLE").assertDoesNotExist()
    }

    @Test
    fun remarkableTree_showsGoldEyebrow() {
        val detail = tree(1).copy(remarkable = true).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("ARBRE REMARQUABLE").assertIsDisplayed()
    }

    @Test
    fun favoriteTree_offersRemoval() {
        val detail = tree(1).toDetailUi(isFavorite = true)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithContentDescription("Retirer des favoris").assertIsDisplayed()
    }

    @Test
    fun favoriteButton_invokesToggle() {
        var toggles = 0
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = { toggles++ }) } }
        compose.onNodeWithContentDescription("Ajouter aux favoris").performClick()
        assertEquals(1, toggles)
    }

    @Test
    fun wikipediaRow_opensTheSpeciesArticle() {
        var opened: String? = null
        val detail =
            tree(
                1,
            ).copy(
                wikipedia = WikipediaArticle("Platane commun", isGenusArticle = false),
            ).toDetailUi(isFavorite = false)
        compose.setContent {
            TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}, onOpenWikipedia = { opened = it }) }
        }
        compose.onNodeWithText("Platane commun · fr.wikipedia.org").assertIsDisplayed()
        compose.onNodeWithText("En savoir plus sur Wikipédia").performClick()
        assertEquals("https://fr.wikipedia.org/wiki/Platane_commun", opened)
    }

    @Test
    fun wikipediaRow_saysWhenItIsTheGenusArticle() {
        val detail =
            tree(1).copy(wikipedia = WikipediaArticle("Platane", isGenusArticle = true)).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("Le genre Platanus sur Wikipédia").assertIsDisplayed()
    }

    @Test
    fun wikipediaRow_absentWithoutArticle() {
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("En savoir plus sur Wikipédia").assertDoesNotExist()
        compose.onNodeWithText("sur Wikipédia", substring = true).assertDoesNotExist()
    }
}
