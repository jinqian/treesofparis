package fr.arbresdeparis.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

/**
 * A tree row: a leaf badge (gold for a [remarkable] tree), the serif [title] and a [subtitle] on one line unless
 * [subtitleMaxLines] allows more. [trailing] holds an optional action, e.g. a favorite toggle. A null [onClick] leaves
 * the row inert. Meant to sit inside a [TreesCardGroup].
 */
@Composable
fun TreeListItem(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    remarkable: Boolean = false,
    subtitleMaxLines: Int = 1,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(end = TreesSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(horizontal = TreesSpacing.m, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeafBadge(remarkable = remarkable)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

/** A round leaf badge: sage for any tree, gold for a remarkable one. Decorative. */
@Composable
fun LeafBadge(
    remarkable: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            modifier
                .size(44.dp)
                .background(if (remarkable) colors.tertiaryContainer else colors.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TreesIcons.Leaf,
            contentDescription = null,
            tint = if (remarkable) colors.tertiary else colors.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun TreeListItemPreview() {
    TreesTheme {
        TreesCardGroup {
            TreeListItem(title = "Platane", subtitle = "Paris 19e · Quai de la Seine", onClick = {}, remarkable = true)
            TreesDivider()
            TreeListItem(title = "Marronnier", subtitle = "Paris 6e · Jardin du Luxembourg", onClick = {})
        }
    }
}
