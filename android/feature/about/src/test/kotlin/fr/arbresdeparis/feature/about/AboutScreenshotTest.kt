package fr.arbresdeparis.feature.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * À propos on a common phone, light and dark, compared with the reference images in `src/test/screenshots/` (CI runs
 * the tests with `-Proborazzi.test.verify=true`). Record new references with `./gradlew recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE_TALL)
class AboutScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val state =
        AboutUiState(
            versionName = "0.2.0",
            contactEmail = "arbresdeparis@gmail.com",
            websiteUrl = "https://arbresdeparis.fr",
            extraction = ExtractionUi(date = "6 novembre 2026", treeCount = "252 975"),
            floweringSheetCount = 27,
            themeMode = ThemeMode.SYSTEM,
        )

    @Test
    fun about_light() = about("light")

    @Test
    @Config(qualifiers = "+night")
    fun about_dark() = about("dark")

    private fun about(theme: String) =
        snap("about_$theme") {
            AboutScreen(
                state = state,
                onOpenUrl = {},
                onWriteToUs = {},
                onOpenLibraries = {},
                onOpenFloweringSources = {},
                onCrashReportingChange = {},
            )
        }

    private fun snap(
        name: String,
        content: @Composable () -> Unit,
    ) {
        compose.setContent { TreesTheme { content() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}

/** A common phone (411 dp wide), tall enough for the whole À propos list. */
private const val PHONE_TALL = "w411dp-h2000dp-xxhdpi"
