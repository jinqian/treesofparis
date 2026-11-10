package fr.arbresdeparis.core.designsystem.leaf

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

private const val VIEWPORT = 100f

/**
 * Draws [leaf] to fill the composable's size. The line keeps [strokeWidth] at every size, so the drawing stays crisp
 * as a 150 dp watermark and readable as a 32 dp badge. Decorative: callers describe the tree in text.
 */
@Composable
fun LeafDrawing(
    leaf: TreeLeaf,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 1.2.dp,
) {
    val path = remember(leaf) { PathParser().parsePathString(leaf.pathData).toPath() }
    Canvas(modifier = modifier) {
        val scale = minOf(size.width, size.height) / VIEWPORT
        val dx = (size.width - VIEWPORT * scale) / 2
        val dy = (size.height - VIEWPORT * scale) / 2
        withTransform({
            translate(dx, dy)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawPath(
                path = path,
                color = color,
                style =
                    Stroke(
                        width = strokeWidth.toPx() / scale,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LeafDrawingPreview() {
    TreesTheme {
        FlowRow(
            modifier = Modifier.padding(TreesSpacing.m),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.m),
        ) {
            TreeLeaf.entries.forEach { LeafDrawing(it, Modifier.size(64.dp)) }
        }
    }
}
