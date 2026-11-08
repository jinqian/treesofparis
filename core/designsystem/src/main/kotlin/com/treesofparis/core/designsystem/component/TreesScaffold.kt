package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.treesofparis.core.designsystem.R
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/**
 * Screen frame with a large serif title (and optional [subtitle]) instead of a top app bar. A secondary screen passes
 * [onBack] to get a back button above its title. The app's outer scaffold (bottom bar) already handles the bottom
 * insets, so this scaffold adds none of its own; the header handles the status bar.
 */
@Composable
fun TreesScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { ScreenHeader(title = title, subtitle = subtitle, onBack = onBack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content,
    )
}

/** The large title at the top of a screen, with a back button when [onBack] is given. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = TreesSpacing.l,
                    end = TreesSpacing.l,
                    top = if (onBack != null) TreesSpacing.s else TreesSpacing.l,
                    bottom = 20.dp,
                ),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.xs),
    ) {
        if (onBack != null) {
            // Aligned with the title's left edge: the 48 dp touch target overhangs into the margin.
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(TreesIcons.Back, contentDescription = stringResource(R.string.designsystem_back))
            }
        }
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
