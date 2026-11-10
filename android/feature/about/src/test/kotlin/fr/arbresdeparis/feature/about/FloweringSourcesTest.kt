package fr.arbresdeparis.feature.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.repository.FloweringRepository
import fr.arbresdeparis.domain.usecase.GetFloweringSources
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Month

@RunWith(RobolectricTestRunner::class)
class FloweringSourcesTest {
    @get:Rule
    val compose = createComposeRule()

    private fun sheet(
        scientificName: String,
        commonName: String,
        months: IntRange,
        showy: Boolean = false,
    ) = FloweringSheet(
        genus = scientificName.substringBefore(' '),
        scientificName = scientificName,
        commonName = commonName,
        months = months.map(Month::of).toSet(),
        showyFlowers = showy,
        url = "https://opendata.paris.fr/$scientificName.pdf",
    )

    private val sources =
        GetFloweringSources(
            object : FloweringRepository {
                override fun sheets() =
                    listOf(
                        sheet("Ulmus minor", "Orme champêtre", 2..4),
                        sheet("Acer campestre", "Érable champêtre", 5..5),
                        sheet("Prunus avium", "Merisier", 3..4, showy = true),
                    )
            },
        )

    @Test
    fun mapper_writesTheMonthsInFrench() {
        val ui = sources().map { it.toUi() }
        assertEquals(
            listOf(
                "Acer campestre · mai",
                "Prunus avium · mars – avril · fleurs voyantes",
                "Ulmus minor · février – avril",
            ),
            ui.map { it.detail },
        )
    }

    @Test
    fun viewModel_listsTheSheetsByCommonName() {
        val names = FloweringSourcesViewModel(sources).sources.map { it.commonName }
        assertEquals(listOf("Érable champêtre", "Merisier", "Orme champêtre"), names)
    }

    @Test
    fun screen_listsTheSheetsAndOpensTheirPdf() {
        var opened: String? = null
        val ui = sources().map { it.toUi() }
        compose.setContent {
            TreesTheme { FloweringSourcesScreen(sources = ui, onOpenUrl = { opened = it }, onBack = {}) }
        }
        compose.onNodeWithText("Sources des floraisons").assertIsDisplayed()
        compose.onNodeWithTag(FLOWERING_SOURCES_LIST_TAG).performScrollToNode(hasText("Merisier"))
        compose.onNodeWithText("Merisier").performClick()
        assertEquals("https://opendata.paris.fr/Prunus avium.pdf", opened)
    }

    @Test
    fun screen_backButtonGoesBack() {
        var backs = 0
        compose.setContent {
            TreesTheme { FloweringSourcesScreen(sources = emptyList(), onOpenUrl = {}, onBack = { backs++ }) }
        }
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }
}
