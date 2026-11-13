package fr.arbresdeparis.feature.map

import androidx.lifecycle.SavedStateHandle
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

/** A remarkable tree's sheet comes with its story (issue #74); other trees have none. */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelStoryTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val remarkable = tree(9002, 48.8551, 2.3501).copy(remarkable = true)
    private val lime = tree(9003, 48.8552, 2.3502).copy(genus = "Tilia")
    private val trees = FakeTreeRepository(listOf(remarkable, lime))
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
            getRemarkableStory = GetRemarkableStory(FakeRemarkableStoryRepository(story(9002))),
            getGenera = GetGenera(FakeStatisticsRepository()),
            clock = NOVEMBER,
            savedState = SavedStateHandle(),
        )

    @Test
    fun remarkableTree_comesWithItsStory() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(9002)
            advanceUntilIdle()
            assertEquals(
                "Square Maurice Gardette",
                vm.uiState.value.selectedTree
                    ?.story
                    ?.site,
            )
        }

    @Test
    fun ordinaryTree_hasNoStory() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(9003)
            advanceUntilIdle()
            assertNull(
                vm.uiState.value.selectedTree
                    ?.story,
            )
        }

    @Test
    fun openingTheStory_closesTheSheet_soBackLandsOnTheMap() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onMarkerClick(9002)
            advanceUntilIdle()
            vm.onStoryOpen()
            assertNull(vm.uiState.value.selectedTree)
        }
}
