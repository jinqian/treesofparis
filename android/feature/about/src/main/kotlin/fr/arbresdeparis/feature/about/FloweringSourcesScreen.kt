package fr.arbresdeparis.feature.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.arbresdeparis.core.designsystem.component.InfoRow
import fr.arbresdeparis.core.designsystem.component.InfoRowTrailing
import fr.arbresdeparis.core.designsystem.component.SectionHeader
import fr.arbresdeparis.core.designsystem.component.TreesCardGroup
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.component.TreesScaffold
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

const val FLOWERING_SOURCES_LIST_TAG = "flowering_sources_list"

/** The guide sheets the flowering months come from, reached from À propos. */
@Composable
fun FloweringSourcesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FloweringSourcesViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    FloweringSourcesScreen(
        sources = viewModel.sources,
        onOpenUrl = uriHandler::openUri,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun FloweringSourcesScreen(
    sources: List<FloweringSourceUi>,
    onOpenUrl: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(
        title = stringResource(R.string.flowering_sources_title),
        subtitle = stringResource(R.string.flowering_sources_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).testTag(FLOWERING_SOURCES_LIST_TAG),
            contentPadding = PaddingValues(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
        ) {
            item {
                Text(
                    text = stringResource(R.string.flowering_sources_method),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = TreesSpacing.s, vertical = TreesSpacing.s),
                )
            }
            item {
                Column(modifier = Modifier.padding(top = TreesSpacing.m)) {
                    SectionHeader(stringResource(R.string.flowering_sources_sheets))
                    SourcesCard(sources, onOpenUrl)
                }
            }
        }
    }
}

@Composable
private fun SourcesCard(
    sources: List<FloweringSourceUi>,
    onOpenUrl: (String) -> Unit,
) {
    TreesCardGroup {
        sources.forEachIndexed { index, source ->
            InfoRow(
                title = source.commonName,
                subtitle = source.detail,
                trailing = InfoRowTrailing.EXTERNAL,
                onClick = { onOpenUrl(source.url) },
            )
            if (index < sources.lastIndex) TreesDivider()
        }
    }
}

@PreviewLightDark
@Composable
private fun FloweringSourcesScreenPreview() {
    TreesTheme {
        FloweringSourcesScreen(
            sources =
                listOf(
                    FloweringSourceUi("Merisier", "Prunus avium · mars – avril · fleurs voyantes", ""),
                    FloweringSourceUi("Platane commun", "Platanus × hispanica · mai – juin", ""),
                ),
            onOpenUrl = {},
            onBack = {},
        )
    }
}
