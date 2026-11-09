package fr.arbresdeparis.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.LocalMapProvider
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapConfig
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private object StubMap : MapProvider {
        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            userLocation: GeoPoint?,
            config: MapConfig,
            palette: MapPalette,
            onMarkerClick: (Long) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) = Box(modifier)
    }

    private var messagesShown = 0
    private var settingsOpened = 0
    private var locateClicks = 0
    private val filterChanges = mutableListOf<Boolean>()
    private var infoOpens = 0
    private var genusClears = 0
    private var genusPickerOpens = 0
    private val genusPicks = mutableListOf<String>()
    private var nearestClicks = 0
    private var nearbyNexts = 0
    private var nearbyOpens = 0
    private var nearbyDismisses = 0

    private fun show(state: MapUiState) =
        compose.setContent {
            CompositionLocalProvider(LocalMapProvider provides StubMap) {
                TreesTheme {
                    MapScreen(
                        state = state,
                        onCameraChange = { _, _ -> },
                        onMarkerClick = {},
                        onDetailDismiss = {},
                        onFavoriteToggle = {},
                        onLocateMe = { locateClicks++ },
                        onMessageDismiss = { messagesShown++ },
                        onOpenSettings = { settingsOpened++ },
                        onRemarkableFilterChange = { filterChanges += it },
                        onRemarkableInfoOpen = { infoOpens++ },
                        onRemarkableInfoDismiss = {},
                        onOpenWikipedia = {},
                        onGenusFilterClear = { genusClears++ },
                        onGenusPickerOpen = { genusPickerOpens++ },
                        onGenusPick = { genusPicks += it.genus },
                        onGenusPickerDismiss = {},
                        onFindNearestTree = { nearestClicks++ },
                        onNearbyNext = { nearbyNexts++ },
                        onNearbyOpen = { nearbyOpens++ },
                        onNearbyDismiss = { nearbyDismisses++ },
                    )
                }
            }
        }

    private val base = MapUiState(initialCamera = MapViewModel.DEFAULT_CAMERA)

    @Test
    fun filterSegments_showAllSelectedAndNoAppNamePill() {
        show(base)
        compose.onNodeWithText("Tous les arbres").assertIsSelected()
        compose.onNodeWithText("Remarquables").assertIsNotSelected()
        compose.onNodeWithText("Arbres de Paris").assertDoesNotExist()
        compose.onNodeWithText("Seuls les arbres remarquables sont affichés").assertDoesNotExist()
    }

    @Test
    fun filterSegments_dotsHaveTheSameSize() {
        show(base)
        val sizes =
            compose
                .onAllNodesWithTag(
                    LEGEND_DOT_TAG,
                    useUnmergedTree = true,
                ).fetchSemanticsNodes()
                .map { it.size }
        assertEquals(2, sizes.size)
        assertEquals(1, sizes.distinct().size)
    }

    @Test
    fun filterSegments_chooseRemarkableThenAll() {
        show(base)
        compose.onNodeWithText("Remarquables").performClick()
        compose.onNodeWithText("Tous les arbres").performClick()
        assertEquals(listOf(true, false), filterChanges)
    }

    @Test
    fun filterSegments_remarkableSelectedShowsCaption() {
        show(base.copy(remarkableOnly = true))
        compose.onNodeWithText("Remarquables").assertIsSelected()
        compose.onNodeWithText("Seuls les arbres remarquables sont affichés").assertIsDisplayed()
    }

    @Test
    fun genusFilter_showsAPillThatReopensTheSheetOrClears() {
        show(base.copy(genusFilter = GenusFilterUi("Tilia", TreeLeaf.TILIA)))
        compose.onNodeWithText("Toutes les essences").assertDoesNotExist()
        // The genus filter combines with the segments: « Tous les arbres » stays chosen.
        compose.onNodeWithText("Tous les arbres").assertIsSelected()
        compose.onNodeWithText("Seulement : Tilleul").performClick()
        compose.onNodeWithContentDescription("Afficher tous les arbres").performClick()
        assertEquals(1, genusPickerOpens)
        assertEquals(1, genusClears)
    }

    @Test
    fun genusFilter_undrawnGenus_isNamedInFrench() {
        show(base.copy(genusFilter = GenusFilterUi("Ginkgo", TreeLeaf.GENERIC, "Arbre aux quarante écus")))
        compose.onNodeWithText("Seulement : Arbre aux quarante écus").assertIsDisplayed()
    }

    @Test
    fun genusPills_haveTheSameHeightWithOrWithoutAGenus() {
        var genusFilter by mutableStateOf<GenusFilterUi?>(null)
        compose.setContent {
            CompositionLocalProvider(LocalMapProvider provides StubMap) {
                TreesTheme {
                    Box {
                        if (genusFilter == null) {
                            GenusFilterButton(onClick = {})
                        } else {
                            GenusFilterPill(genusFilter!!, onOpen = {}, onClear = {})
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag(GENUS_PILL_TAG).assertHeightIsEqualTo(GENUS_PILL_HEIGHT)
        genusFilter = GenusFilterUi("Tilia", TreeLeaf.TILIA)
        compose.onNodeWithTag(GENUS_PILL_TAG).assertHeightIsEqualTo(GENUS_PILL_HEIGHT)
    }

    @Test
    fun noGenusFilter_offersAllGenera() {
        show(base)
        compose.onNodeWithText("Toutes les essences").performClick()
        assertEquals(1, genusPickerOpens)
    }

    private val picker =
        GenusPickerUi(
            drawn =
                listOf(
                    GenusChoiceUi("Platanus", "Platane", TreeLeaf.PLATANUS, 42_591, "42 591", selected = false),
                    GenusChoiceUi("Acer", "Erable", TreeLeaf.ACER, 21_157, "21 157", selected = true),
                ),
            others =
                listOf(
                    GenusChoiceUi("Ginkgo", "Arbre aux quarante écus", TreeLeaf.GENERIC, 1_154, "1 154", false),
                ),
            remarkableOnly = false,
        )

    @Test
    fun genusPicker_listsDrawnAndOtherGenera_withCounts() {
        show(base.copy(genusPicker = picker))
        compose.onNodeWithText("Essences").assertIsDisplayed()
        compose.onNodeWithText("Platane").assertIsDisplayed()
        compose.onNodeWithText("42 591 arbres").assertIsDisplayed()
        compose.onNodeWithText("Érable", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("Platane").performClick()
        assertEquals(listOf("Platanus"), genusPicks)
    }

    @Test
    fun genusPicker_searchNarrowsTheList_andSaysWhenNothingMatches() {
        show(base.copy(genusPicker = picker))
        compose.onNodeWithText("Rechercher une essence").performTextInput("gink")
        compose.onNodeWithText("Platane").assertDoesNotExist()
        compose.onNodeWithText("Arbre aux quarante écus").assertIsDisplayed()
        compose.onNodeWithText("Rechercher une essence").performTextReplacement("chêne")
        compose.onNodeWithText("Aucune essence ne correspond à « chêne ».").assertIsDisplayed()
    }

    @Test
    fun infoButton_opensExplanation() {
        show(base)
        compose.onNodeWithContentDescription("Qu’est-ce qu’un arbre remarquable ?").performClick()
        assertEquals(1, infoOpens)
    }

    @Test
    fun locateButton_invokesLocateMe() {
        show(base)
        compose.onNodeWithContentDescription("Me localiser").performClick()
        assertEquals(1, locateClicks)
    }

    @Test
    fun locateButton_whileLocating_showsProgress() {
        show(base.copy(isLocating = true))
        compose.onNodeWithContentDescription("Localisation…").assertIsDisplayed()
    }

    @Test
    fun outOfAreaMessage_isShownThenConsumed() {
        show(base.copy(message = MapMessage.OUT_OF_AREA))
        compose.onNodeWithText("Vous êtes en dehors de la zone couverte par la carte.").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 10_000) { messagesShown == 1 }
    }

    @Test
    fun permissionDenied_settingsActionOpensSettings() {
        show(base.copy(message = MapMessage.PERMISSION_DENIED))
        compose.onNodeWithText("Réglages").performClick()
        compose.waitUntil(timeoutMillis = 5_000) { settingsOpened == 1 }
    }

    private val nearby =
        NearbyTreesUi(
            from = GeoPoint.PARIS_CENTER,
            trees =
                listOf(
                    NearbyTreeUi(
                        1,
                        "Platane",
                        "Platanus x hispanica",
                        "5\u00A0m",
                        GeoPoint.PARIS_CENTER,
                        TreeLeaf.GENERIC,
                    ),
                    NearbyTreeUi(
                        2,
                        "Tilleul argenté",
                        "Tilia tomentosa",
                        "7\u00A0m",
                        GeoPoint.PARIS_CENTER,
                        TreeLeaf.TILIA,
                    ),
                ),
        )

    @Test
    fun nearestTreeButton_findsTheNearestTree() {
        show(base)
        compose.onNodeWithText("Arbre le plus proche").performClick()
        assertEquals(1, nearestClicks)
    }

    @Test
    fun nearestTreeButton_whileSearching_showsProgress() {
        show(base.copy(isFindingNearest = true))
        compose.onNodeWithContentDescription("Recherche de l’arbre le plus proche…").assertIsDisplayed()
    }

    @Test
    fun nearbyCard_showsTheTreeAndItsDistance_inPlaceOfTheButton() {
        show(base.copy(nearby = nearby))
        compose.onNodeWithText("Platane").assertIsDisplayed()
        compose.onNodeWithText("Platanus x hispanica").assertIsDisplayed()
        compose.onNodeWithText("Le plus proche · à 5\u00A0m").assertIsDisplayed()
        compose.onNodeWithText("Arbre le plus proche").assertDoesNotExist()
    }

    @Test
    fun nearbyCard_secondTree_saysItsRank() {
        show(base.copy(nearby = nearby.copy(index = 1)))
        compose.onNodeWithText("Tilleul argenté").assertIsDisplayed()
        compose.onNodeWithText("2ᵉ plus proche · à 7\u00A0m").assertIsDisplayed()
    }

    @Test
    fun nearbyCard_actions() {
        show(base.copy(nearby = nearby))
        compose.onNodeWithText("Voir la fiche").performClick()
        compose.onNodeWithText("Suivant").performClick()
        compose.onNodeWithContentDescription("Fermer").assertHasClickAction().performClick()
        assertEquals(listOf(1, 1, 1), listOf(nearbyOpens, nearbyNexts, nearbyDismisses))
    }
}
