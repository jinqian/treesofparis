package com.treesofparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.leaf.LeafDrawing
import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/**
 * Two segments, « Tous les arbres » and « Remarquables », whose dots match the pins so the control doubles as a
 * legend, plus a « ? » button that explains what a remarkable tree is.
 */
@Composable
internal fun RemarkableFilterBar(
    remarkableOnly: Boolean,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(colors.surfaceContainerHighest, CircleShape)
                    .border(1.dp, colors.outlineVariant, CircleShape)
                    .padding(4.dp)
                    .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Segment(
                label = stringResource(R.string.map_filter_all),
                dotColor = TreesTheme.colors.treePin,
                selected = !remarkableOnly,
                onClick = { onRemarkableFilterChange(false) },
                modifier = Modifier.weight(1f),
            )
            Segment(
                label = stringResource(R.string.map_filter_remarkable),
                dotColor = TreesTheme.colors.remarkable,
                selected = remarkableOnly,
                onClick = { onRemarkableFilterChange(true) },
                modifier = Modifier.weight(1f),
            )
        }
        Surface(
            shape = CircleShape,
            color = colors.surfaceContainerLow,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 4.dp,
            modifier = Modifier.size(48.dp),
        ) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = TreesIcons.Help,
                    contentDescription = stringResource(R.string.map_remarkable_info_title),
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun Segment(
    label: String,
    dotColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxHeight()
                .then(
                    if (selected) {
                        Modifier.shadow(1.dp, CircleShape).background(colors.surfaceContainerLow, CircleShape)
                    } else {
                        Modifier
                    },
                ).selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(color = dotColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

internal const val LEGEND_DOT_TAG = "legend_dot"

/** One dot size for every legend, so ordinary and remarkable trees read as equals in the key. */
private val LEGEND_DOT_SIZE = 12.dp

/** A pin as drawn on the map: a coloured dot with a paper ring. Decorative. */
@Composable
internal fun LegendDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .testTag(LEGEND_DOT_TAG)
                .size(LEGEND_DOT_SIZE + 4.dp)
                .background(TreesTheme.colors.pinRing, CircleShape)
                .padding(2.dp)
                .background(color, CircleShape),
    )
}

/** The bottom sheet body explaining remarkable trees, with the legend and a shortcut to the filter. */
@Composable
internal fun RemarkableInfoContent(
    onShowRemarkableOnly: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = TreesSpacing.l)
                .padding(top = TreesSpacing.xs, bottom = TreesSpacing.l)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(R.string.map_remarkable_info_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.map_remarkable_info_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            TreesDivider()
            LegendRow(stringResource(R.string.map_legend_tree), TreesTheme.colors.treePin)
            TreesDivider()
            LegendRow(stringResource(R.string.map_detail_remarkable), TreesTheme.colors.remarkable)
            TreesDivider()
        }
        Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
            Button(onClick = onShowRemarkableOnly, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.map_remarkable_info_show), style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.map_remarkable_info_close), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) { LegendDot(color) }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * « Seulement : Tilleul » with the genus leaf: tapping the name opens the « Essences » sheet to choose another one, the
 * cross shows every tree again.
 */
@Composable
internal fun GenusFilterPill(
    filter: GenusFilterUi,
    onOpen: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val name =
        if (filter.leaf == TreeLeaf.GENERIC) filter.commonName ?: filter.genus else stringResource(filter.leaf.label)
    Surface(
        modifier = modifier.testTag(GENUS_PILL_TAG).height(GENUS_PILL_HEIGHT),
        shape = CircleShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier =
                    Modifier
                        .clickable(onClickLabel = stringResource(R.string.map_genus_change), onClick = onOpen)
                        .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LeafDrawing(filter.leaf, Modifier.size(22.dp), strokeWidth = 1.3.dp)
                Text(stringResource(R.string.map_filter_genus, name), style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onClear) {
                Icon(
                    TreesIcons.Close,
                    contentDescription = stringResource(R.string.map_filter_genus_clear),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Small note under the segments while only remarkable trees are shown. */
@Composable
internal fun RemarkableOnlyCaption(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = CAPTION_ALPHA),
        shadowElevation = 1.dp,
    ) {
        Text(
            text = stringResource(R.string.map_filter_remarkable_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = TreesSpacing.s),
        )
    }
}

private const val CAPTION_ALPHA = 0.94f

@PreviewLightDark
@Composable
private fun RemarkableFilterBarPreview() {
    TreesTheme {
        Column(Modifier.padding(TreesSpacing.m), verticalArrangement = Arrangement.spacedBy(TreesSpacing.m)) {
            RemarkableFilterBar(
                remarkableOnly = false,
                onRemarkableFilterChange = {},
                onInfoClick = {},
            )
            RemarkableFilterBar(
                remarkableOnly = true,
                onRemarkableFilterChange = {},
                onInfoClick = {},
            )
            RemarkableOnlyCaption()
        }
    }
}

@PreviewLightDark
@Composable
private fun RemarkableInfoContentPreview() {
    TreesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            RemarkableInfoContent(onShowRemarkableOnly = {}, onClose = {})
        }
    }
}
