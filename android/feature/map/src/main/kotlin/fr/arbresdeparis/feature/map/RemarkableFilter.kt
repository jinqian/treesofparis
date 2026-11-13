package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

internal const val LEGEND_DOT_TAG = "legend_dot"

/** One dot size for every legend, so ordinary and remarkable trees read as equals in the key. */
private val LEGEND_DOT_SIZE = 12.dp

/** A pin as drawn on the map: a coloured dot with a paper ring. Decorative. */
@Composable
internal fun LegendDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .testTag(LEGEND_DOT_TAG)
                .size(LEGEND_DOT_SIZE + 4.dp)
                .background(TreesTheme.colors.pinRing, CircleShape)
                .padding(2.dp)
                .background(color, CircleShape),
    )
}

/** The bottom sheet body explaining remarkable trees, with the legend and a shortcut to the filter. */
@Composable
internal fun RemarkableInfoContent(
    onShowRemarkableOnly: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = TreesSpacing.l)
                .padding(top = TreesSpacing.xs, bottom = TreesSpacing.l)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(R.string.map_remarkable_info_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.map_remarkable_info_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            TreesDivider()
            LegendRow(stringResource(R.string.map_legend_tree), TreesTheme.colors.treePin)
            TreesDivider()
            LegendRow(stringResource(R.string.map_detail_remarkable), TreesTheme.colors.remarkable)
            TreesDivider()
        }
        Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
            Button(onClick = onShowRemarkableOnly, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.map_remarkable_info_show), style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.map_remarkable_info_close), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) { LegendDot(color) }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Small note under the filters while only remarkable trees are shown, ending with « Qu’est-ce que c’est ? »: the whole
 * note opens the explanation of remarkable trees (it replaces the « ? » button).
 */
@Composable
internal fun RemarkableOnlyCaption(
    onWhatIsIt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val caption = stringResource(R.string.map_filter_remarkable_caption)
    val link = stringResource(R.string.map_filter_remarkable_what)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLow.copy(alpha = CAPTION_ALPHA),
        shadowElevation = 1.dp,
    ) {
        Text(
            text =
                buildAnnotatedString {
                    append(caption)
                    append(" · ")
                    withStyle(
                        SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline),
                    ) { append(link) }
                },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier =
                Modifier
                    .clickable(role = Role.Button, onClick = onWhatIsIt)
                    .heightIn(min = 48.dp)
                    .wrapContentHeight()
                    .padding(horizontal = 14.dp, vertical = TreesSpacing.s),
        )
    }
}

private const val CAPTION_ALPHA = 0.94f

@PreviewLightDark
@Composable
private fun RemarkableInfoContentPreview() {
    TreesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            RemarkableInfoContent(onShowRemarkableOnly = {}, onClose = {})
        }
    }
}
