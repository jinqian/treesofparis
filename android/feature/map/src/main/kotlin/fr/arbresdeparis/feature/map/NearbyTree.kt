package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.ScientificNameStyle
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.GeoPoint

/** « Arbre le plus proche »: a moss pill above « Me localiser »; shows a spinner while it locates the user. */
@Composable
internal fun NearestTreeButton(
    isSearching: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.map_nearest_find)
    ExtendedFloatingActionButton(
        onClick = { if (!isSearching) onClick() },
        modifier = modifier.height(48.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 6.dp),
    ) {
        if (isSearching) {
            val description = stringResource(R.string.map_nearest_searching)
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp).semantics { contentDescription = description },
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(TreesIcons.Leaf, contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Text(label, modifier = Modifier.padding(start = TreesSpacing.s), style = MaterialTheme.typography.titleSmall)
    }
}

/** The nearby tree on show: its leaf, rank and distance, names, then « Voir la fiche » and « Suivant ». */
@Composable
internal fun NearbyTreeCard(
    nearby: NearbyTreesUi,
    onOpen: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NearbyTreeHeader(nearby, onDismiss)
            NearbyTreeActions(onOpen, onNext)
        }
    }
}

@Composable
private fun NearbyTreeHeader(
    nearby: NearbyTreesUi,
    onDismiss: () -> Unit,
) {
    val tree = nearby.current
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.padding(top = TreesSpacing.m),
            shape = RoundedCornerShape(16.dp),
            color = colors.primaryContainer,
        ) {
            LeafDrawing(tree.leaf, Modifier.size(52.dp).padding(8.dp), strokeWidth = 1.3.dp)
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 14.dp, top = TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = rankLabel(nearby.rank, tree.distance),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
            )
            Text(
                text = tree.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            tree.scientificName?.let {
                Text(
                    text = it,
                    style = ScientificNameStyle.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
            Icon(
                TreesIcons.Close,
                contentDescription = stringResource(R.string.map_nearest_close),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun NearbyTreeActions(
    onOpen: () -> Unit,
    onNext: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        Button(onClick = onOpen, modifier = Modifier.weight(1f).height(44.dp)) {
            Text(stringResource(R.string.map_nearest_open), style = MaterialTheme.typography.titleSmall)
        }
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.weight(1f).height(44.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Text(stringResource(R.string.map_nearest_next), style = MaterialTheme.typography.titleSmall)
            Icon(
                TreesIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp).size(16.dp),
            )
        }
    }
}

/** « Le plus proche · à 5 m », then « 2ᵉ plus proche · à 7 m ». */
@Composable
private fun rankLabel(
    rank: Int,
    distance: String,
): String =
    if (rank == 1) {
        stringResource(R.string.map_nearest_first, distance)
    } else {
        stringResource(R.string.map_nearest_rank, rank, distance)
    }

@PreviewLightDark
@Composable
private fun NearbyTreeCardPreview() {
    TreesTheme {
        NearbyTreeCard(
            nearby =
                NearbyTreesUi(
                    from = GeoPoint.PARIS_CENTER,
                    trees =
                        listOf(
                            NearbyTreeUi(
                                id = 1,
                                name = "Platane commun",
                                scientificName = "Platanus x hispanica",
                                distance = "5 m",
                                location = GeoPoint.PARIS_CENTER,
                                leaf = TreeLeaf.forGenus("Platanus"),
                            ),
                        ),
                ),
            onOpen = {},
            onNext = {},
            onDismiss = {},
            modifier = Modifier.fillMaxWidth().padding(TreesSpacing.m),
        )
    }
}

@PreviewLightDark
@Composable
private fun NearestTreeButtonPreview() {
    TreesTheme { NearestTreeButton(isSearching = false, onClick = {}, modifier = Modifier.padding(TreesSpacing.m)) }
}
