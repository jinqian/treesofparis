package com.treesofparis.feature.herbier

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.feature.herbier.mapper.toHerbierUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HerbierScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private var showAllClicks = 0

    private fun show(showAll: Boolean = false) =
        compose.setContent {
            TreesTheme {
                HerbierScreen(
                    state = HerbierUiState.Content(PARIS.toHerbierUi(), showAllGenera = showAll),
                    onShowAllGenera = { showAllClicks++ },
                )
            }
        }

    private fun scrollTo(text: String) = compose.onNodeWithTag(HERBIER_LIST_TAG).performScrollToNode(hasText(text))

    @Test
    fun titleTotalsAndHeadline() {
        show()
        compose.onNodeWithText("Herbier").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("220 339 arbres · 202 genres · 1 004 espèces et variétés").assertIsDisplayed()
        compose.onNodeWithText("1 arbre sur 5 est un platane.").assertIsDisplayed()
    }

    @Test
    fun genera_showTenThenOfferTheRest() {
        show()
        scrollTo("Voir les 12 essences dessinées")
        // Pin is the 10th drawn genus in the fixture; Poirier, the 11th, waits for « Voir les… ».
        compose.onNodeWithText("Pin").assertExists()
        compose.onNodeWithText("Poirier").assertDoesNotExist()
        compose.onNodeWithText("Voir les 12 essences dessinées").performClick()
        assertEquals(1, showAllClicks)
    }

    @Test
    fun genera_allShownOnceExpanded() {
        show(showAll = true)
        scrollTo("Hêtre")
        compose.onNodeWithText("Hêtre").assertIsDisplayed()
        compose.onNodeWithText("Voir les 12 essences dessinées").assertDoesNotExist()
    }

    @Test
    fun remarkableAndRecords() {
        show()
        scrollTo("Platane · 7,40 m de tour")
        compose.onNodeWithText("181 arbres remarquables, dont 44 platanes").assertIsDisplayed()
        compose.onNodeWithText("Platane · 45 m").assertIsDisplayed()
        compose.onNodeWithText("Paris 19e · Parc des Buttes Chaumont").assertExists()
    }

    @Test
    fun mostWoodedArrondissements() {
        show()
        scrollTo("Paris 16e")
        compose.onNodeWithText("Paris 15e").assertIsDisplayed()
    }
}
