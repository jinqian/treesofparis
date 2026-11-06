package com.treesofparis.feature.favorites

import app.cash.turbine.test
import com.treesofparis.domain.usecase.ObserveFavorites
import com.treesofparis.domain.usecase.ToggleFavorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val trees = FakeTreeRepository(listOf(tree(1, "Platane"), tree(2, "Tilleul", "PARIS 1ER ARRDT")))

    private fun viewModel(favorites: FakeFavoritesRepository) =
        FavoritesViewModel(ObserveFavorites(favorites, trees), ToggleFavorite(favorites))

    @Test
    fun startsLoading() {
        assertEquals(FavoritesUiState.Loading, viewModel(FakeFavoritesRepository()).uiState.value)
    }

    @Test
    fun emptyList_emitsEmpty() =
        runTest(main.dispatcher) {
            viewModel(FakeFavoritesRepository()).uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())
                assertEquals(FavoritesUiState.Empty, awaitItem())
            }
        }

    @Test
    fun favorites_emitContentInOrder() =
        runTest(main.dispatcher) {
            viewModel(FakeFavoritesRepository(listOf(2, 1))).uiState.test {
                skipItems(1)
                val content = awaitItem() as FavoritesUiState.Content
                assertEquals(
                    listOf(
                        FavoriteUiModel(id = 2, title = "Tilleul", subtitle = "Quai de la Seine · Paris 1er"),
                        FavoriteUiModel(id = 1, title = "Platane", subtitle = "Quai de la Seine · Paris 19e"),
                    ),
                    content.items,
                )
            }
        }

    @Test
    fun onRemove_togglesFavorite() =
        runTest(main.dispatcher) {
            val favorites = FakeFavoritesRepository(listOf(1, 2))
            val vm = viewModel(favorites)
            vm.onRemove(1)
            advanceUntilIdle()
            assertEquals(listOf(2L), favorites.ids.value)
        }
}
