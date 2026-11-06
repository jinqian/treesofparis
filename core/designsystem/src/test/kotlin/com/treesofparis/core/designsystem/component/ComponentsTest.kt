package com.treesofparis.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComponentsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyState_showsMessage() {
        compose.setContent { TreesTheme { EmptyState(message = "Aucun favori pour le moment") } }
        compose.onNodeWithText("Aucun favori pour le moment").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retries = 0
        compose.setContent { TreesTheme { ErrorState(message = "Oups", onRetry = { retries++ }) } }
        compose.onNodeWithText("Oups").assertIsDisplayed()
        compose.onNodeWithText("Réessayer").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun errorState_withoutRetry_hidesButton() {
        compose.setContent { TreesTheme { ErrorState(message = "Oups", onRetry = null) } }
        compose.onNodeWithText("Réessayer").assertDoesNotExist()
    }

    @Test
    fun treeListItem_showsTitleAndSubtitle() {
        compose.setContent { TreesTheme { TreeListItem(title = "Platane", subtitle = "Paris 19e", onClick = {}) } }
        compose.onNodeWithText("Platane").assertIsDisplayed()
        compose.onNodeWithText("Paris 19e").assertIsDisplayed()
    }

    @Test
    fun treeListItem_click() {
        var clicks = 0
        compose.setContent {
            TreesTheme {
                TreeListItem(
                    title = "Platane",
                    subtitle = "Paris 19e",
                    onClick = { clicks++ },
                )
            }
        }
        compose.onNodeWithText("Platane").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun sectionHeader_showsText() {
        compose.setContent { TreesTheme { SectionHeader(text = "Données") } }
        compose.onNodeWithText("Données").assertIsDisplayed()
    }

    @Test
    fun treesScaffold_showsTitleAndContent() {
        compose.setContent {
            TreesTheme { TreesScaffold(title = "Favoris") { EmptyState(message = "Vide") } }
        }
        compose.onNodeWithText("Favoris").assertIsDisplayed()
        compose.onNodeWithText("Vide").assertIsDisplayed()
    }
}
