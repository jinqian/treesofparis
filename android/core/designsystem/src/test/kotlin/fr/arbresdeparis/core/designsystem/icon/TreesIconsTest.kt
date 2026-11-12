package fr.arbresdeparis.core.designsystem.icon

import androidx.compose.ui.graphics.vector.VectorPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TreesIconsTest {
    @Test
    fun appLogo_isTheLauncherTreeOnItsGrid() {
        val logo = TreesIcons.AppLogo

        assertEquals(108f, logo.viewportWidth)
        assertEquals(108f, logo.viewportHeight)
        val paths = logo.root.map { it as VectorPath }
        assertEquals("crown, trunk and branches, remarkable dot", 3, paths.size)
        // The crown and the branches are strokes; the dot is filled.
        assertNotNull(paths[0].stroke)
        assertNotNull(paths[1].stroke)
        assertNull(paths[2].stroke)
        assertNotNull(paths[2].fill)
    }
}
