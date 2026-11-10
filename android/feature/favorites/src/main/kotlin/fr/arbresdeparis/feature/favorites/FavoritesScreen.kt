package fr.arbresdeparis.feature.favorites

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
import fr.arbresdeparis.core.designsystem.component.EmptyState
import fr.arbresdeparis.core.designsystem.component.LoadingState
import fr.arbresdeparis.core.designsystem.component.SprigIllustration
import fr.arbresdeparis.core.designsystem.component.TreeListItem
import fr.arbresdeparis.core.designsystem.component.TreesCardGroup
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.component.TreesScaffold
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

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
                    val title = item.displayTitle()
                    TreeListItem(
                        title = title,
                        subtitle = item.displaySubtitle(),
                        remarkable = item.remarkable,
                        // « Retiré des données de la Ville » then where it stood.
                        subtitleMaxLines = if (item.gone) 2 else 1,
                        onClick = if (item.gone) null else ({ onTreeClick(item.id) }),
                        trailing = { RemoveButton(item.id, title, onRemove) },
                    )
                    if (index < items.lastIndex) TreesDivider()
                }
            }
        }
    }
}

@Composable
private fun FavoriteUiModel.displayTitle(): String = title ?: stringResource(R.string.favorites_gone_title)

/** A removed tree says so before where it stood. */
@Composable
private fun FavoriteUiModel.displaySubtitle(): String =
    when {
        !gone -> subtitle.orEmpty()
        subtitle == null -> stringResource(R.string.favorites_gone)
        else -> stringResource(R.string.favorites_gone_place, subtitle)
    }

@Composable
private fun RemoveButton(
    id: Long,
    title: String,
    onRemove: (Long) -> Unit,
) {
    IconButton(onClick = { onRemove(id) }) {
        Icon(
            imageVector = TreesIcons.Favorite,
            contentDescription = stringResource(R.string.favorites_remove, title),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@PreviewLightDark
@Composable
@Suppress("MagicNumber") // sample ids
private fun FavoritesScreenPreview() {
    TreesTheme {
        FavoritesScreen(
            state =
                FavoritesUiState.Content(
                    listOf(
                        FavoriteUiModel(1, "Platane", "Paris 19e · Quai de la Seine", remarkable = true),
                        FavoriteUiModel(2, "Cerisier à fleurs", "Seine-Saint-Denis · Cimetière de Pantin"),
                        FavoriteUiModel(3, "Marronnier", "Paris 6e · Rue de Vaugirard", gone = true),
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
