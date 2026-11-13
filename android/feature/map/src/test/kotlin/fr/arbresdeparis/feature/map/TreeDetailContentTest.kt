package fr.arbresdeparis.feature.map

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.WikipediaArticle
import fr.arbresdeparis.feature.map.mapper.toDetailUi
import fr.arbresdeparis.feature.map.mapper.toFloweringUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Month

@RunWith(RobolectricTestRunner::class)
class TreeDetailContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun missingFields_showFrenchPlaceholders() {
        val detail = tree(1).copy(genus = null, species = null, heightM = null).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("Espèce inconnue").assertIsDisplayed()
        // Under the « Hauteur » label, « inconnue » alone is enough.
        compose.onNodeWithText("inconnue").assertIsDisplayed()
        compose.onNodeWithText("Hauteur inconnue").assertDoesNotExist()
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
    fun flowering_showsTheRangeKindAndWait() {
        val season = FloweringSeason("Platanus", setOf(Month.MAY, Month.JUNE), showyFlowers = false)
        val detail = tree(1).toDetailUi(isFavorite = false, flowering = season.toFloweringUi(Month.NOVEMBER))
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("FLORAISON").assertIsDisplayed()
        compose.onNodeWithText("mai – juin · fleurs discrètes").assertIsDisplayed()
        compose.onNodeWithText("Nous sommes en novembre : prochaine floraison dans 6 mois.").assertIsDisplayed()
    }

    @Test
    fun flowering_inSeason_saysSo() {
        val season = FloweringSeason("Prunus", setOf(Month.MARCH, Month.APRIL), showyFlowers = true)
        val detail = tree(1).toDetailUi(isFavorite = false, flowering = season.toFloweringUi(Month.APRIL))
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("mars – avril · fleurs voyantes").assertIsDisplayed()
        compose.onNodeWithText("En fleur ce mois-ci.").assertIsDisplayed()
    }

    @Test
    fun noFlowering_noSection() {
        compose.setContent {
            TreesTheme {
                TreeDetailContent(
                    detail = tree(1).toDetailUi(false),
                    onFavoriteToggle = {},
                )
            }
        }
        compose.onNodeWithText("FLORAISON").assertDoesNotExist()
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
    fun addressRow_opensTheTreeLocation_fromAnywhereOnTheRow() {
        var opened: GeoPoint? = null
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent {
            TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}, onOpenDirections = { opened = it }) }
        }
        compose.onNodeWithText("Quai de la Seine", substring = true).performClick()
        assertEquals(detail.location, opened)
    }

    @Test
    fun addressRow_isATouchTarget_withoutMakingTheSheetTaller() {
        val detail = tree(1).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        val header = compose.onNodeWithTag(DETAIL_HEADER_TAG).getUnclippedBoundsInRoot()
        val row = compose.onNodeWithTag(DETAIL_LOCATION_TAG).getUnclippedBoundsInRoot()
        val stats = compose.onNodeWithTag(DETAIL_STATS_TAG).getUnclippedBoundsInRoot()
        assertTrue("touch target ${row.bottom - row.top}", row.bottom - row.top >= 48.dp)
        // Before: 20 dp, a 22 dp address line, 20 dp. The sheet keeps that height.
        assertEquals(62f, (stats.top - header.bottom).value, 0.5f)
    }

    @Test
    fun longAddress_takesAtMostTwoLines() {
        val address =
            "Decoration de la Place de l'Hôtel de Ville / Esplanade de la Libération, Fontaine Côté Rivoli"
        val detail = tree(1).toDetailUi(isFavorite = false).copy(address = address)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        val layouts = mutableListOf<TextLayoutResult>()
        compose
            .onNodeWithText(address, substring = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(layouts)
        assertTrue("lines: ${layouts.single().lineCount}", layouts.single().lineCount <= 2)
        val row = compose.onNodeWithTag(DETAIL_LOCATION_TAG).getUnclippedBoundsInRoot()
        assertEquals(48f, (row.bottom - row.top).value, 0.5f) // still the 48 dp row: the sheet does not grow
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

    @Test
    fun remarkableWithStory_tellsOneSentenceAndOpensItsStory() {
        var opened = 0
        val detail = tree(1).copy(remarkable = true).toDetailUi(isFavorite = false, story = story(1))
        compose.setContent {
            TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}, onOpenStory = { opened++ }) }
        }
        compose.onNodeWithText("ARBRE REMARQUABLE · PAYSAGER").assertIsDisplayed()
        compose.onNodeWithText("Square Maurice Gardette", substring = true).assertIsDisplayed()
        // The summary is nearly always a formula, and the header already says why: the sheet tells the story instead.
        compose
            .onNodeWithText(
                "Cet arbre est classé remarquable pour son ampleur et son ancienneté.",
            ).assertDoesNotExist()
        compose
            .onNodeWithText(
                "Planté en 1879, cet arbre fait aujourd’hui l’attraction de ce lieu riche d’histoire.",
            ).assertExists()
        compose.onNodeWithText("Son histoire").performScrollTo().performClick()
        assertEquals(1, opened)
        compose.onNodeWithText("« Les arbres remarquables »", substring = true).assertExists()
    }

    @Test
    fun remarkableWithoutStory_hasNoStoryCard() {
        val detail = tree(1).copy(remarkable = true).toDetailUi(isFavorite = false)
        compose.setContent { TreesTheme { TreeDetailContent(detail = detail, onFavoriteToggle = {}) } }
        compose.onNodeWithText("Son histoire").assertDoesNotExist()
    }
}
