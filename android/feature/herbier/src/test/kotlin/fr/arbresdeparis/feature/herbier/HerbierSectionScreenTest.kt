package fr.arbresdeparis.feature.herbier

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Level 2 of the Herbier: one screen per card, with a back button. */
@RunWith(RobolectricTestRunner::class)
class HerbierSectionScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val genusClicks = mutableListOf<String>()
    private val treeClicks = mutableListOf<Long>()
    private var backs = 0

    private fun show(section: HerbierSection) =
        compose.setContent {
            TreesTheme {
                HerbierSectionScreen(
                    section = section,
                    state = content(),
                    onBack = { backs++ },
                    onGenusClick = { genusClicks += it },
                    onTreeClick = { treeClicks += it },
                )
            }
        }

    private fun scrollTo(text: String) = compose.onNodeWithTag(HERBIER_LIST_TAG).performScrollToNode(hasText(text))

    @Test
    fun genera_listsEveryDrawnGenus_andOpensItOnTheMap() {
        show(HerbierSection.GENERA)
        compose.onNodeWithText("12 essences dessinées · 74\u202F% des arbres").assertIsDisplayed()
        scrollTo("Hêtre")
        compose.onNodeWithText("Hêtre").assertIsDisplayed()
        scrollTo("Tilleul")
        compose.onNodeWithText("Tilleul").performClick()
        assertEquals(listOf("Tilia"), genusClicks)
    }

    @Test
    fun flowering_showsTheCalendar() {
        show(HerbierSection.FLOWERING)
        compose.onNodeWithText("Floraisons").assertIsDisplayed()
        compose
            .onNodeWithTag(HERBIER_LIST_TAG)
            .performScrollToNode(hasContentDescription("Cerisier, prunier : mars – avril, fleurs voyantes"))
        compose.onNodeWithContentDescription("Cerisier, prunier : mars – avril, fleurs voyantes").assertIsDisplayed()
        scrollTo("Fleurs discrètes")
        compose.onNodeWithText("Fleurs discrètes").assertIsDisplayed()
    }

    @Test
    fun remarkable_showsCountAndRecords() {
        show(HerbierSection.REMARKABLE)
        scrollTo("Platane · 7,40 m de tour")
        compose.onNodeWithText("181 arbres remarquables, dont 44 platanes").assertExists()
        compose.onNodeWithText("Platane · 45 m").assertExists()
    }

    @Test
    fun remarkable_listsEveryTreeByArea_andOpensItOnTheMap() {
        show(HerbierSection.REMARKABLE)
        scrollTo("BOIS DE BOULOGNE")
        scrollTo("PARIS 6E")
        compose.onNodeWithText("PARIS 6E").assertExists()
        scrollTo("Jardin du Luxembourg")
        compose.onNodeWithText("Marronnier").performClick()
        assertEquals(listOf(13L), treeClicks)
    }

    @Test
    fun ageAndAreas_showStagesAndArrondissements() {
        show(HerbierSection.AGE_AND_AREAS)
        compose.onNodeWithText("Jeune adulte").assertIsDisplayed()
        scrollTo("Paris 16e")
        compose.onNodeWithText("Paris 15e").assertIsDisplayed()
    }

    @Test
    fun backButton_goesBack() {
        show(HerbierSection.REMARKABLE)
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }
}
