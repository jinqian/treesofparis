package com.treesofparis.feature.herbier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.treesofparis.core.designsystem.component.LoadingState
import com.treesofparis.core.designsystem.component.SectionHeader
import com.treesofparis.core.designsystem.component.TreesCardGroup
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.leaf.LeafDrawing
import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.util.Locale

const val HERBIER_LIST_TAG = "herbier_list"

/** The Herbier tab: what the trees of Paris are, in figures and leaves. [onGenusClick] shows a genus on the map. */
@Composable
fun HerbierRoute(
    onGenusClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HerbierViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HerbierScreen(
        state = state,
        onShowAllGenera = viewModel::onShowAllGenera,
        onGenusClick = onGenusClick,
        modifier = modifier,
    )
}

@Composable
fun HerbierScreen(
    state: HerbierUiState,
    onShowAllGenera: () -> Unit,
    modifier: Modifier = Modifier,
    onGenusClick: (String) -> Unit = {},
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
                    verticalArrangement = Arrangement.spacedBy(TreesSpacing.l),
                ) {
                    state.herbier.headline?.let { headline -> item { HeadlineCard(headline) } }
                    generaSection(state.herbier.genera, state.showAllGenera, onShowAllGenera, onGenusClick)
                    item { StagesSection(state.herbier.stages, state.herbier.unknownStageCount) }
                    item { RemarkableSection(state.herbier.remarkable) }
                    if (state.herbier.areas.isNotEmpty()) item { AreasSection(state.herbier.areas) }
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

private fun LazyListScope.generaSection(
    genera: List<GenusUi>,
    showAll: Boolean,
    onShowAll: () -> Unit,
    onGenusClick: (String) -> Unit,
) {
    if (genera.isEmpty()) return
    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(stringResource(R.string.herbier_section_genera))
            Text(
                stringResource(R.string.herbier_genera_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = TreesSpacing.s),
            )
        }
    }
    val visible = if (showAll) genera else genera.take(HerbierUi.COLLAPSED_GENERA)
    item {
        TreesCardGroup {
            visible.forEachIndexed { index, genus ->
                GenusRow(genus, onClick = { onGenusClick(genus.genus) })
                if (index < visible.lastIndex) TreesDivider()
            }
            if (visible.size < genera.size) {
                TreesDivider()
                TextButton(onClick = onShowAll, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(
                        stringResource(R.string.herbier_show_all, genera.size),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun GenusRow(
    genus: GenusUi,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = stringResource(R.string.herbier_see_on_map),
                    role = Role.Button,
                    onClick = onClick,
                ).padding(start = TreesSpacing.m, end = TreesSpacing.s, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            LeafDrawing(genus.leaf, Modifier.size(34.dp), strokeWidth = 1.3.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s), verticalAlignment = Alignment.Bottom) {
                Text(
                    stringResource(genus.leaf.label),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(genus.share, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            Bar(genus.fraction, height = 4.dp)
            Text(
                stringResource(R.string.herbier_trees, genus.count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            TreesIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** A thin horizontal bar, [fraction] of the available width filled with the tree-pin green. */
@Composable
private fun Bar(
    fraction: Float,
    height: Dp,
    color: Color = TreesTheme.colors.treePin,
) {
    val shape = RoundedCornerShape(height / 2)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape),
    ) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(height).background(color, shape))
    }
}

@Composable
private fun StagesSection(
    stages: List<StageUi>,
    unknown: String?,
) {
    if (stages.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        SectionHeader(stringResource(R.string.herbier_section_stages))
        TreesCardGroup {
            Column(
                modifier = Modifier.padding(TreesSpacing.m),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                stages.forEach { stage ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stage.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                stage.count,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Bar(stage.fraction, height = 8.dp)
                    }
                }
                if (unknown != null) {
                    Text(
                        stringResource(R.string.herbier_unknown_stage, unknown),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RemarkableSection(remarkable: RemarkableUi) {
    val colors = MaterialTheme.colorScheme
    val summary =
        remarkable.leading?.let {
            val label =
                if (it.count ==
                    1
                ) {
                    stringResource(it.leaf.label).lowercase(Locale.FRENCH)
                } else {
                    stringResource(it.leaf.pluralLabel)
                }
            stringResource(R.string.herbier_remarkable_count_leading, remarkable.count, it.count, label)
        } ?: stringResource(R.string.herbier_remarkable_count, remarkable.count)
    Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        SectionHeader(stringResource(R.string.herbier_section_remarkable))
        // No inner padding: the gold header fills the card up to its rounded top edge.
        TreesCardGroup(contentPadding = PaddingValues()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.tertiaryContainer).padding(TreesSpacing.m),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(TreesTheme.colors.pinRing, CircleShape)
                        .padding(2.dp)
                        .background(TreesTheme.colors.remarkable, CircleShape),
                )
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = colors.onTertiaryContainer)
            }
            remarkable.tallest?.let {
                Record(
                    stringResource(R.string.herbier_record_tallest),
                    stringResource(R.string.herbier_record_height, it.name, it.value),
                    it.place,
                )
            }
            if (remarkable.tallest != null && remarkable.widest != null) TreesDivider()
            remarkable.widest?.let {
                Record(
                    stringResource(R.string.herbier_record_widest),
                    stringResource(R.string.herbier_record_girth, it.name, it.value),
                    it.place,
                )
            }
        }
    }
}

@Composable
private fun Record(
    title: String,
    value: String,
    place: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = TreesSpacing.m, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            title.uppercase(Locale.FRENCH),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AreasSection(areas: List<AreaUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        SectionHeader(stringResource(R.string.herbier_section_areas))
        TreesCardGroup {
            areas.forEachIndexed { index, area ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = TreesSpacing.m, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        area.rank.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(28.dp),
                    )
                    Text(area.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        area.count,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (index < areas.lastIndex) TreesDivider()
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
        HerbierScreen(
            state =
                HerbierUiState.Content(
                    HerbierUi(
                        totalTrees = "220 339",
                        genusCount = "202",
                        taxonCount = "1 004",
                        headline =
                            HeadlineUi(
                                TreeLeaf.PLATANUS,
                                5,
                                "42 591",
                                GenusCountUi(TreeLeaf.AESCULUS, "24 795"),
                            ),
                        genera =
                            listOf(
                                GenusUi(TreeLeaf.PLATANUS, "Platanus", "19,3 %", "42 591", 1f),
                                GenusUi(TreeLeaf.AESCULUS, "Aesculus", "11,3 %", "24 795", 0.58f),
                            ),
                        stages = listOf(StageUi("Jeune", "45 781", 0.51f), StageUi("Adulte", "89 118", 1f)),
                        unknownStageCount = "36 239",
                        remarkable =
                            RemarkableUi(
                                count = "181",
                                leading = LeadingGenusUi(TreeLeaf.PLATANUS, 44),
                                tallest = RecordUi("Platane", "45 m", "Bois de Boulogne · Route du Lac à Bagatelle"),
                                widest = RecordUi("Platane", "7,40 m", "Paris 19e · Parc des Buttes Chaumont"),
                            ),
                        areas = listOf(AreaUi(1, "Paris 15e", "18 622")),
                    ),
                ),
            onShowAllGenera = {},
        )
    }
}
