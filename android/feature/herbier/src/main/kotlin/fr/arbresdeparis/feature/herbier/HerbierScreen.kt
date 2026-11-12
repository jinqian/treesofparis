package fr.arbresdeparis.feature.herbier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.arbresdeparis.core.designsystem.component.LoadingState
import fr.arbresdeparis.core.designsystem.component.SectionHeader
import fr.arbresdeparis.core.designsystem.component.TreesScaffold
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import java.time.Month
import java.util.Locale

const val HERBIER_LIST_TAG = "herbier_list"

/**
 * The Herbier tab, level 1: a headline, what flowers this month and four cards. [onOpenSection] opens a card's
 * level-2 screen on the Herbier back stack.
 */
@Composable
fun HerbierRoute(
    onOpenSection: (HerbierSection) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HerbierViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HerbierScreen(state = state, onOpenSection = onOpenSection, modifier = modifier)
}

@Composable
fun HerbierScreen(
    state: HerbierUiState,
    onOpenSection: (HerbierSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle =
        (state as? HerbierUiState.Content)?.herbier?.let {
            stringResource(R.string.herbier_totals, it.totalTrees, it.genusCount, it.taxonCount)
        }
    TreesScaffold(title = stringResource(R.string.herbier_title), subtitle = subtitle, modifier = modifier) { padding ->
        when (state) {
            HerbierUiState.Loading -> {
                LoadingState(Modifier.padding(padding))
            }

            is HerbierUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).testTag(HERBIER_LIST_TAG),
                    contentPadding =
                        PaddingValues(
                            start = TreesSpacing.m,
                            end = TreesSpacing.m,
                            bottom = TreesSpacing.l,
                        ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.herbier.headline?.let { headline -> item { HeadlineCard(headline) } }
                    item { SeasonCard(state.flowering, onClick = { onOpenSection(HerbierSection.FLOWERING) }) }
                    item {
                        SectionHeader(
                            stringResource(R.string.herbier_explore),
                            modifier = Modifier.padding(top = TreesSpacing.s),
                        )
                    }
                    item { ExploreGrid(state, onOpenSection) }
                }
            }
        }
    }
}

@Composable
private fun HeadlineCard(headline: HeadlineUi) {
    val colors = MaterialTheme.colorScheme
    val name = stringResource(headline.leaf.label).lowercase(Locale.FRENCH)
    val plural = stringResource(headline.leaf.pluralLabel)
    val detail =
        headline.runnerUp?.let {
            stringResource(
                R.string.herbier_headline_detail,
                headline.count,
                plural,
                it.count,
                stringResource(it.leaf.pluralLabel),
            )
        } ?: stringResource(R.string.herbier_headline_detail_alone, headline.count, plural)
    Surface(shape = MaterialTheme.shapes.large, color = colors.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Box {
            LeafDrawing(
                leaf = headline.leaf,
                color = colors.primary.copy(alpha = WATERMARK_ALPHA),
                modifier = Modifier.align(Alignment.BottomEnd).size(150.dp).offset(x = 18.dp, y = 22.dp),
            )
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            ) {
                Text(
                    text = stringResource(R.string.herbier_did_you_know).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
                Text(
                    text = stringResource(R.string.herbier_headline, headline.oneIn, name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth(HEADLINE_WIDTH),
                )
                Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
            }
        }
    }
}

private const val WATERMARK_ALPHA = 0.55f
private const val HEADLINE_WIDTH = 0.7f

@PreviewLightDark
@Composable
@Suppress("MagicNumber") // sample figures
private fun HerbierScreenPreview() {
    TreesTheme {
        HerbierScreen(state = previewContent(Month.NOVEMBER), onOpenSection = {})
    }
}
