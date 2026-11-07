package com.treesofparis.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Every icon the app uses: thin, rounded line icons on a 24 dp grid, drawn for the « Herbier » look. Features never
 * import an icon library directly. Icons are drawn in black and tinted by `Icon`.
 */
object TreesIcons {
    val Map: ImageVector = line("Map", "M9 4 3 6v14l6-2 6 2 6-2V4l-6 2-6-2z", "M9 4v14M15 6v14")
    val MapOutlined: ImageVector = Map
    val FavoriteOutlined: ImageVector = line("FavoriteOutlined", HEART)
    val Favorite: ImageVector = filled("Favorite", HEART)
    val About: ImageVector = line("About", circle(12f, 12f, 9f), "M12 11v5M12 8h.01")
    val Help: ImageVector =
        line("Help", circle(12f, 12f, 9f), "M9.6 9.4a2.5 2.5 0 0 1 4.8 1c0 1.6-2.4 2-2.4 3.4M12 17h.01")
    val AboutOutlined: ImageVector = About
    val MyLocation: ImageVector = line("MyLocation", circle(12f, 12f, 4f), "M12 2v3M12 19v3M2 12h3M19 12h3")
    val Remarkable: ImageVector = filled("Remarkable", STAR)
    val RemarkableOutlined: ImageVector = line("RemarkableOutlined", STAR)
    val Leaf: ImageVector = line("Leaf", "M5 19c0-8 6-14 15-14 0 9-6 15-14 15", "M5 19 13 11")
    val Place: ImageVector = line("Place", "M12 21s-6-5.5-6-11a6 6 0 0 1 12 0c0 5.5-6 11-6 11z", circle(12f, 10f, 2f))
    val ChevronRight: ImageVector = line("ChevronRight", "M9 6l6 6-6 6")
    val OpenExternal: ImageVector = line("OpenExternal", "M7 17 17 7M9 7h8v8")
    val Error: ImageVector = line("Error", "M12 4 2.5 20h19z", "M12 10v4M12 17h.01")
}

private const val HEART = "M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z"
private const val STAR = "M12 3l2.6 6 6.4.5-4.9 4.3 1.5 6.2L12 16.6 6.4 20l1.5-6.2L3 9.5 9.4 9z"
private const val SIZE = 24f
private const val STROKE = 1.7f

private fun circle(
    cx: Float,
    cy: Float,
    r: Float,
) = "M${cx - r} ${cy}a$r $r 0 1 0 ${r + r} 0a$r $r 0 1 0 ${-(r + r)} 0"

private fun builder(
    name: String,
    width: Float = SIZE,
    height: Float = SIZE,
) = ImageVector.Builder(
    name = name,
    defaultWidth = width.dp,
    defaultHeight = height.dp,
    viewportWidth = width,
    viewportHeight = height,
)

private fun line(
    name: String,
    vararg paths: String,
): ImageVector = lineDrawing(name, SIZE, SIZE, STROKE, *paths)

/** A drawing made of rounded strokes on a [width]×[height] viewport (icons and illustrations). */
internal fun lineDrawing(
    name: String,
    width: Float,
    height: Float,
    strokeWidth: Float,
    vararg paths: String,
): ImageVector =
    builder(name, width, height)
        .apply {
            paths.forEach { path ->
                addPath(
                    pathData = addPathNodes(path),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = strokeWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

private fun filled(
    name: String,
    path: String,
): ImageVector =
    builder(name)
        .apply {
            addPath(
                pathData = addPathNodes(path),
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = STROKE,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
