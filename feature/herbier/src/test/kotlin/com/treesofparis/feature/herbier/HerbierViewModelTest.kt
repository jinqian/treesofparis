package com.treesofparis.feature.herbier

import com.treesofparis.domain.usecase.GetFloweringSeasons
import com.treesofparis.domain.usecase.GetRemarkableTrees
import com.treesofparis.domain.usecase.GetTreeStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HerbierViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private fun viewModel() =
        HerbierViewModel(
            GetTreeStatistics(FakeStatisticsRepository()),
            GetFloweringSeasons(FakeFloweringRepository()),
            GetRemarkableTrees(FakeTreeRepository()),
            NOVEMBER,
        )

    @Test
    fun startsLoading() {
        assertEquals(HerbierUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun loadsStatisticsAndTheFloweringOfTheParisMonth() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.uiState.launchIn(backgroundScope)
            advanceUntilIdle()
            val content = vm.uiState.value as HerbierUiState.Content
            assertEquals("220\u202F339", content.herbier.totalTrees)
            assertEquals(11, content.flowering.currentMonth)
            assertEquals(SEASONS.size, content.flowering.rows.size)
            assertEquals(4, content.remarkableTrees.sumOf { it.trees.size })
        }
}
