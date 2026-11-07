package com.treesofparis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.icon.TreesIllustrations
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme

/**
 * Centered message for a screen with nothing to show: an optional [illustration], a serif [title], the [message],
 * and an optional call to action ([actionLabel] + [onAction]).
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    illustration: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = TreesSpacing.xl, vertical = TreesSpacing.l),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration?.invoke()
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(TreesSpacing.s))
            Button(onClick = onAction, modifier = Modifier.height(48.dp)) {
                Icon(TreesIcons.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(TreesSpacing.s))
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** The botanical sprig drawn above empty states. Decorative: no content description. */
@Composable
fun SprigIllustration(modifier: Modifier = Modifier) {
    Icon(
        imageVector = TreesIllustrations.Sprig,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = modifier.size(width = 140.dp, height = 160.dp),
    )
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    TreesTheme {
        EmptyState(
            title = "Aucun favori pour le moment",
            message = "Touchez un arbre sur la carte, puis le cœur, pour le retrouver ici.",
            illustration = { SprigIllustration() },
            actionLabel = "Explorer la carte",
            onAction = {},
        )
    }
}
