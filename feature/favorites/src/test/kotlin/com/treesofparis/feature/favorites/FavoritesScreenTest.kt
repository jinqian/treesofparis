package com.treesofparis.feature.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoritesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val content =
        FavoritesUiState.Content(
            listOf(FavoriteUiModel(7, "Platane", "Quai de la Seine · Paris 19e", remarkable = true)),
        )

    @Test
    fun empty_showsMessageAndExploreActionOpensMap() {
        var explored = 0
        compose.setContent {
            TreesTheme {
                FavoritesScreen(FavoritesUiState.Empty, onTreeClick = {}, onRemove = {}, onExploreMap = { explored++ })
            }
        }
        compose.onNodeWithText("Favoris").assertIsDisplayed()
        compose.onNodeWithText("Aucun favori pour le moment").assertIsDisplayed()
        compose.onNodeWithText("Explorer la carte").performClick()
        assertEquals(1, explored)
    }

    @Test
    fun content_showsCountUnderTitle() {
        compose.setContent { TreesTheme { FavoritesScreen(content, onTreeClick = {}, onRemove = {}) } }
        compose.onNodeWithText("1 arbre").assertIsDisplayed()
    }

    @Test
    fun itemClick_reportsTreeId() {
        var clicked: Long? = null
        compose.setContent { TreesTheme { FavoritesScreen(content, onTreeClick = { clicked = it }, onRemove = {}) } }
        compose.onNodeWithText("Platane").performClick()
        assertEquals(7L, clicked)
    }

    @Test
    fun removeButton_reportsTreeId() {
        var removed: Long? = null
        compose.setContent { TreesTheme { FavoritesScreen(content, onTreeClick = {}, onRemove = { removed = it }) } }
        compose.onNodeWithContentDescription("Retirer Platane des favoris").performClick()
        assertEquals(7L, removed)
    }
}
