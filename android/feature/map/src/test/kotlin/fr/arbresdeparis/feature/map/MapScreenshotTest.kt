package fr.arbresdeparis.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.LocalMapProvider
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MapConfig
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The Carte tab on a common phone, light and dark: the map's own controls, the tree sheet, the « Essences » sheet and
 * the nearby tree card. The map itself is a stub (MapLibre needs OpenGL): only what the app draws over it is checked.
 * Compared with the reference images in `src/test/screenshots/` (CI runs the tests with
 * `-Proborazzi.test.verify=true`). Record new references with `./gradlew recordRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class MapScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val base = MapUiState(initialCamera = MapViewModel.DEFAULT_CAMERA)

    @Test
    fun map_light() = map("map_light", base)

    @Test
    @Config(qualifiers = "+night")
    fun map_dark() = map("map_dark", base)

    @Test
    fun mapFiltered_light() =
        map(
            "map_filtered_light",
            base.copy(genusFilter = GenusFilterUi("Tilia", TreeLeaf.TILIA), remarkableOnly = true),
        )

    @Test
    fun mapZoomHint_light() = map("map_zoom_hint_light", base.copy(showZoomHint = true))

    @Test
    fun mapNearby_light() = map("map_nearby_light", base.copy(nearby = NEARBY))

    @Test
    @Config(qualifiers = "+night")
    fun mapNearby_dark() = map("map_nearby_dark", base.copy(nearby = NEARBY))

    @Test
    fun treeDetail_light() = sheet("tree_detail_light") { TreeDetailContent(TREE, onFavoriteToggle = {}) }

    @Test
    @Config(qualifiers = "+night")
    fun treeDetail_dark() = sheet("tree_detail_dark") { TreeDetailContent(TREE, onFavoriteToggle = {}) }

    @Test
    fun treeDetailStory_light() = sheet("tree_detail_story_light") { StorySheet() }

    @Test
    @Config(qualifiers = "+night")
    fun treeDetailStory_dark() = sheet("tree_detail_story_dark") { StorySheet() }

    @Test
    fun remarkableStory_light() = snap("remarkable_story_light") { RemarkableStoryScreen(STORY_TREE, {}, {}) }

    @Test
    @Config(qualifiers = "+night")
    fun remarkableStory_dark() = snap("remarkable_story_dark") { RemarkableStoryScreen(STORY_TREE, {}, {}) }

    @Test
    fun genusPicker_light() = sheet("genus_picker_light") { GenusPickerContent(PICKER, onPick = {}) }

    @Test
    fun filterHelp_light() = sheet("filter_help_light") { FilterHelpContent(onShowRemarkableOnly = {}, onClose = {}) }

    @Test
    @Config(qualifiers = "+night")
    fun filterHelp_dark() = sheet("filter_help_dark") { FilterHelpContent(onShowRemarkableOnly = {}, onClose = {}) }

    private fun map(
        name: String,
        state: MapUiState,
    ) = snap(name) {
        CompositionLocalProvider(LocalMapProvider provides PaperMap) {
            MapScreen(
                state = state,
                onCameraChange = { _, _ -> },
                onMarkerClick = {},
                onDetailDismiss = {},
                onFavoriteToggle = {},
                onLocateResult = {},
                onMessageDismiss = {},
                onOpenSettings = {},
                onRemarkableFilterChange = {},
                onRemarkableInfoOpen = {},
                onRemarkableInfoDismiss = {},
                onOpenWikipedia = {},
                onGenusFilterClear = {},
                onGenusPickerOpen = {},
                onGenusPick = {},
                onGenusPickerDismiss = {},
                onFindNearestTree = {},
                onNearbyNext = {},
                onNearbyOpen = {},
                onNearbyDismiss = {},
                // Our own buttons: the system location button is drawn by Android, outside the composition.
                showSystemLocationButtons = false,
            )
        }
    }

    @Composable
    private fun StorySheet() = TreeDetailContent(STORY_TREE, onFavoriteToggle = {})

    /** A sheet's content as the bottom sheet shows it: on the sheet colour, at the bottom of the screen. */
    private fun sheet(
        name: String,
        content: @Composable () -> Unit,
    ) = snap(name) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge,
            ) { Column { content() } }
        }
    }

    private fun snap(
        name: String,
        content: @Composable () -> Unit,
    ) {
        compose.setContent { TreesTheme { content() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    /** Stands in for MapLibre: the map's paper ground, nothing drawn on it. */
    private object PaperMap : MapProvider {
        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            clusters: List<MapCluster>,
            userLocation: GeoPoint?,
            config: MapConfig,
            palette: MapPalette,
            onMarkerClick: (Long) -> Unit,
            onClusterClick: (MapCluster) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) = Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }

    private companion object {
        val TREE =
            TreeDetailUi(
                id = 1,
                location = GeoPoint.PARIS_CENTER,
                name = "Robinier",
                scientificName = "Robinia pseudoacacia",
                area = "Paris 5e",
                address = "Square René Viviani",
                height = MeasureUi("15", "m"),
                circumference = MeasureUi("365", "cm"),
                stage = "Mature",
                remarkable = true,
                isFavorite = true,
                leaf = TreeLeaf.ROBINIA,
                wikipedia =
                    WikipediaUi(
                        "https://fr.wikipedia.org/wiki/Robinia_pseudoacacia",
                        "Robinier faux-acacia",
                        null,
                    ),
                flowering =
                    FloweringUi(
                        months = List(12) { it in 4..5 },
                        showyFlowers = true,
                        range = "mai – juin",
                        currentMonth = 10,
                        currentMonthName = "novembre",
                        monthsUntilFlowering = 6,
                    ),
            )

        /** The Robinier of Square René Viviani with its story; no photo URL, so the screenshot shows its drawing. */
        val STORY_TREE =
            TREE.copy(
                story =
                    RemarkableStoryUi(
                        site = "Square René Viviani",
                        qualification = "Historique",
                        summary = "Cet arbre est classé remarquable pour son caractère historique.",
                        story =
                            "Originaire des États-Unis, le Robinier faux-acacia appartient à la famille des " +
                                "Fabacées. Introduit et planté en 1601 par Jean Robin, auquel il doit son nom, " +
                                "cet arbre est le plus vieux de Paris.",
                        plantingYear = "1602",
                        label = "A.R.B.R.E.S en 2015",
                        councilDecision = "2015 DEVE 152",
                        signUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/124358.pdf",
                        photoUrl = null,
                        photoCredit = "© Jean-Pierre Viguié / Ville de Paris",
                    ),
            )

        val PICKER =
            GenusPickerUi(
                drawn =
                    listOf(
                        GenusChoiceUi("Platanus", "Platane", TreeLeaf.PLATANUS, 42_591, "42 591", selected = false),
                        GenusChoiceUi("Aesculus", "Marronnier", TreeLeaf.AESCULUS, 24_795, "24 795", selected = false),
                        GenusChoiceUi("Tilia", "Tilleul", TreeLeaf.TILIA, 23_072, "23 072", selected = true),
                        GenusChoiceUi("Acer", "Érable", TreeLeaf.ACER, 21_157, "21 157", selected = false),
                        GenusChoiceUi("Sophora", "Sophora", TreeLeaf.STYPHNOLOBIUM, 11_307, "11 307", selected = false),
                        GenusChoiceUi("Prunus", "Cerisier", TreeLeaf.PRUNUS, 9_246, "9 246", selected = false),
                    ),
                others =
                    listOf(
                        GenusChoiceUi("Ginkgo", "Arbre aux quarante écus", TreeLeaf.GENERIC, 1_154, "1 154", false),
                    ),
                remarkableOnly = false,
            )

        val NEARBY =
            NearbyTreesUi(
                from = GeoPoint.PARIS_CENTER,
                trees =
                    listOf(
                        NearbyTreeUi(
                            1,
                            "Platane",
                            "Platanus × hispanica",
                            "12 m",
                            GeoPoint.PARIS_CENTER,
                            TreeLeaf.PLATANUS,
                        ),
                        NearbyTreeUi(2, "Tilleul", "Tilia tomentosa", "35 m", GeoPoint.PARIS_CENTER, TreeLeaf.TILIA),
                    ),
            )
    }
}

/** A common phone: 411 × 891 dp. */
private const val PHONE = "w411dp-h891dp-xxhdpi"
