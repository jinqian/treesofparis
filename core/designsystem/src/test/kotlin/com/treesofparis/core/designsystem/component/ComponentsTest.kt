package com.treesofparis.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.HerbierPalette
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

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun theme_usesHerbierPaperAndGold() {
        var background = HerbierPalette.Ink
        var remarkable = HerbierPalette.Ink
        compose.setContent {
            TreesTheme(darkTheme = false) {
                background = MaterialTheme.colorScheme.background
                remarkable = TreesTheme.colors.remarkable
            }
        }
        assertEquals(HerbierPalette.Paper, background)
        assertEquals(HerbierPalette.Gold, remarkable)
    }

    @Test
    fun emptyState_showsMessage() {
        compose.setContent { TreesTheme { EmptyState(message = "Aucun favori pour le moment") } }
        compose.onNodeWithText("Aucun favori pour le moment").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsTitleAndActionInvokesCallback() {
        var clicks = 0
        compose.setContent {
            TreesTheme {
                EmptyState(
                    title = "Aucun favori",
                    message = "Touchez un arbre",
                    actionLabel = "Explorer la carte",
                    onAction = { clicks++ },
                    illustration = { SprigIllustration() },
                )
            }
        }
        compose.onNodeWithText("Aucun favori").assert(isHeading)
        compose.onNodeWithText("Explorer la carte").performClick()
        assertEquals(1, clicks)
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
        compose.setContent {
            TreesTheme { TreeListItem(title = "Platane", subtitle = "Paris 19e", onClick = {}, remarkable = true) }
        }
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
    fun sectionHeader_isAnUppercaseHeading() {
        compose.setContent { TreesTheme { SectionHeader(text = "Données") } }
        compose.onNodeWithText("DONNÉES").assertIsDisplayed().assert(isHeading)
    }

    @Test
    fun treesScaffold_showsTitleAsHeadingSubtitleAndContent() {
        compose.setContent {
            TreesTheme {
                TreesScaffold(title = "Favoris", subtitle = "2 arbres") { EmptyState(message = "Vide") }
            }
        }
        compose.onNodeWithText("Favoris").assertIsDisplayed().assert(isHeading)
        compose.onNodeWithText("2 arbres").assertIsDisplayed()
        compose.onNodeWithText("Vide").assertIsDisplayed()
    }

    @Test
    fun infoRow_insideCardGroup_isClickable() {
        var clicks = 0
        compose.setContent {
            TreesTheme {
                TreesCardGroup {
                    InfoRow(title = "Licence ODbL", trailing = InfoRowTrailing.EXTERNAL, onClick = { clicks++ })
                    TreesDivider()
                    InfoRow(title = "© OpenStreetMap", subtitle = "osmdroid")
                }
            }
        }
        compose.onNodeWithText("osmdroid").assertIsDisplayed()
        compose.onNodeWithText("Licence ODbL").performClick()
        assertEquals(1, clicks)
    }
}
