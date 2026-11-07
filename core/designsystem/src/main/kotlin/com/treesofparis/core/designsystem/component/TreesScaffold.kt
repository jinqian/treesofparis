package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/**
 * Screen frame with a large serif title (and optional [subtitle]) instead of a top app bar. The app's outer
 * scaffold (bottom bar) already handles the bottom insets, so this scaffold adds none of its own; the header handles
 * the status bar.
 */
@Composable
fun TreesScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { ScreenHeader(title = title, subtitle = subtitle) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content,
    )
}

/** The large title at the top of a tab. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = TreesSpacing.l, end = TreesSpacing.l, top = TreesSpacing.l, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.xs),
    ) {
        Text(title, style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TreesScaffoldPreview() {
    TreesTheme {
        TreesScaffold(title = "Favoris", subtitle = "2 arbres") { padding ->
            EmptyState(message = "Aucun favori pour le moment", modifier = Modifier.padding(padding))
        }
    }
}
