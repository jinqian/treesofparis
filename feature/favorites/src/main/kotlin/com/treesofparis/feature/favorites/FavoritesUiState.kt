package com.treesofparis.feature.favorites

data class FavoriteUiModel(
    val id: Long,
    val title: String,
    val subtitle: String,
    val remarkable: Boolean = false,
)

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data object Empty : FavoritesUiState

    data class Content(
        val items: List<FavoriteUiModel>,
    ) : FavoritesUiState
}
