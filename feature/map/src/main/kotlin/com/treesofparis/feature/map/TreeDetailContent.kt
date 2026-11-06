package com.treesofparis.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/** The body of the tree detail bottom sheet. */
@Composable
fun TreeDetailContent(
    detail: TreeDetailUi,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = TreesSpacing.l)
                .padding(bottom = TreesSpacing.l)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detail.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            FavoriteButton(isFavorite = detail.isFavorite, onClick = onFavoriteToggle)
        }
        Text(
            text = detail.scientificName ?: stringResource(R.string.map_detail_unknown_species),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(detail.address, style = MaterialTheme.typography.bodyMedium)
        Text(detail.area, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        if (detail.remarkable) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.map_detail_remarkable)) },
                leadingIcon = { Icon(TreesIcons.Remarkable, contentDescription = null) },
            )
        }
        DetailRow(
            stringResource(R.string.map_detail_height),
            detail.height ?: stringResource(R.string.map_detail_unknown_height),
        )
        DetailRow(
            stringResource(R.string.map_detail_circumference),
            detail.circumference ?: stringResource(R.string.map_detail_unknown_circumference),
        )
        DetailRow(
            stringResource(R.string.map_detail_stage),
            detail.stage ?: stringResource(R.string.map_detail_unknown_stage),
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) TreesIcons.Favorite else TreesIcons.FavoriteOutlined,
            contentDescription =
                stringResource(
                    if (isFavorite) R.string.map_favorite_remove else R.string.map_favorite_add,
                ),
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@PreviewLightDark
@Composable
private fun TreeDetailContentPreview() {
    TreesTheme {
        TreeDetailContent(
            detail =
                TreeDetailUi(
                    id = 1,
                    name = "Platane",
                    scientificName = "Platanus x hispanica",
                    area = "Paris 19e",
                    address = "Quai de la Seine",
                    height = "16 m",
                    circumference = "165 cm",
                    stage = "Adulte",
                    remarkable = true,
                    isFavorite = false,
                ),
            onFavoriteToggle = {},
        )
    }
}
