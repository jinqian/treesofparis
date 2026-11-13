package fr.arbresdeparis.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Favoris on a common phone, light and dark, with trees and empty, compared with the reference images in
 * `src/test/screenshots/` (CI runs the tests with `-Proborazzi.test.verify=true`). Record new references with
 * `./gradlew recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class FavoritesScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val trees =
        FavoritesUiState.Content(
            listOf(
                FavoriteUiModel(7, "Platane", "Paris 19e · Quai de la Seine", remarkable = true),
                FavoriteUiModel(8, "Tilleul argenté", "Paris 5e · Square René Viviani"),
                FavoriteUiModel(9, "Ginkgo", "Boulogne-Billancourt · Parc Rothschild"),
                FavoriteUiModel(10, "Marronnier", null, gone = true),
            ),
        )

    @Test
    fun favorites_light() = favorites("light", trees)

    @Test
    @Config(qualifiers = "+night")
    fun favorites_dark() = favorites("dark", trees)

    @Test
    fun favoritesEmpty_light() = favorites("empty_light", FavoritesUiState.Empty)

    @Test
    @Config(qualifiers = "+night")
    fun favoritesEmpty_dark() = favorites("empty_dark", FavoritesUiState.Empty)

    private fun favorites(
        name: String,
        state: FavoritesUiState,
    ) = snap("favorites_$name") { FavoritesScreen(state, onTreeClick = {}, onRemove = {}) }

    private fun snap(
        name: String,
        content: @Composable () -> Unit,
    ) {
        compose.setContent { TreesTheme { content() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}

/** A common phone: 411 × 891 dp. */
private const val PHONE = "w411dp-h891dp-xxhdpi"
