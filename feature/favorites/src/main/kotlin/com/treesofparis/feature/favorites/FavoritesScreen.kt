package com.treesofparis.feature.favorites

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.treesofparis.core.designsystem.component.EmptyState
import com.treesofparis.core.designsystem.component.LoadingState
import com.treesofparis.core.designsystem.component.TreeListItem
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesTheme

/** The Favoris tab. [onTreeClick] opens the tree on the map. */
@Composable
fun FavoritesRoute(
    onTreeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(state = state, onTreeClick = onTreeClick, onRemove = viewModel::onRemove, modifier = modifier)
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onTreeClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(title = stringResource(R.string.favorites_title), modifier = modifier) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (state) {
            FavoritesUiState.Loading -> {
                LoadingState(contentModifier)
            }

            FavoritesUiState.Empty -> {
                EmptyState(
                    message = stringResource(R.string.favorites_empty),
                    modifier = contentModifier,
                )
            }

            is FavoritesUiState.Content -> {
                LazyColumn(contentModifier) {
                    items(state.items, key = { it.id }) { item ->
                        TreeListItem(
                            title = item.title,
                            subtitle = item.subtitle,
                            onClick = { onTreeClick(item.id) },
                            trailing = { RemoveButton(item, onRemove) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveButton(
    item: FavoriteUiModel,
    onRemove: (Long) -> Unit,
) {
    IconButton(onClick = { onRemove(item.id) }) {
        Icon(
            imageVector = TreesIcons.Favorite,
            contentDescription = stringResource(R.string.favorites_remove, item.title),
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@PreviewLightDark
@Composable
private fun FavoritesScreenPreview() {
    TreesTheme {
        FavoritesScreen(
            state =
                FavoritesUiState.Content(
                    listOf(
                        FavoriteUiModel(1, "Platane", "Quai de la Seine · Paris 19e"),
                        FavoriteUiModel(2, "Cerisier à fleurs", "Cimetière de Pantin · Seine-Saint-Denis"),
                    ),
                ),
            onTreeClick = {},
            onRemove = {},
        )
    }
}
