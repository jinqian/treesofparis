package fr.arbresdeparis.feature.herbier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

/** Rows of the Floraisons card: the year from hazel to sophora. */
private val SAMPLE_ROWS = listOf(TreeLeaf.CORYLUS, TreeLeaf.PRUNUS, TreeLeaf.PLATANUS, TreeLeaf.STYPHNOLOBIUM)
private const val SAMPLE_SIZE = 4

/** The four cards of the Herbier home, two by two. */
@Composable
internal fun ExploreGrid(
    state: HerbierUiState.Content,
    onOpenSection: (HerbierSection) -> Unit,
) {
    val herbier = state.herbier
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardPair(
            first = {
                ExploreCard(
                    title = stringResource(R.string.herbier_section_genera),
                    detail = stringResource(R.string.herbier_card_genera, herbier.genera.size, herbier.drawnShare),
                    onClick = { onOpenSection(HerbierSection.GENERA) },
                    modifier = it,
                ) { GeneraIllustration(herbier.genera) }
            },
            second = {
                ExploreCard(
                    title = stringResource(R.string.herbier_section_flowering),
                    detail = stringResource(R.string.herbier_card_flowering, state.flowering.rows.size),
                    onClick = { onOpenSection(HerbierSection.FLOWERING) },
                    modifier = it,
                ) {
                    val sample = state.flowering.rows.filter { row -> row.leaf in SAMPLE_ROWS }
                    MiniCalendar(sample.ifEmpty { state.flowering.rows.take(SAMPLE_SIZE) })
                }
            },
        )
        CardPair(
            first = {
                ExploreCard(
                    title = stringResource(R.string.herbier_section_remarkable),
                    detail =
                        herbier.remarkable.tallest?.let { record ->
                            stringResource(R.string.herbier_card_remarkable, record.value)
                        }
                            ?: stringResource(R.string.herbier_card_remarkable_none),
                    onClick = { onOpenSection(HerbierSection.REMARKABLE) },
                    modifier = it,
                ) { RemarkableIllustration(herbier.remarkable.count) }
            },
            second = {
                ExploreCard(
                    title = stringResource(R.string.herbier_section_age_areas),
                    detail = stringResource(R.string.herbier_card_age_areas),
                    onClick = { onOpenSection(HerbierSection.AGE_AND_AREAS) },
                    modifier = it,
                ) { StagesIllustration(herbier.stages) }
            },
        )
    }
}

/** Two cards side by side, as tall as the taller one. */
@Composable
private fun CardPair(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        first(Modifier.weight(1f).fillMaxHeight())
        second(Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun ExploreCard(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    illustration: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(TreesSpacing.m), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.BottomStart) {
                illustration()
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun GeneraIllustration(genera: List<GenusUi>) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        genera.getOrNull(0)?.let { LeafDrawing(it.leaf, Modifier.size(54.dp), strokeWidth = 1.3.dp) }
        genera.getOrNull(1)?.let { LeafDrawing(it.leaf, Modifier.size(40.dp), strokeWidth = 1.3.dp) }
    }
}

@Composable
private fun RemarkableIllustration(count: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .size(24.dp)
                .background(TreesTheme.colors.pinRing, CircleShape)
                .padding(3.dp)
                .background(TreesTheme.colors.remarkable, CircleShape),
        )
        Text(count, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun StagesIllustration(stages: List<StageUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        stages.forEach {
            Box(
                Modifier
                    .fillMaxWidth(it.fraction.coerceIn(0f, 1f))
                    .height(7.dp)
                    .background(TreesTheme.colors.treePin, RoundedCornerShape(4.dp)),
            )
        }
    }
}
