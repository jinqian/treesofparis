package com.treesofparis.feature.map

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
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
class RemarkableInfoContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val chosen = mutableListOf<Boolean>()
    private var dismissed = 0

    private fun show() =
        compose.setContent {
            TreesTheme {
                RemarkableInfoContent(onShowRemarkableOnly = { chosen += true }, onClose = { dismissed++ })
            }
        }

    @Test
    fun explainsWithTitleAndLegend() {
        show()
        compose
            .onNodeWithText("Qu’est-ce qu’un arbre remarquable ?")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("Arbre").assertIsDisplayed()
        compose.onNodeWithText("Arbre remarquable").assertIsDisplayed()
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
