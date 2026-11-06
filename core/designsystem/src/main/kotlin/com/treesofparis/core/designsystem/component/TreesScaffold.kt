package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.treesofparis.core.designsystem.theme.TreesTheme

/**
 * Screen frame with a top app bar. The app's outer scaffold (bottom bar) already handles the bottom insets, so
 * this scaffold adds none of its own; the top bar handles the status bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreesScaffold(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(title) }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun TreesScaffoldPreview() {
    TreesTheme {
        TreesScaffold(title = "Favoris") { padding ->
            EmptyState(message = "Aucun favori pour le moment", modifier = Modifier.padding(padding))
        }
    }
}
