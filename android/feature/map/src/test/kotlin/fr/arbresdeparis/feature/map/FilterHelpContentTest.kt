package fr.arbresdeparis.feature.map

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilterHelpContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val chosen = mutableListOf<Boolean>()
    private var dismissed = 0

    private fun show() =
        compose.setContent {
            TreesTheme {
                FilterHelpContent(onShowRemarkableOnly = { chosen += true }, onClose = { dismissed++ })
            }
        }

    @Test
    fun explainsBothFilters_briefly() {
        show()
        compose
            .onNodeWithText("Les filtres de la carte")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("Tous les arbres").assertIsDisplayed()
        compose.onNodeWithText("Touchez pour ne garder qu’une essence.").assertIsDisplayed()
        compose.onNodeWithText("Remarquables").assertIsDisplayed()
        compose.onNodeWithText("Les deux filtres se combinent.").assertIsDisplayed()
    }

    @Test
    fun filterDots_haveTheSameSize() {
        show()
        val sizes =
            compose
                .onAllNodesWithTag(
                    LEGEND_DOT_TAG,
                    useUnmergedTree = true,
                ).fetchSemanticsNodes()
                .map { it.size }
        assertEquals(2, sizes.size)
        assertEquals(1, sizes.distinct().size)
    }

    @Test
    fun showRemarkableOnly_choosesTheFilter() {
        show()
        compose.onNodeWithText("Voir seulement les remarquables").performClick()
        assertEquals(listOf(true), chosen)
    }

    @Test
    fun close_dismisses() {
        show()
        compose.onNodeWithText("Fermer").performClick()
        assertEquals(1, dismissed)
    }
}
