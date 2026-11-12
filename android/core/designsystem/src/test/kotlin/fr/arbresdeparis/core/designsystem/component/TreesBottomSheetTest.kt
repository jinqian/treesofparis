package fr.arbresdeparis.core.designsystem.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class TreesBottomSheetTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun opensAtTheHeightOfItsContent_notHalfway() {
        compose.setContent {
            TreesTheme {
                TreesBottomSheet(onDismiss = {}) {
                    Text("top")
                    // Taller than half the screen, shorter than the screen: the tree sheet with its flowering bar.
                    Spacer(Modifier.height(560.dp))
                    Text("bottom")
                }
            }
        }
        compose.onNodeWithText("bottom").assertIsDisplayed()
    }
}
