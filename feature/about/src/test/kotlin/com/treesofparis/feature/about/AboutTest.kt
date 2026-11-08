package com.treesofparis.feature.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.domain.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutTest {
    @get:Rule
    val compose = createComposeRule()

    private val state = AboutUiState(versionName = "0.1.0")

    @Test
    fun viewModel_exposesVersion() {
        assertEquals("0.1.0", AboutViewModel(AppInfo("0.1.0")).uiState.versionName)
    }

    @Test
    fun aboutScreen_showsVersionAndCredits() {
        compose.setContent { TreesTheme { AboutScreen(state, libraries = emptyList(), onOpenUrl = {}) } }
        compose.onNodeWithText("VERSION 0.1.0").assertIsDisplayed()
        compose.onNodeWithText("« Les arbres », Paris Data — Ville de Paris").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_showsOsmAttribution() {
        compose.setContent { TreesTheme { AboutScreen(state, libraries = emptyList(), onOpenUrl = {}) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("© les contributeurs d’OpenStreetMap"))
        compose.onNodeWithText("© les contributeurs d’OpenStreetMap").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_creditsBundledFonts() {
        compose.setContent { TreesTheme { AboutScreen(state, libraries = emptyList(), onOpenUrl = {}) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Newsreader et Inter"))
        compose.onNodeWithText("SIL Open Font License 1.1").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_listsLibrariesAndOpensTheirPage() {
        var opened: String? = null
        val libs =
            listOf(LibraryUi(name = "osmdroid", detail = "6.1.20 · Apache 2.0", url = "https://github.com/osmdroid"))
        compose.setContent { TreesTheme { AboutScreen(state, libraries = libs, onOpenUrl = { opened = it }) } }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("osmdroid"))
        compose.onNodeWithText("osmdroid").performClick()
        assertEquals("https://github.com/osmdroid", opened)
    }
}
