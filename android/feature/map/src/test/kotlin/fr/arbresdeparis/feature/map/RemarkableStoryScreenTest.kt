package fr.arbresdeparis.feature.map

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.feature.map.mapper.toDetailUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** « Son histoire »: the page a remarkable tree's sheet opens. */
@RunWith(RobolectricTestRunner::class)
class RemarkableStoryScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val opened = mutableListOf<String>()
    private var backs = 0

    private fun show(story: RemarkableStory = story(1)) {
        val detail = tree(1).copy(remarkable = true).toDetailUi(isFavorite = false, story = story)
        compose.setContent {
            TreesTheme {
                RemarkableStoryScreen(detail = detail, onBack = { backs++ }, onOpenUrl = { opened += it })
            }
        }
    }

    @Test
    fun tellsWhyAndTheStory_withThePhotoCredit() {
        show()
        compose.onNodeWithText("© Sonia Yassa / Ville de Paris").assertExists()
        compose.onNodeWithText("Pourquoi il est remarquable".uppercase()).assertExists()
        compose.onNodeWithText("Cet arbre est classé remarquable pour son ampleur et son ancienneté.").assertExists()
        compose.onNodeWithText("Son histoire").assertExists()
        compose.onNodeWithText("Planté en 1879", substring = true).assertExists()
    }

    @Test
    fun showsThePlantingYearLabelAndCouncilDecision() {
        show()
        compose.onNodeWithText("1882").performScrollTo()
        compose.onNodeWithText("A.R.B.R.E.S en 2015").performScrollTo()
        compose.onNodeWithText("Conseil de Paris 2015 DEVE 152").performScrollTo()
    }

    @Test
    fun signOpensThePdf_andBackGoesBack() {
        show()
        compose.onNodeWithText("Le panneau de l’arbre").performScrollTo().performClick()
        assertEquals(listOf("https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/124914.pdf"), opened)
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun whileLoading_showsThePageWithItsBackButton() {
        compose.setContent {
            TreesTheme { RemarkableStoryPage(RemarkableStoryUiState.Loading, onBack = { backs++ }, onOpenUrl = {}) }
        }
        compose.onNodeWithTag(STORY_LOADING_TAG).assertExists()
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun withoutStory_saysSo() {
        compose.setContent {
            TreesTheme { RemarkableStoryPage(RemarkableStoryUiState.Unavailable, onBack = {}, onOpenUrl = {}) }
        }
        compose.onNodeWithText("L’histoire de cet arbre n’est pas disponible.").assertExists()
        compose.onNodeWithTag(STORY_LOADING_TAG).assertDoesNotExist()
    }

    @Test
    fun photo_opensInFullScreen_withItsCredit_andClosesAgain() {
        show()
        compose.onNodeWithContentDescription("Voir la photo en plein écran").performClick()
        compose.onNodeWithTag(PHOTO_VIEWER_TAG).assertIsDisplayed()
        compose.onAllNodesWithText("© Sonia Yassa / Ville de Paris").assertCountEquals(2) // the page's and the viewer's
        compose.onNodeWithContentDescription("Fermer").performClick()
        compose.onNodeWithTag(PHOTO_VIEWER_TAG).assertDoesNotExist()
    }

    @Test
    fun withoutPhoto_nothingToOpen() {
        show(story(1).copy(photoUrl = null))
        compose.onNodeWithContentDescription("Voir la photo en plein écran").assertDoesNotExist()
    }
}
