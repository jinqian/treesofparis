package com.treesofparis.feature.herbier

import com.treesofparis.domain.usecase.GetTreeStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HerbierViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private fun viewModel() = HerbierViewModel(GetTreeStatistics(FakeStatisticsRepository()))

    @Test
    fun startsLoading() {
        assertEquals(HerbierUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun loadsStatistics_showingTheTopGeneraFirst() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.uiState.launchIn(backgroundScope)
            advanceUntilIdle()
            val content = vm.uiState.value as HerbierUiState.Content
            assertEquals("220 339", content.herbier.totalTrees)
            assertFalse(content.showAllGenera)
        }

    @Test
    fun onShowAllGenera_expandsTheList() =
        runTest(main.dispatcher) {
            val vm = viewModel()
            vm.uiState.launchIn(backgroundScope)
            advanceUntilIdle()
            vm.onShowAllGenera()
            advanceUntilIdle()
            assertTrue((vm.uiState.value as HerbierUiState.Content).showAllGenera)
        }
}
