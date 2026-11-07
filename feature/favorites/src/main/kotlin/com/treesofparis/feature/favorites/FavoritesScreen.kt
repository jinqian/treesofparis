package com.treesofparis.feature.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.treesofparis.core.designsystem.component.EmptyState
import com.treesofparis.core.designsystem.component.LoadingState
import com.treesofparis.core.designsystem.component.SprigIllustration
import com.treesofparis.core.designsystem.component.TreeListItem
import com.treesofparis.core.designsystem.component.TreesCardGroup
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/** The Favoris tab. [onTreeClick] opens the tree on the map; [onExploreMap] switches to the Carte tab. */
@Composable
fun FavoritesRoute(
    onTreeClick: (Long) -> Unit,
    onExploreMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        state = state,
        onTreeClick = onTreeClick,
        onRemove = viewModel::onRemove,
        onExploreMap = onExploreMap,
        modifier = modifier,
    )
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onTreeClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onExploreMap: () -> Unit = {},
) {
    val subtitle =
        (state as? FavoritesUiState.Content)?.let {
            pluralStringResource(R.plurals.favorites_count, it.items.size, it.items.size)
        }
    TreesScaffold(
        title = stringResource(R.string.favorites_title),
        subtitle = subtitle,
        modifier = modifier,
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (state) {
            FavoritesUiState.Loading -> {
                LoadingState(contentModifier)
            }

            FavoritesUiState.Empty -> {
                EmptyState(
                    title = stringResource(R.string.favorites_empty_title),
                    message = stringResource(R.string.favorites_empty),
                    illustration = { SprigIllustration() },
                    actionLabel = stringResource(R.string.favorites_explore_map),
                    onAction = onExploreMap,
                    modifier = contentModifier,
                )
            }

            is FavoritesUiState.Content -> {
                FavoritesList(state.items, onTreeClick, onRemove, contentModifier)
            }
        }
    }
}

@Composable
private fun FavoritesList(
    items: List<FavoriteUiModel>,
    onTreeClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A handful of trees at most: one card in a scrolling column reads better than a lazy list of loose rows.
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
    ) {
        TreesCardGroup {
            items.forEachIndexed { index, item ->
                key(item.id) {
                    TreeListItem(
                        title = item.title,
                        subtitle = item.subtitle,
                        remarkable = item.remarkable,
                        onClick = { onTreeClick(item.id) },
                        trailing = { RemoveButton(item, onRemove) },
                    )
                    if (index < items.lastIndex) TreesDivider()
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
            tint = MaterialTheme.colorScheme.primary,
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
                        FavoriteUiModel(1, "Platane", "Quai de la Seine · Paris 19e", remarkable = true),
                        FavoriteUiModel(2, "Cerisier à fleurs", "Cimetière de Pantin · Seine-Saint-Denis"),
                    ),
                ),
            onTreeClick = {},
            onRemove = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun FavoritesEmptyPreview() {
    TreesTheme { FavoritesScreen(state = FavoritesUiState.Empty, onTreeClick = {}, onRemove = {}) }
}
