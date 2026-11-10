package fr.arbresdeparis.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.model.FavoriteTree
import fr.arbresdeparis.domain.usecase.ObserveFavorites
import fr.arbresdeparis.domain.usecase.ToggleFavorite
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        observeFavorites: ObserveFavorites,
        private val toggleFavorite: ToggleFavorite,
    ) : ViewModel() {
        val uiState: StateFlow<FavoritesUiState> =
            observeFavorites()
                .map { trees -> trees.toUiState() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoritesUiState.Loading)

        fun onRemove(id: Long) {
            viewModelScope.launch { toggleFavorite(id) }
        }

        private fun List<FavoriteTree>.toUiState(): FavoritesUiState =
            if (isEmpty()) FavoritesUiState.Empty else FavoritesUiState.Content(map { it.toFavoriteUi() })

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
