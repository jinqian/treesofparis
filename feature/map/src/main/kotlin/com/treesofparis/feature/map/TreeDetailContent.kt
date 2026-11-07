package com.treesofparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.icon.TreesIllustrations
import com.treesofparis.core.designsystem.theme.ScientificNameStyle
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.util.Locale

/** The body of the tree detail bottom sheet, laid out like a herbarium label. */
@Composable
fun TreeDetailContent(
    detail: TreeDetailUi,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Icon(
            imageVector = TreesIllustrations.PlaneLeaf,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.align(Alignment.TopEnd).size(150.dp).offset(x = 24.dp),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TreesSpacing.l)
                    .padding(top = TreesSpacing.xs, bottom = TreesSpacing.l)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Header(detail, onFavoriteToggle)
            Location(detail)
            Stats(detail)
            Text(
                text = stringResource(R.string.map_detail_source),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Header(
    detail: TreeDetailUi,
    onFavoriteToggle: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (detail.remarkable) RemarkableEyebrow()
            Text(detail.name, style = MaterialTheme.typography.displaySmall)
            Text(
                text = detail.scientificName ?: stringResource(R.string.map_detail_unknown_species),
                style = ScientificNameStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FavoriteButton(isFavorite = detail.isFavorite, onClick = onFavoriteToggle)
    }
}

@Composable
private fun RemarkableEyebrow() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        Icon(
            imageVector = TreesIcons.Remarkable,
            contentDescription = null,
            tint = TreesTheme.colors.remarkable,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.map_detail_remarkable).uppercase(Locale.FRENCH),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (isFavorite) colors.primaryContainer else colors.surfaceContainerLow,
        border = if (isFavorite) null else BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.padding(top = TreesSpacing.xs).size(48.dp),
    ) {
        IconButton(onClick = onClick, colors = IconButtonDefaults.iconButtonColors(contentColor = colors.primary)) {
            Icon(
                imageVector = if (isFavorite) TreesIcons.Favorite else TreesIcons.FavoriteOutlined,
                contentDescription =
                    stringResource(if (isFavorite) R.string.map_favorite_remove else R.string.map_favorite_add),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun Location(detail: TreeDetailUi) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = TreesIcons.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(detail.address, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f, fill = false))
        Text("·", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(detail.area, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun Stats(detail: TreeDetailUi) {
    Column {
        TreesDivider()
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Stat(
                label = stringResource(R.string.map_detail_height),
                measure = detail.height,
                placeholder = stringResource(R.string.map_detail_unknown_height),
                modifier = Modifier.weight(1f),
            )
            StatSeparator()
            Stat(
                label = stringResource(R.string.map_detail_circumference),
                measure = detail.circumference,
                placeholder = stringResource(R.string.map_detail_unknown_circumference),
                modifier = Modifier.weight(1f).padding(start = TreesSpacing.m),
            )
            StatSeparator()
            Stat(
                label = stringResource(R.string.map_detail_stage),
                measure = detail.stage?.let { MeasureUi(it, unit = "") },
                placeholder = stringResource(R.string.map_detail_unknown_stage),
                modifier = Modifier.weight(1f).padding(start = TreesSpacing.m),
            )
        }
        TreesDivider()
    }
}

@Composable
private fun StatSeparator() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight().width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/** A figure in large serif with a small unit, or a muted placeholder when the dataset has no value. */
@Composable
private fun Stat(
    label: String,
    measure: MeasureUi?,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.xs),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            measure == null -> {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            measure.unit.isEmpty() -> {
                Text(measure.value, style = MaterialTheme.typography.titleLarge)
            }

            else -> {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(measure.value, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = measure.unit,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TreeDetailContentPreview() {
    TreesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            TreeDetailContent(
                detail =
                    TreeDetailUi(
                        id = 1,
                        name = "Platane",
                        scientificName = "Platanus x hispanica",
                        area = "Paris 19e",
                        address = "Quai de la Seine",
                        height = MeasureUi("16", "m"),
                        circumference = MeasureUi("165", "cm"),
                        stage = "Adulte",
                        remarkable = true,
                        isFavorite = false,
                    ),
                onFavoriteToggle = {},
            )
        }
    }
}
