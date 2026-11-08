package com.treesofparis.feature.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.domain.model.AppInfo
import com.treesofparis.domain.model.DatasetInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class AboutTest {
    @get:Rule
    val compose = createComposeRule()

    private val state =
        AboutUiState(
            versionName = "0.1.0",
            extraction = ExtractionUi(date = "6 novembre 2026", treeCount = "220\u202F339"),
        )

    @Test
    fun viewModel_exposesVersionAndExtraction() {
        val dataset = DatasetInfo(extractedAt = Instant.parse("2026-11-06T16:30:03Z"), treeCount = 220_339)
        val ui = AboutViewModel(AppInfo("0.1.0"), dataset).uiState
        assertEquals("0.1.0", ui.versionName)
        assertEquals(ExtractionUi(date = "6 novembre 2026", treeCount = "220\u202F339"), ui.extraction)
    }

    @Test
    fun extraction_isTheParisDate() {
        // 23:30 UTC on 5 November is already 6 November in Paris.
        val late = DatasetInfo(extractedAt = Instant.parse("2026-11-05T23:30:00Z"), treeCount = 1)
        assertEquals("6 novembre 2026", late.toExtractionUi()?.date)
        assertNull(DatasetInfo(extractedAt = null, treeCount = 1).toExtractionUi())
    }

    @Test
    fun aboutScreen_showsTheDataExtraction() {
        compose.setContent { TreesTheme { AboutScreen(state, onOpenUrl = {}, onOpenLibraries = {}) } }
        compose.onNodeWithText("Extraction du 6 novembre 2026 · 220\u202F339 arbres").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_showsVersionAndCredits() {
        compose.setContent { TreesTheme { AboutScreen(state, onOpenUrl = {}, onOpenLibraries = {}) } }
        compose.onNodeWithText("VERSION 0.1.0").assertIsDisplayed()
        compose.onNodeWithText("« Les arbres », Paris Data — Ville de Paris").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_showsOsmAttribution() {
        compose.setContent { TreesTheme { AboutScreen(state, onOpenUrl = {}, onOpenLibraries = {}) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("© les contributeurs d’OpenStreetMap"))
        compose.onNodeWithText("© les contributeurs d’OpenStreetMap").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_creditsBundledFonts() {
        compose.setContent { TreesTheme { AboutScreen(state, onOpenUrl = {}, onOpenLibraries = {}) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Newsreader et Inter"))
        compose.onNodeWithText("SIL Open Font License 1.1").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_opensTheLibrariesScreen() {
        var opened = 0
        compose.setContent { TreesTheme { AboutScreen(state, onOpenUrl = {}, onOpenLibraries = { opened++ }) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Bibliothèques open source"))
        compose.onNodeWithText("Bibliothèques open source").performClick()
        assertEquals(1, opened)
        compose.onNodeWithText("osmdroid").assertDoesNotExist()
    }

    @Test
    fun librariesScreen_listsLibrariesAndOpensTheirPage() {
        var opened: String? = null
        val libs =
            listOf(LibraryUi(name = "osmdroid", detail = "6.1.20 · Apache 2.0", url = "https://github.com/osmdroid"))
        compose.setContent {
            TreesTheme { LibrariesScreen(libraries = libs, onOpenUrl = { opened = it }, onBack = {}) }
        }
        compose.onNodeWithText("Bibliothèques open source").assertIsDisplayed()
        compose.onNodeWithTag(LIBRARIES_LIST_TAG).performScrollToNode(hasText("osmdroid"))
        compose.onNodeWithText("osmdroid").performClick()
        assertEquals("https://github.com/osmdroid", opened)
    }

    @Test
    fun librariesScreen_backButtonGoesBack() {
        var backs = 0
        compose.setContent {
            TreesTheme {
                LibrariesScreen(
                    libraries = emptyList(),
                    onOpenUrl = {},
                    onBack = { backs++ },
                )
            }
        }
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }
}
