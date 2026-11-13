package fr.arbresdeparis.feature.map

import fr.arbresdeparis.domain.usecase.GetRemarkableStory
import fr.arbresdeparis.domain.usecase.GetTree
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** « Son histoire » loads its tree: the page shows at once, then its story, or says it has none. */
@OptIn(ExperimentalCoroutinesApi::class)
class RemarkableStoryViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val noisetier = tree(9002).copy(remarkable = true)
    private val lime = tree(9003).copy(genus = "Tilia")

    private fun viewModel() =
        RemarkableStoryViewModel(
            GetTree(FakeTreeRepository(listOf(noisetier, lime))),
            GetRemarkableStory(FakeRemarkableStoryRepository(story(9002))),
        )

    @Test
    fun startsLoading() {
        assertEquals(RemarkableStoryUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun aRemarkableTree_showsItsStory() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpen(9002)
            advanceUntilIdle()
            val content = vm.uiState.value as RemarkableStoryUiState.Content
            assertEquals("Square Maurice Gardette", content.detail.story?.site)
        }

    @Test
    fun aTreeWithoutStory_isUnavailable() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.onOpen(9003)
            advanceUntilIdle()
            assertEquals(RemarkableStoryUiState.Unavailable, vm.uiState.value)
        }
}
