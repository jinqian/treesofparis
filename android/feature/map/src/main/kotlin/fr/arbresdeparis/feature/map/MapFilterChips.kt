package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

internal const val FILTER_ROW_TAG = "filter_row"

/**
 * The map's two filters on one line, which combine: which trees (« Tous les arbres ▾ », or the essence chosen in the
 * « Essences » sheet) and « Remarquables » (on or off), then « ? », which explains them. Their dots match the pins, so
 * they double as the legend. The
 * row scrolls sideways when it does not fit; [contentPadding] keeps the chips off the screen edges while letting the
 * row scroll under them.
 */
@Composable
internal fun MapFilterChips(
    remarkableOnly: Boolean,
    genusFilter: GenusFilterUi?,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onGenusPickerOpen: () -> Unit,
    onGenusFilterClear: () -> Unit,
    onHelpOpen: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = TreesSpacing.m),
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(FILTER_ROW_TAG)
                .horizontalScroll(rememberScrollState())
                .padding(contentPadding)
                // Room for the chips' shadows, which the scrolling row would otherwise clip.
                .padding(vertical = TreesSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (genusFilter == null) {
            GenusFilterButton(onClick = onGenusPickerOpen)
        } else {
            GenusFilterPill(filter = genusFilter, onOpen = onGenusPickerOpen, onClear = onGenusFilterClear)
        }
        RemarkableChip(checked = remarkableOnly, onCheckedChange = onRemarkableFilterChange)
        HelpButton(onClick = onHelpOpen)
    }
}

/** « ? »: how the filters work and what a remarkable tree is. */
@Composable
private fun HelpButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
        modifier = Modifier.size(GENUS_PILL_HEIGHT),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = TreesIcons.Help,
                contentDescription = stringResource(R.string.map_filters_help),
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** « ● Tous les arbres ▾ »: every tree is shown; the ▾ says a tap opens the « Essences » sheet. */
@Composable
internal fun GenusFilterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier =
            modifier
                .testTag(GENUS_PILL_TAG)
                .height(GENUS_PILL_HEIGHT)
                .clickable(
                    onClickLabel = stringResource(R.string.map_genus_choose),
                    role = Role.Button,
                    onClick = onClick,
                ),
        shape = CircleShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = TreesTheme.colors.treePin)
            Text(
                stringResource(R.string.map_filter_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Icon(TreesIcons.ChevronDown, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * The chosen essence, « 🍃 Tilleul ▾ × », in sage: tapping the name opens the « Essences » sheet to choose another one,
 * the cross shows every tree again.
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
        color = colors.primaryContainer,
        border = BorderStroke(1.dp, colors.primary),
        shadowElevation = 2.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier =
                    Modifier
                        .clickable(onClickLabel = stringResource(R.string.map_genus_change), onClick = onOpen)
                        .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LeafDrawing(filter.leaf, Modifier.size(22.dp), strokeWidth = 1.3.dp)
                Text(
                    name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onPrimaryContainer,
                    maxLines = 1,
                )
                Icon(TreesIcons.ChevronDown, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onClear) {
                Icon(
                    TreesIcons.Close,
                    contentDescription = stringResource(R.string.map_filter_genus_clear),
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** « ● Remarquables », on or off; sage with a ✓ when on. Combines with the essence. */
@Composable
private fun RemarkableChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier =
            Modifier
                .height(GENUS_PILL_HEIGHT)
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        shape = CircleShape,
        color = if (checked) colors.primaryContainer else colors.surfaceContainerLow,
        border = BorderStroke(1.dp, if (checked) colors.primary else colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = if (checked) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(color = TreesTheme.colors.remarkable)
            Text(
                stringResource(R.string.map_filter_remarkable),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Medium,
                color = if (checked) colors.onPrimaryContainer else colors.onSurface,
                maxLines = 1,
            )
            if (checked) {
                Icon(TreesIcons.Check, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MapFilterChipsPreview() {
    TreesTheme {
        Column(
            modifier = Modifier.padding(vertical = TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        ) {
            MapFilterChips(false, null, {}, {}, {}, {})
            MapFilterChips(true, GenusFilterUi("Tilia", TreeLeaf.TILIA), {}, {}, {}, {})
        }
    }
}
