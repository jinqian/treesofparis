package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.treesofparis.core.designsystem.R
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/** Centered error message, with a « Réessayer » button when [onRetry] is given. */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(TreesSpacing.l),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = TreesIcons.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Button(onClick = onRetry) { Text(stringResource(R.string.designsystem_retry)) }
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    TreesTheme { ErrorState(message = "Impossible de charger les arbres.", onRetry = {}) }
}
