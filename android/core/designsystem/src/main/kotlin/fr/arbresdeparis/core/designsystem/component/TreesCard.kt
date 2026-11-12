package fr.arbresdeparis.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

/**
 * A light card with a hairline border that groups rows; separate rows with [TreesDivider]. Pass no [contentPadding]
 * when the first or last row has its own background (e.g. a coloured header) so it reaches the rounded edge.
 */
@Composable
fun TreesCardGroup(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = TreesSpacing.xs),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** The 1 dp hairline between rows of a [TreesCardGroup]. */
@Composable
fun TreesDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

/** What an [InfoRow] shows at its end: nothing, a chevron (opens a screen) or an arrow (opens a web page). */
enum class InfoRowTrailing { NONE, CHEVRON, EXTERNAL }

/**
 * A one- or two-line row of information, clickable when [onClick] is given. A [leadingIcon] sits in a sage circle
 * before the text (e.g. « Nous écrire »).
 */
@Composable
fun InfoRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: InfoRowTrailing = InfoRowTrailing.NONE,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(horizontal = TreesSpacing.m, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m),
    ) {
        if (leadingIcon != null) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val icon =
            when (trailing) {
                InfoRowTrailing.NONE -> null
                InfoRowTrailing.CHEVRON -> TreesIcons.ChevronRight
                InfoRowTrailing.EXTERNAL -> TreesIcons.OpenExternal
            }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * A row with a switch at its end, for a setting. The whole row toggles it, and screen readers read it as one switch
 * with its [title] and [subtitle].
 */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = TreesSpacing.m, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // The row handles the toggle; the switch only shows the state.
        Switch(checked = checked, onCheckedChange = null)
    }
}

@PreviewLightDark
@Composable
private fun TreesCardGroupPreview() {
    TreesTheme {
        TreesCardGroup {
            InfoRow(title = "« Les arbres », Paris Data", subtitle = "Ville de Paris")
            TreesDivider()
            InfoRow(title = "Licence ODbL", trailing = InfoRowTrailing.EXTERNAL, onClick = {})
            TreesDivider()
            SwitchRow(title = "Envoyer les rapports de plantage", checked = true, onCheckedChange = {})
        }
    }
}
