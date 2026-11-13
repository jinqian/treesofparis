package fr.arbresdeparis.feature.herbier

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
 * The Herbier and its four sections on a common phone, light and dark, compared with the reference images in
 * `src/test/screenshots/` (CI runs the tests with `-Proborazzi.test.verify=true`). Record new references with
 * `./gradlew recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class HerbierScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun herbier_light() = snap("herbier_light") { HerbierScreen(content(), onOpenSection = {}) }

    @Test
    @Config(qualifiers = "+night")
    fun herbier_dark() = snap("herbier_dark") { HerbierScreen(content(), onOpenSection = {}) }

    @Test
    fun genera_light() = section("genera_light", HerbierSection.GENERA)

    @Test
    @Config(qualifiers = "+night")
    fun genera_dark() = section("genera_dark", HerbierSection.GENERA)

    @Test
    fun flowering_light() = section("flowering_light", HerbierSection.FLOWERING)

    @Test
    fun remarkable_light() = section("remarkable_light", HerbierSection.REMARKABLE)

    @Test
    @Config(qualifiers = "+night")
    fun remarkable_dark() = section("remarkable_dark", HerbierSection.REMARKABLE)

    @Test
    fun ageAndAreas_light() = section("age_and_areas_light", HerbierSection.AGE_AND_AREAS)

    private fun section(
        name: String,
        section: HerbierSection,
    ) = snap("herbier_$name") { HerbierSectionScreen(section, content(), onBack = {}, onGenusClick = {}) }

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
