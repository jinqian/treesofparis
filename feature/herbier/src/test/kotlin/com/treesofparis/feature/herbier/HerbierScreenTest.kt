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
import com.treesofparis.feature.herbier.mapper.toFloweringCalendarUi
import com.treesofparis.feature.herbier.mapper.toHerbierUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Month

/** Level 1 of the Herbier: the headline, the season and four cards. */
@RunWith(RobolectricTestRunner::class)
class HerbierScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val opened = mutableListOf<HerbierSection>()

    private fun show(month: Month = Month.NOVEMBER) =
        compose.setContent {
            TreesTheme {
                HerbierScreen(
                    state = content(month),
                    onOpenSection = { opened += it },
                )
            }
        }

    private fun scrollTo(text: String) = compose.onNodeWithTag(HERBIER_LIST_TAG).performScrollToNode(hasText(text))

    @Test
    fun titleTotalsAndHeadline() {
        show()
        compose.onNodeWithText("Herbier").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("220\u202F339 arbres · 202 genres · 1\u202F004 espèces et variétés").assertIsDisplayed()
        compose.onNodeWithText("1 arbre sur 5 à Paris est un platane.").assertIsDisplayed()
    }

    @Test
    fun seasonCard_inNovember_waitsForFebruary() {
        show()
        scrollTo("Les arbres se reposent.")
        compose.onNodeWithText("EN FLEUR EN NOVEMBRE").assertIsDisplayed()
        compose.onNodeWithText("Prochaines floraisons dès février : noisetier et orme.").assertIsDisplayed()
        compose.onNodeWithText("Les arbres se reposent.").performClick()
        assertEquals(listOf(HerbierSection.FLOWERING), opened)
    }

    @Test
    fun seasonCard_inMay_namesWhatFlowers() {
        show(Month.MAY)
        scrollTo("3 essences en fleur")
        compose.onNodeWithText("Platane · Marronnier · Tilleul").assertIsDisplayed()
    }

    @Test
    fun exploreCards_openTheirSection() {
        show()
        scrollTo("Âge et quartiers")
        compose.onNodeWithText("12 essences dessinées, 74\u202F% des arbres").assertExists()
        compose.onNodeWithText("Le plus haut mesure 45 m").assertExists()
        compose.onNodeWithText("Les essences").performClick()
        compose.onNodeWithText("Floraisons").performClick()
        compose.onNodeWithText("Remarquables").performClick()
        compose.onNodeWithText("Âge et quartiers").performClick()
        assertEquals(HerbierSection.entries, opened)
    }
}

fun content(month: Month = Month.NOVEMBER) =
    HerbierUiState.Content(
        herbier = PARIS.toHerbierUi(),
        flowering = SEASONS.toFloweringCalendarUi(month, PARIS.genera.map { it.genus }),
    )
