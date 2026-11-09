package com.treesofparis.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treesofparis.domain.model.FavoriteTree
import com.treesofparis.domain.usecase.ObserveFavorites
import com.treesofparis.domain.usecase.ToggleFavorite
import dagger.hilt.android.lifecycle.HiltViewModel
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
