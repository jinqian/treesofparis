package fr.arbresdeparis.feature.favorites

data class FavoriteUiModel(
    val id: Long,
    /** The tree's name; null for a removed tree the app never recorded. */
    val title: String?,
    /** « Paris 19e · Quai de la Seine »; null when unknown. */
    val subtitle: String?,
    val remarkable: Boolean = false,
    /** The tree is no longer in the City's data (felled or replaced): the row cannot open it on the map. */
    val gone: Boolean = false,
)

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data object Empty : FavoritesUiState

    data class Content(
        val items: List<FavoriteUiModel>,
    ) : FavoritesUiState
}
