package fr.arbresdeparis.feature.map

import androidx.lifecycle.SavedStateHandle
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MarkerStyle
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.usecase.GetFloweringSeason
import fr.arbresdeparis.domain.usecase.GetGenera
import fr.arbresdeparis.domain.usecase.GetNearestTrees
import fr.arbresdeparis.domain.usecase.GetTree
import fr.arbresdeparis.domain.usecase.GetTreeClusters
import fr.arbresdeparis.domain.usecase.GetTreesInBounds
import fr.arbresdeparis.domain.usecase.LocateUser
import fr.arbresdeparis.domain.usecase.ObserveIsFavorite
import fr.arbresdeparis.domain.usecase.ToggleFavorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val center = GeoPoint.PARIS_CENTER
    private val near = GeoBounds(south = 48.85, west = 2.34, north = 48.86, east = 2.36)
    private val remarkable = tree(9002, 48.8551, 2.3501).copy(remarkable = true)
    private val lime = tree(9003, 48.8552, 2.3502).copy(genus = "Tilia")
    private val trees =
        FakeTreeRepository(
            listOf(remarkable, lime) + (1L..3000L).map { tree(it, 48.855, 2.35) } + tree(9001, 48.9059, 2.4101),
        )
    private val favorites = FakeFavoritesRepository()
    private val location = FakeLocationRepository(null)

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()) =
        MapViewModel(
            getTreesInBounds = GetTreesInBounds(trees),
            getTreeClusters = GetTreeClusters(trees),
            getTree = GetTree(trees),
            observeIsFavorite = ObserveIsFavorite(favorites),
            toggleFavorite = ToggleFavorite(favorites, trees),
            locateUser = LocateUser(location),
            getNearestTrees = GetNearestTrees(trees),
            getFloweringSeason = GetFloweringSeason(FakeFloweringRepository()),
            getGenera = GetGenera(FakeStatisticsRepository()),
            clock = NOVEMBER,
            savedState = saved,
        )

    @Test
    fun selectedTree_knowsWhereItStands() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(9001)
            advanceUntilIdle()
            assertEquals(
                GeoPoint(48.9059, 2.4101),
                vm.uiState.value.selectedTree
                    ?.location,
            )
        }

    @Test
    fun mapConfig_allowsStreetLevelZoom() {
        assertEquals(21.0, MapViewModel.MAP_CONFIG.maxZoom, 0.0)
    }

    @Test
    fun initialState_isParisCentre() {
        val state = viewModel().uiState.value
        assertEquals(center, state.initialCamera.center)
        assertNull(state.cameraRequest)
    }

    @Test
    fun onCameraChange_belowMinZoom_showsTreeGroupsInsteadOfTrees() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = MapViewModel.MIN_TREE_ZOOM - 1), near)
            advanceUntilIdle()
            val state = vm.uiState.value
            assertTrue(state.showZoomHint)
            assertTrue(state.markers.isEmpty())
            assertTrue(trees.boundsCalls.isEmpty())
            // The groups of a zoom level are built once, for the whole map.
            assertEquals(listOf(GeoBounds.COVERAGE), trees.clusterCalls)
            assertTrue(state.clusters.isNotEmpty())
            assertTrue(state.clusters.all { it.weight in 0.0..1.0 })
        }

    @Test
    fun onCameraChange_atStreetLevel_dropsTheGroups() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 13.0), near)
            advanceUntilIdle()
            vm.onCameraChange(MapCamera(center, zoom = 17.0), near)
            advanceUntilIdle()
            assertTrue(
                vm.uiState.value.clusters
                    .isEmpty(),
            )
            assertTrue(
                vm.uiState.value.markers
                    .isNotEmpty(),
            )
        }

    @Test
    fun onClusterClick_zoomsTwoLevelsCloser_atMostJustPastStreetLevel() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            val group = MapCluster(id = 0, position = GeoPoint(48.86, 2.34), count = 12, label = "12")
            vm.onCameraChange(MapCamera(center, zoom = 12.0), near)
            vm.onClusterClick(group)
            assertEquals(
                MapCamera(group.position, zoom = 14.0),
                vm.uiState.value.cameraRequest
                    ?.camera,
            )

            vm.onCameraChange(MapCamera(center, zoom = 15.0), near)
            vm.onClusterClick(group)
            assertEquals(
                16.0,
                vm.uiState.value.cameraRequest
                    ?.camera
                    ?.zoom,
            )
        }

    @Test
    fun onCameraChange_debouncesAndCapsMarkers() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            repeat(5) { vm.onCameraChange(MapCamera(center, zoom = 17.0), near) }
            advanceTimeBy(MapViewModel.CAMERA_DEBOUNCE_MS - 1)
            runCurrent()
            assertTrue("no query before the debounce", trees.boundsCalls.isEmpty())
            advanceUntilIdle()
            assertEquals(listOf(near to MapViewModel.MAX_MARKERS), trees.boundsCalls)
            assertEquals(MapViewModel.MAX_MARKERS, vm.uiState.value.markers.size)
            assertFalse(vm.uiState.value.showZoomHint)
        }

    @Test
    fun permissionGranted_insideCoverage_issuesCameraRequestToUser() =
        runTest(main.dispatcher) {
            val pantin = GeoPoint(48.9059, 2.4101)
            location.location = pantin
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true)
            advanceUntilIdle()
            assertEquals(pantin, vm.uiState.value.userLocation)
            assertEquals(
                pantin,
                vm.uiState.value.cameraRequest
                    ?.camera
                    ?.center,
            )
        }

    @Test
    fun permissionGranted_outsideCoverage_keepsParisCentre() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(45.764, 4.8357) // Lyon
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true)
            advanceUntilIdle()
            assertNull(vm.uiState.value.cameraRequest)
            assertNull(vm.uiState.value.userLocation)
            assertEquals(center, vm.uiState.value.initialCamera.center)
        }

    @Test
    fun permissionDenied_keepsParisCentre() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(48.86, 2.34)
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = false)
            advanceUntilIdle()
            assertNull(vm.uiState.value.cameraRequest)
            assertNull(vm.uiState.value.userLocation)
        }

    @Test
    fun automaticRequest_outsideCoverage_showsNoMessage() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(37.422, -122.084) // emulator default, Mountain View
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true)
            advanceUntilIdle()
            assertNull(vm.uiState.value.message)
        }

    @Test
    fun locateMe_outsideCoverage_showsOutOfAreaMessage() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(37.422, -122.084)
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true, userInitiated = true)
            advanceUntilIdle()
            assertEquals(MapMessage.OUT_OF_AREA, vm.uiState.value.message)
            assertNull(vm.uiState.value.cameraRequest)
        }

    @Test
    fun locateMe_unavailable_showsUnavailableMessage() =
        runTest(main.dispatcher) {
            location.location = null
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true, userInitiated = true)
            advanceUntilIdle()
            assertEquals(MapMessage.LOCATION_UNAVAILABLE, vm.uiState.value.message)
        }

    @Test
    fun locateMe_permissionDenied_showsDeniedMessage() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = false, userInitiated = true)
            advanceUntilIdle()
            assertEquals(MapMessage.PERMISSION_DENIED, vm.uiState.value.message)
        }

    @Test
    fun onMessageShown_clearsMessage() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = false, userInitiated = true)
            advanceUntilIdle()
            vm.onMessageShown()
            assertNull(vm.uiState.value.message)
        }

    @Test
    fun locateMe_showsLocatingWhileWaitingForAFix() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(48.86, 2.34)
            location.delayMs = 1_000
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true, userInitiated = true)
            runCurrent()
            assertTrue(vm.uiState.value.isLocating)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isLocating)
        }

    @Test
    fun locateMe_twice_recentresEachTime() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(48.86, 2.34)
            val vm = viewModel()
            vm.onLocationPermissionResult(granted = true, userInitiated = true)
            advanceUntilIdle()
            val first = vm.uiState.value.cameraRequest
            vm.onLocationPermissionResult(granted = true, userInitiated = true)
            advanceUntilIdle()
            val second = vm.uiState.value.cameraRequest
            assertNotNull(first)
            assertTrue("a new request id moves the map again", first!!.id != second!!.id)
        }

    @Test
    fun markers_remarkableTreesHaveRemarkableStyle() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(remarkable.location, zoom = 17.0), GeoBounds.around(remarkable.location, 50.0))
            advanceUntilIdle()
            val styles =
                vm.uiState.value.markers
                    .associate { it.id to it.style }
            assertEquals(MarkerStyle.REMARKABLE, styles[9002])
            assertEquals(MarkerStyle.TREE, styles[1])
        }

    @Test
    fun remarkableFilter_showsOnlyRemarkableTreesAtAnyZoom() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 12.0), GeoBounds.COVERAGE)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.showZoomHint)

            vm.onRemarkableFilterChange(true)
            advanceUntilIdle()

            assertTrue(vm.uiState.value.remarkableOnly)
            assertFalse(vm.uiState.value.showZoomHint)
            assertEquals(
                listOf(9002L),
                vm.uiState.value.markers
                    .map { it.id },
            )
            assertEquals(true, trees.remarkableOnlyCalls.last())
        }

    @Test
    fun remarkableFilter_toggleReloadsWithoutWaitingForTheCameraDebounce() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 17.0), near)
            advanceUntilIdle()
            vm.onRemarkableFilterChange(true)
            runCurrent()
            assertEquals(
                listOf(9002L),
                vm.uiState.value.markers
                    .map { it.id },
            )
        }

    @Test
    fun remarkableFilter_off_restoresAllTreesAndZoomHint() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 12.0), GeoBounds.COVERAGE)
            vm.onRemarkableFilterChange(true)
            advanceUntilIdle()
            vm.onRemarkableFilterChange(false)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.remarkableOnly)
            assertTrue(vm.uiState.value.showZoomHint)
            assertTrue(
                vm.uiState.value.markers
                    .isEmpty(),
            )
        }

    @Test
    fun remarkableInfo_opensThenClosesWhenAFilterIsChosen() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onRemarkableInfoOpen()
            assertTrue(vm.uiState.value.showRemarkableInfo)
            vm.onRemarkableFilterChange(true)
            assertFalse(vm.uiState.value.showRemarkableInfo)
            assertTrue(vm.uiState.value.remarkableOnly)
        }

    @Test
    fun remarkableInfo_dismiss_keepsTheFilter() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onRemarkableInfoOpen()
            vm.onRemarkableInfoDismiss()
            assertFalse(vm.uiState.value.showRemarkableInfo)
            assertFalse(vm.uiState.value.remarkableOnly)
        }

    @Test
    fun remarkableFilter_survivesRecreation() =
        runTest(main.dispatcher) {
            val saved = SavedStateHandle()
            viewModel(saved).onRemarkableFilterChange(true)
            advanceUntilIdle()
            assertTrue(viewModel(saved).uiState.value.remarkableOnly)
        }

    @Test
    fun onMarkerClick_loadsDetailAndSelectsMarker() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 17.0), GeoBounds(48.905, 2.40, 48.91, 2.42))
            advanceUntilIdle()
            vm.onMarkerClick(9001)
            advanceUntilIdle()
            assertEquals(
                9001L,
                vm.uiState.value.selectedTree
                    ?.id,
            )
            assertEquals(
                listOf(9001L),
                vm.uiState.value.markers
                    .filter { it.selected }
                    .map { it.id },
            )
        }

    @Test
    fun selectedTree_showsItsGenusFlowering_fromTheParisMonth() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(lime.id)
            advanceUntilIdle()
            val flowering =
                vm.uiState.value.selectedTree
                    ?.flowering
            assertEquals("mai – juin", flowering?.range)
            assertEquals(11, flowering?.currentMonth)
            assertEquals(6, flowering?.monthsUntilFlowering)
            vm.onMarkerClick(remarkable.id)
            advanceUntilIdle()
            assertNull(
                vm.uiState.value.selectedTree
                    ?.flowering,
            )
        }

    @Test
    fun onFavoriteToggle_updatesIsFavorite() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(1)
            advanceUntilIdle()
            assertEquals(
                false,
                vm.uiState.value.selectedTree
                    ?.isFavorite,
            )
            vm.onFavoriteToggle()
            advanceUntilIdle()
            assertEquals(
                true,
                vm.uiState.value.selectedTree
                    ?.isFavorite,
            )
            assertEquals(listOf(1L), favorites.ids.value)
        }

    @Test
    fun onDetailDismissed_clearsSelection() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(1)
            advanceUntilIdle()
            vm.onDetailDismissed()
            advanceUntilIdle()
            assertNull(vm.uiState.value.selectedTree)
        }

    @Test
    fun onOpenTree_centresOnTreeAndOpensDetail() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpenTree(9001)
            advanceUntilIdle()
            assertEquals(
                GeoPoint(48.9059, 2.4101),
                vm.uiState.value.cameraRequest
                    ?.camera
                    ?.center,
            )
            assertEquals(
                9001L,
                vm.uiState.value.selectedTree
                    ?.id,
            )
        }

    @Test
    fun onOpenTree_sameIdTwice_movesOnce() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpenTree(9001)
            advanceUntilIdle()
            val first = vm.uiState.value.cameraRequest
            vm.onOpenTree(9001)
            advanceUntilIdle()
            assertEquals(first, vm.uiState.value.cameraRequest)
        }

    @Test
    fun cameraAndSelection_surviveRecreationViaSavedStateHandle() =
        runTest(main.dispatcher) {
            val saved = SavedStateHandle()
            val first = viewModel(saved)
            first.onCameraChange(MapCamera(GeoPoint(48.87, 2.33), zoom = 17.5), near)
            first.onMarkerClick(1)
            advanceUntilIdle()

            val second = viewModel(saved)
            advanceUntilIdle()
            assertEquals(MapCamera(GeoPoint(48.87, 2.33), zoom = 17.5), second.uiState.value.initialCamera)
            assertNotNull(second.uiState.value.selectedTree)
        }

    // « Arbre le plus proche »

    /** 11 m south of the Pantin tree 9001, far from the others. */
    private val nextToPantinTree = GeoPoint(48.9058, 2.4101)
    private val aroundPantin = GeoBounds(48.905, 2.40, 48.91, 2.42)

    private fun MapViewModel.findNearestTree() {
        onNearestTreeRequest(granted = true)
    }

    @Test
    fun nearestTree_framesTheClosestTreeAndHighlightsItWithoutOpeningTheSheet() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            val vm = viewModel()
            vm.onCameraChange(MapCamera(nextToPantinTree, zoom = 19.0), aroundPantin)
            vm.findNearestTree()
            advanceUntilIdle()

            val state = vm.uiState.value
            val nearby = state.nearby!!
            assertEquals(9001L, nearby.current.id)
            assertEquals(1, nearby.rank)
            assertEquals("11\u00A0m", nearby.current.distance)
            assertEquals(nextToPantinTree, state.userLocation)
            assertEquals(NEARBY_MAX_ZOOM, state.cameraRequest!!.camera.zoom, 0.0)
            assertEquals(listOf(9001L), state.markers.filter { it.selected }.map { it.id })
            assertNull(state.selectedTree)
            assertFalse(state.isFindingNearest)
        }

    @Test
    fun nearestTree_offersTheNextClosestThenWrapsAround() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            val vm = viewModel()
            vm.findNearestTree()
            advanceUntilIdle()
            val first = vm.uiState.value.cameraRequest!!
            val count =
                vm.uiState.value.nearby!!
                    .trees.size
            assertEquals(MapViewModel.NEARBY_COUNT, count)

            vm.onNearbyNext()
            assertEquals(
                2,
                vm.uiState.value.nearby!!
                    .rank,
            )
            assertTrue(
                vm.uiState.value.nearby!!
                    .current.id != 9001L,
            )
            assertTrue(
                "moves to the next tree",
                vm.uiState.value.cameraRequest!!
                    .id != first.id,
            )

            repeat(count - 1) { vm.onNearbyNext() }
            assertEquals(
                9001L,
                vm.uiState.value.nearby!!
                    .current.id,
            )
        }

    @Test
    fun nearestTree_openShowsItsSheet_andTheCardStaysForAfterwards() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            val vm = viewModel()
            vm.onCameraChange(MapCamera(nextToPantinTree, zoom = 19.0), aroundPantin)
            vm.findNearestTree()
            advanceUntilIdle()

            vm.onNearbyOpen()
            advanceUntilIdle()
            assertEquals(
                9001L,
                vm.uiState.value.selectedTree
                    ?.id,
            )

            vm.onDetailDismissed()
            assertNotNull(vm.uiState.value.nearby)
            assertEquals(
                listOf(9001L),
                vm.uiState.value.markers
                    .filter { it.selected }
                    .map { it.id },
            )
        }

    @Test
    fun nearestTree_dismiss_clearsTheCardAndTheHighlight() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            val vm = viewModel()
            vm.onCameraChange(MapCamera(nextToPantinTree, zoom = 19.0), aroundPantin)
            vm.findNearestTree()
            advanceUntilIdle()

            vm.onNearbyDismiss()
            assertNull(vm.uiState.value.nearby)
            assertTrue(
                vm.uiState.value.markers
                    .none { it.selected },
            )
        }

    @Test
    fun nearestTree_followsTheRemarkableFilter() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(48.855, 2.35) // on the cluster of ordinary trees
            val vm = viewModel()
            vm.onRemarkableFilterChange(true)
            vm.findNearestTree()
            advanceUntilIdle()
            assertEquals(
                listOf(9002L),
                vm.uiState.value.nearby!!
                    .trees
                    .map { it.id },
            )
        }

    @Test
    fun nearestTree_aFilterChangeClosesTheCard() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            val vm = viewModel()
            vm.findNearestTree()
            advanceUntilIdle()
            vm.onRemarkableFilterChange(true)
            assertNull(vm.uiState.value.nearby)
        }

    @Test
    fun nearestTree_noTreeAround_saysSo() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(48.7423, 2.3600) // Thiais, more than 10 km from every test tree
            val vm = viewModel()
            vm.findNearestTree()
            advanceUntilIdle()
            assertNull(vm.uiState.value.nearby)
            assertEquals(MapMessage.NO_TREE_NEARBY, vm.uiState.value.message)
        }

    @Test
    fun nearestTree_outsideCoverageOrNotLocated_explainsWhy() =
        runTest(main.dispatcher) {
            location.location = GeoPoint(45.764, 4.8357) // Lyon
            val vm = viewModel()
            vm.findNearestTree()
            advanceUntilIdle()
            assertEquals(MapMessage.OUT_OF_AREA, vm.uiState.value.message)

            vm.onMessageShown()
            vm.onNearestTreeRequest(granted = false)
            // No location yet: the precise location only comes from the location button.
            assertEquals(MapMessage.LOCATE_FIRST, vm.uiState.value.message)
            assertNull(vm.uiState.value.nearby)
        }

    @Test
    fun nearestTree_showsProgressWhileWaitingForAFix() =
        runTest(main.dispatcher) {
            location.location = nextToPantinTree
            location.delayMs = 1_000
            val vm = viewModel()
            vm.findNearestTree()
            runCurrent()
            assertTrue(vm.uiState.value.isFindingNearest)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isFindingNearest)
        }
}
