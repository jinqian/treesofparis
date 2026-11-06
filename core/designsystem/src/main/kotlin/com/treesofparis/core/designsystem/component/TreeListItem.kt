package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.treesofparis.core.designsystem.theme.TreesTheme

/** A two-line list row for a tree. [trailing] holds an optional action, e.g. a favorite toggle. */
@Composable
fun TreeListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = trailing,
        modifier = modifier.clickable(onClick = onClick),
    )
}

@PreviewLightDark
@Composable
private fun TreeListItemPreview() {
    TreesTheme { TreeListItem(title = "Platane commun", subtitle = "Quai de la Seine · Paris 19e", onClick = {}) }
}
