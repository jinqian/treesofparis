package fr.arbresdeparis.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import com.github.takahirom.roborazzi.captureRoboImage
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The map controls and sheets on the smallest phones testers use: a 320 dp wide screen, and a 360 dp one with the
 * system text at 130 %. Every text must fit (no ellipsis, no overflow) and no word may be cut across two lines.
 *
 * The 360 dp phone at normal text size is checked for the leaf grid too (three cards a row). The checks run with the
 * unit tests. The screenshots are taken with `./gradlew :feature:map:recordRoborazziDebug`
 * (written to `src/test/screenshots/`) and compared with `verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SmallScreenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = NARROW)
    fun filterBar_narrow() = filterBar("narrow")

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun filterBar_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        filterBar("large_text")
    }

    @Test
    @Config(qualifiers = NARROW)
    fun treeDetail_narrow() = treeDetail("narrow")

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun treeDetail_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        treeDetail("large_text")
    }

    @Test
    @Config(qualifiers = NARROW)
    fun treeDetailStory_narrow() = treeDetail("story_narrow", withStory = true)

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun treeDetailStory_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        treeDetail("story_large_text", withStory = true)
    }

    @Test
    @Config(qualifiers = NARROW)
    fun remarkableStory_narrow() = remarkableStory("narrow")

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun remarkableStory_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        remarkableStory("large_text")
    }

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun remarkableStoryUnavailable_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        show { RemarkableStoryPage(RemarkableStoryUiState.Unavailable, onBack = {}, onOpenUrl = {}) }
        check("remarkable_story_unavailable_large_text")
    }

    @Test
    @Config(qualifiers = LARGE_TEXT)
    fun storyPhotoViewer_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        show { StoryPhotoViewer(micocoulier(story = STORY), onClose = {}) }
        check("story_photo_viewer_large_text")
    }

    @Test
    @Config(qualifiers = NARROW_TALL)
    fun genusPicker_narrow() = genusPicker("narrow")

    /** A common 360 dp phone keeps three leaf cards a row: the names must still fit there. */
    @Test
    @Config(qualifiers = REGULAR_TALL)
    fun genusPicker_regular() = genusPicker("regular")

    @Test
    @Config(qualifiers = LARGE_TEXT_TALL)
    fun genusPicker_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        genusPicker("large_text")
    }

    @Test
    @Config(qualifiers = NARROW_TALL)
    fun filterHelp_narrow() = filterHelp("narrow")

    @Test
    @Config(qualifiers = LARGE_TEXT_TALL)
    fun filterHelp_largeText() {
        RuntimeEnvironment.setFontScale(LARGE_FONT_SCALE)
        filterHelp("large_text")
    }

    private fun filterHelp(variant: String) {
        show { FilterHelpContent(onShowRemarkableOnly = {}, onClose = {}) }
        check("filter_help_$variant")
    }

    private fun filterBar(variant: String) {
        show {
            Column(
                Modifier.padding(vertical = TreesSpacing.m),
                verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            ) {
                MapFilterChips(
                    remarkableOnly = false,
                    genusFilter = null,
                    onRemarkableFilterChange = {},
                    onGenusPickerOpen = {},
                    onGenusFilterClear = {},
                    onHelpOpen = {},
                )
                MapFilterChips(
                    remarkableOnly = true,
                    genusFilter = GenusFilterUi("Celtis", TreeLeaf.CELTIS),
                    onRemarkableFilterChange = {},
                    onGenusPickerOpen = {},
                    onGenusFilterClear = {},
                    onHelpOpen = {},
                )
                RemarkableOnlyCaption(Modifier.padding(horizontal = TreesSpacing.m))
            }
        }
        check("filter_bar_$variant")
    }

    private fun treeDetail(
        variant: String,
        withStory: Boolean = false,
    ) {
        show {
            TreeDetailContent(
                detail = micocoulier(story = if (withStory) STORY else null),
                onFavoriteToggle = {},
            )
        }
        check("tree_detail_$variant")
    }

    private fun remarkableStory(variant: String) {
        show { RemarkableStoryScreen(micocoulier(story = STORY), onBack = {}, onOpenUrl = {}) }
        check("remarkable_story_$variant")
    }

    private fun micocoulier(story: RemarkableStoryUi?) =
        TreeDetailUi(
            id = 1,
            location = GeoPoint.PARIS_CENTER,
            name = "Micocoulier de Provence",
            scientificName = "Celtis australis",
            area = "Paris 19e",
            address = "Quai de la Seine",
            height = MeasureUi("16", "m"),
            circumference = MeasureUi("165", "cm"),
            stage = "Jeune (arbre)",
            remarkable = true,
            isFavorite = false,
            leaf = TreeLeaf.CELTIS,
            story = story,
        )

    @Suppress("MagicNumber") // sample counts
    private fun genusPicker(variant: String) {
        val drawn =
            TreeLeaf.entries.filter { it != TreeLeaf.GENERIC }.mapIndexed { i, leaf ->
                GenusChoiceUi(leaf.genus!!, leaf.name, leaf, 42_591 - i, "42 591", selected = i == 0)
            }
        show {
            GenusPickerContent(picker = GenusPickerUi(drawn, others = emptyList(), remarkableOnly = false), onPick = {})
        }
        check("genus_picker_$variant")
        val heights =
            compose
                .onAllNodesWithTag(LEAF_CARD_TAG)
                .fetchSemanticsNodes()
                .map { it.size.height }
                .toSet()
        assertEquals("every leaf card has the same height", 1, heights.size)
    }

    private fun show(content: @Composable () -> Unit) {
        compose.setContent {
            TreesTheme { Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) { content() } }
        }
    }

    private fun check(name: String) {
        val root = compose.onRoot()
        root.captureRoboImage("src/test/screenshots/$name.png")
        assertEveryTextFits(root)
    }

    /**
     * Fails on a text that is ellipsized, needs more lines than it may use, or breaks a word across two lines.
     * (`didOverflowWidth` is not reliable under Robolectric: a word too wide for its box shows up as a split word.)
     */
    private fun assertEveryTextFits(root: SemanticsNodeInteraction) {
        val problems = mutableListOf<String>()

        fun visit(node: androidx.compose.ui.semantics.SemanticsNode) {
            val layout = node.textLayout()
            if (layout != null) {
                val text = layout.layoutInput.text.text
                when {
                    layout.didOverflowHeight -> problems += "« $text » is cut (needs more lines)"
                    (0 until layout.lineCount).any { layout.isLineEllipsized(it) } -> problems += "« $text » is cut"
                    layout.splitsAWord(text) -> problems += "« $text » breaks a word across lines"
                }
            }
            node.children.forEach(::visit)
        }
        visit(root.fetchSemanticsNode())
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }

    private fun androidx.compose.ui.semantics.SemanticsNode.textLayout(): TextLayoutResult? {
        val action =
            config.getOrNull(SemanticsActions.GetTextLayoutResult)?.takeIf {
                config.getOrNull(SemanticsProperties.Text) != null
            }
        val results = mutableListOf<TextLayoutResult>()
        action?.action?.invoke(results)
        return results.firstOrNull()
    }

    private fun TextLayoutResult.splitsAWord(text: String): Boolean =
        (0 until lineCount - 1).any { line ->
            val end = getLineEnd(line)
            end in 1 until text.length && text[end - 1].isLetterOrDigit() && text[end].isLetterOrDigit()
        }

    private companion object {
        const val NARROW = "w320dp-h640dp-xhdpi"
        const val NARROW_TALL = "w320dp-h1400dp-xhdpi"
        const val REGULAR_TALL = "w360dp-h1100dp-xxhdpi"
        const val LARGE_TEXT = "w360dp-h640dp-xxhdpi"
        const val LARGE_TEXT_TALL = "w360dp-h1600dp-xxhdpi"
        const val LARGE_FONT_SCALE = 1.3f

        val STORY =
            RemarkableStoryUi(
                site = "Square René Viviani",
                qualification = "Historique",
                summary = "Cet arbre est classé remarquable pour son caractère historique.",
                story =
                    "Introduit et planté en 1601 par Jean Robin, auquel il doit son nom, cet arbre est le plus " +
                        "vieux de Paris.",
                plantingYear = "1602",
                label = "A.R.B.R.E.S en 2015",
                councilDecision = "2015 DEVE 152",
                signUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/124358.pdf",
                photoUrl = null,
                photoCredit = "© Jean-Pierre Viguié / Ville de Paris",
                teaser =
                    "Introduit et planté en 1601 par Jean Robin, auquel il doit son nom, cet arbre est le plus " +
                        "vieux de Paris.",
            )
    }
}
