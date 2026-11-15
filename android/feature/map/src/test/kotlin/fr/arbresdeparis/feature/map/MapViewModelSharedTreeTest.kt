package fr.arbresdeparis.feature.map

import androidx.lifecycle.SavedStateHandle
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.usecase.GetFloweringSeason
import fr.arbresdeparis.domain.usecase.GetGenera
import fr.arbresdeparis.domain.usecase.GetNearestTrees
import fr.arbresdeparis.domain.usecase.GetRemarkableStory
import fr.arbresdeparis.domain.usecase.GetTree
import fr.arbresdeparis.domain.usecase.GetTreeClusters
import fr.arbresdeparis.domain.usecase.GetTreesInBounds
import fr.arbresdeparis.domain.usecase.LocateUser
import fr.arbresdeparis.domain.usecase.ObserveIsFavorite
import fr.arbresdeparis.domain.usecase.ToggleFavorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** A shared link (issue #88) may point to a tree that has left the data since. */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelSharedTreeTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val trees = FakeTreeRepository(listOf(tree(9003, 48.8552, 2.3502)))
    private val favorites = FakeFavoritesRepository()

    private fun viewModel() =
        MapViewModel(
            getTreesInBounds = GetTreesInBounds(trees),
            getTreeClusters = GetTreeClusters(trees),
            getTree = GetTree(trees),
            observeIsFavorite = ObserveIsFavorite(favorites),
            toggleFavorite = ToggleFavorite(favorites, trees),
            locateUser = LocateUser(FakeLocationRepository(null)),
            getNearestTrees = GetNearestTrees(trees),
            getFloweringSeason = GetFloweringSeason(FakeFloweringRepository()),
            getRemarkableStory = GetRemarkableStory(FakeRemarkableStoryRepository()),
            getGenera = GetGenera(FakeStatisticsRepository()),
            clock = NOVEMBER,
            savedState = SavedStateHandle(),
        )

    @Test
    fun onOpenTree_goneFromTheData_goesToItsPlaceAndSaysSo() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpenTree(424242, fallback = GeoPoint(48.86, 2.35))
            advanceUntilIdle()
            assertEquals(
                GeoPoint(48.86, 2.35),
                vm.uiState.value.cameraRequest
                    ?.camera
                    ?.center,
            )
            assertEquals(MapMessage.TREE_NOT_FOUND, vm.uiState.value.message)
            assertEquals(null, vm.uiState.value.selectedTree)
        }

    @Test
    fun onOpenTree_goneWithoutAPlace_onlySaysSo() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpenTree(424242)
            advanceUntilIdle()
            assertEquals(null, vm.uiState.value.cameraRequest)
            assertEquals(MapMessage.TREE_NOT_FOUND, vm.uiState.value.message)
        }
}
