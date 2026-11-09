package com.treesofparis.feature.map

import androidx.lifecycle.SavedStateHandle
import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MarkerStyle
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.usecase.GetFloweringSeason
import com.treesofparis.domain.usecase.GetGenera
import com.treesofparis.domain.usecase.GetNearestTrees
import com.treesofparis.domain.usecase.GetTree
import com.treesofparis.domain.usecase.GetTreesInBounds
import com.treesofparis.domain.usecase.LocateUser
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

/** The genus filter: the « Essences » sheet, the pill, and how it combines with the remarkable filter. */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelGenusTest {
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

    private fun choice(
        genus: String,
        name: String,
    ) = GenusChoiceUi(genus, name, TreeLeaf.forGenus(genus), count = 1, countLabel = "1", selected = false)

    @Test
    fun genusFilter_showsOnlyThatGenusAtAnyZoom() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onCameraChange(MapCamera(center, zoom = 12.0), GeoBounds.COVERAGE)
            vm.onGenusPick(choice("Tilia", "Tilleul"))
            advanceUntilIdle()
            assertEquals(GenusFilterUi("Tilia", TreeLeaf.TILIA, "Tilleul"), vm.uiState.value.genusFilter)
            assertFalse(vm.uiState.value.showZoomHint)
            assertEquals(
                listOf(9003L),
                vm.uiState.value.markers
                    .map { it.id },
            )
            assertEquals("Tilia", trees.genusCalls.last())
        }

    @Test
    fun genusFilter_combinesWithRemarkable() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onRemarkableFilterChange(true)
            vm.onGenusPick(choice("Platanus", "Platane"))
            vm.onCameraChange(MapCamera(center, zoom = 12.0), GeoBounds.COVERAGE)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.remarkableOnly)
            assertEquals(
                "Platanus",
                vm.uiState.value.genusFilter
                    ?.genus,
            )
            assertEquals(
                listOf(9002L),
                vm.uiState.value.markers
                    .map { it.id },
            )
            assertEquals(true, trees.remarkableOnlyCalls.last())
            assertEquals("Platanus", trees.genusCalls.last())
        }

    @Test
    fun remarkable_onAGenusWithoutRemarkableTrees_dropsTheGenus() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusPick(choice("Tilia", "Tilleul"))
            vm.onRemarkableFilterChange(true)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.remarkableOnly)
            assertNull(vm.uiState.value.genusFilter)
        }

    @Test
    fun genusFromHerbier_showsAllItsTrees_evenAfterRemarkableOnes() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onRemarkableFilterChange(true)
            vm.onGenusFilterRequest("Tilia")
            assertFalse(vm.uiState.value.remarkableOnly)
            assertEquals(
                "Tilia",
                vm.uiState.value.genusFilter
                    ?.genus,
            )
        }

    @Test
    fun genusFromHerbier_withoutDrawing_isNamedInFrench() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusFilterRequest("Ginkgo")
            advanceUntilIdle()
            assertEquals(
                GenusFilterUi("Ginkgo", TreeLeaf.GENERIC, "Arbre aux quarante écus"),
                vm.uiState.value.genusFilter,
            )
        }

    @Test
    fun genusPicker_drawnGeneraInAGrid_othersAfter_currentOneSelected() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusPick(choice("Tilia", "Tilleul"))
            vm.onGenusPickerOpen()
            advanceUntilIdle()
            val picker = vm.uiState.value.genusPicker!!
            assertEquals(listOf("Platanus", "Tilia"), picker.drawn.map { it.genus })
            assertEquals(listOf("Ginkgo"), picker.others.map { it.genus })
            assertEquals(listOf(false, true), picker.drawn.map { it.selected })
            assertEquals("3\u202F001", picker.drawn.first().countLabel)
            assertFalse(picker.remarkableOnly)
        }

    @Test
    fun genusPicker_whileRemarkableOnly_listsGeneraWithRemarkableTrees() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onRemarkableFilterChange(true)
            vm.onGenusPickerOpen()
            advanceUntilIdle()
            val picker = vm.uiState.value.genusPicker!!
            assertEquals(listOf("Platanus"), (picker.drawn + picker.others).map { it.genus })
            assertEquals(1, picker.drawn.single().count)
            assertTrue(picker.remarkableOnly)
        }

    @Test
    fun genusPicker_pickingAGenusFiltersAndCloses() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusPickerOpen()
            advanceUntilIdle()
            vm.onGenusPick(
                vm.uiState.value.genusPicker!!
                    .others
                    .single(),
            )
            assertNull(vm.uiState.value.genusPicker)
            assertEquals(
                GenusFilterUi("Ginkgo", TreeLeaf.GENERIC, "Arbre aux quarante écus"),
                vm.uiState.value.genusFilter,
            )
        }

    @Test
    fun genusPicker_dismissKeepsTheFilter() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusPick(choice("Tilia", "Tilleul"))
            vm.onGenusPickerOpen()
            advanceUntilIdle()
            vm.onGenusPickerDismiss()
            assertNull(vm.uiState.value.genusPicker)
            assertEquals(
                "Tilia",
                vm.uiState.value.genusFilter
                    ?.genus,
            )
        }

    @Test
    fun genusFilter_clear_showsAllTreesAgain() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onGenusPick(choice("Tilia", "Tilleul"))
            vm.onGenusFilterClear()
            vm.onCameraChange(MapCamera(center, zoom = 17.0), near)
            advanceUntilIdle()
            assertNull(vm.uiState.value.genusFilter)
            assertEquals(null, trees.genusCalls.last())
        }

    @Test
    fun genusFilter_fromNavigation_isAppliedOnceAndSurvivesRecreation() =
        runTest(main.dispatcher) {
            val saved = SavedStateHandle()
            viewModel(saved).apply {
                onGenusFilterRequest("Tilia")
                onGenusFilterClear()
            }
            // Recreated: the request is not applied again over the user's choice.
            val recreated = viewModel(saved)
            recreated.onGenusFilterRequest("Tilia")
            assertNull(recreated.uiState.value.genusFilter)
        }
}
