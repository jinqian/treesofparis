package fr.arbresdeparis.core.designsystem.leaf

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeLeafTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun forGenus_findsTheDrawnGenera() {
        assertEquals(TreeLeaf.PLATANUS, TreeLeaf.forGenus("Platanus"))
        assertEquals(TreeLeaf.AESCULUS, TreeLeaf.forGenus("Aesculus"))
        assertEquals(TreeLeaf.FAGUS, TreeLeaf.forGenus("Fagus"))
    }

    @Test
    fun forGenus_ignoresCaseAndSpaces() {
        assertEquals(TreeLeaf.TILIA, TreeLeaf.forGenus("  tilia "))
    }

    @Test
    fun forGenus_unknownOrMissing_isGeneric() {
        assertEquals(TreeLeaf.GENERIC, TreeLeaf.forGenus("Ginkgo"))
        assertEquals(TreeLeaf.GENERIC, TreeLeaf.forGenus(null))
        assertEquals(TreeLeaf.GENERIC, TreeLeaf.forGenus(""))
    }

    @Test
    fun twentyOneGeneraAreDrawn_eachWithItsOwnGenus() {
        val drawn = TreeLeaf.entries.filter { it != TreeLeaf.GENERIC }
        assertEquals(21, drawn.size)
        assertEquals(21, drawn.mapNotNull { it.genus }.toSet().size)
    }

    @Test
    fun everyLeafHasAPathThatParses() {
        TreeLeaf.entries.forEach { leaf ->
            assertTrue("${leaf.name} has no path", PathParser().parsePathString(leaf.pathData).toNodes().isNotEmpty())
        }
    }

    @Test
    fun leafDrawing_isDisplayed() {
        compose.setContent {
            TreesTheme {
                LeafDrawing(
                    TreeLeaf.AESCULUS,
                    modifier = Modifier.size(48.dp).testTag("leaf"),
                )
            }
        }
        compose.onNodeWithTag("leaf").assertIsDisplayed()
    }
}
