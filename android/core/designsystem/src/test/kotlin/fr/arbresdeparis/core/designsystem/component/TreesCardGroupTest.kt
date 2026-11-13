package fr.arbresdeparis.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Issue #67: a row's ripple must fill the card up to its rounded edge, with no strip left above or below. */
@RunWith(RobolectricTestRunner::class)
class TreesCardGroupTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun firstAndLastRows_reachTheCardEdges() {
        compose.setContent {
            TreesTheme {
                TreesCardGroup(modifier = Modifier.testTag(CARD)) {
                    InfoRow(title = "Nous écrire", onClick = {}, modifier = Modifier.testTag(FIRST))
                    TreesDivider()
                    InfoRow(title = "Site web", onClick = {}, modifier = Modifier.testTag(LAST))
                }
            }
        }
        val card = compose.onNodeWithTag(CARD).getBoundsInRoot()
        assertEquals(card.top, compose.onNodeWithTag(FIRST).getBoundsInRoot().top)
        assertEquals(card.bottom, compose.onNodeWithTag(LAST).getBoundsInRoot().bottom)
    }

    private companion object {
        const val CARD = "card"
        const val FIRST = "first"
        const val LAST = "last"
    }
}
