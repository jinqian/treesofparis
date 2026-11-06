package com.treesofparis.feature.map

import androidx.lifecycle.SavedStateHandle
import com.treesofparis.core.map.MapCamera
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.usecase.GetTree
import com.treesofparis.domain.usecase.GetTreesInBounds
import com.treesofparis.domain.usecase.GetUserLocationInCoverage
import com.treesofparis.domain.usecase.ObserveIsFavorite
import com.treesofparis.domain.usecase.ToggleFavorite
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
    private val trees = FakeTreeRepository((1L..3000L).map { tree(it, 48.855, 2.35) } + tree(9001, 48.9059, 2.4101))
    private val favorites = FakeFavoritesRepository()
    private val location = FakeLocationRepository(null)

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()) =
        MapViewModel(
            getTreesInBounds = GetTreesInBounds(trees),
            getTree = GetTree(trees),
            observeIsFavorite = ObserveIsFavorite(favorites),
            toggleFavorite = ToggleFavorite(favorites),
            getUserLocation = GetUserLocationInCoverage(location),
            savedState = saved,
        )

    @Test
    fun initialState_isParisCentre() {
        val state = viewModel().uiState.value
        assertEquals(center, state.initialCamera.center)
        assertNull(state.cameraRequest)
    }

    @Test
    fun onCameraChange_belowMinZoom_showsZoomHintAndLoadsNoTrees() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = MapViewModel.MIN_TREE_ZOOM - 1), near)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.showZoomHint)
            assertTrue(
                vm.uiState.value.markers
                    .isEmpty(),
            )
            assertTrue(trees.boundsCalls.isEmpty())
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
}
