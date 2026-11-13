package fr.arbresdeparis.feature.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CrashReportingQuestionTest {
    @get:Rule
    val compose = createComposeRule()

    private val answers = mutableListOf<Boolean>()

    private fun show() =
        compose.setContent {
            TreesTheme { CrashReportingQuestion(onAnswer = { answers += it }) }
        }

    @Test
    fun saysWhatIsSentAndWhereToChangeIt() {
        show()
        compose.onNodeWithText("Envoyer les rapports de plantage ?").assertIsDisplayed()
        compose.onNodeWithText("jamais votre position ni vos favoris", substring = true).assertIsDisplayed()
        compose.onNodeWithText("À\u00A0propos", substring = true).assertIsDisplayed()
    }

    @Test
    fun envoyer_answersYes() {
        show()
        compose.onNodeWithText("Envoyer").performClick()
        assertEquals(listOf(true), answers)
    }

    @Test
    fun nePasEnvoyer_answersNo() {
        show()
        compose.onNodeWithText("Ne pas envoyer").performClick()
        assertEquals(listOf(false), answers)
    }
}
