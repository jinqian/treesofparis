package fr.arbresdeparis.core.map.osmdroid

import android.graphics.Paint
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MarkerStyle
import fr.arbresdeparis.domain.model.GeoPoint
import org.osmdroid.api.IGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

/** The marker id for an overlay point index, or null if the index does not match a marker. */
internal fun markerIdAt(
    index: Int?,
    markers: List<MapMarker>,
): Long? = index?.let { markers.getOrNull(it)?.id }

internal fun selectedIndex(markers: List<MapMarker>): Int? = markers.indexOfFirst { it.selected }.takeIf { it >= 0 }

/** Marker sizes in pixels for a screen [density], from sizes defined in dp so pins look the same on every screen. */
internal data class MarkerSizes(
    val treeRadiusPx: Float,
    val selectedRadiusPx: Float,
    val userRadiusPx: Float,
    val remarkableRadiusPx: Float,
    val ringWidthPx: Float,
    val cellSizePx: Int,
) {
    /** Radius of the ring drawn under a pin of [radiusPx]. */
    fun ringRadius(radiusPx: Float): Float = radiusPx + ringWidthPx
}

private const val TREE_RADIUS_DP = 6f
private const val SELECTED_RADIUS_DP = 9f
private const val USER_RADIUS_DP = 8f
private const val REMARKABLE_RADIUS_DP = 8f
private const val RING_WIDTH_DP = 2f
private const val CELL_SIZE_DP = 4f

internal fun markerSizes(density: Float) =
    MarkerSizes(
        treeRadiusPx = TREE_RADIUS_DP * density,
        selectedRadiusPx = SELECTED_RADIUS_DP * density,
        userRadiusPx = USER_RADIUS_DP * density,
        remarkableRadiusPx = REMARKABLE_RADIUS_DP * density,
        ringWidthPx = RING_WIDTH_DP * density,
        cellSizePx = (CELL_SIZE_DP * density).toInt().coerceAtLeast(1),
    )

private fun fill(color: Int) =
    Paint().apply {
        style = Paint.Style.FILL
        this.color = color
        isAntiAlias = true
    }

/** Markers grouped by style, in drawing order ([MarkerStyle] order), without empty groups. */
internal fun markerLayers(markers: List<MapMarker>): Map<MarkerStyle, List<MapMarker>> {
    val byStyle = markers.groupBy { it.style }
    return MarkerStyle.entries.mapNotNull { style -> byStyle[style]?.let { style to it } }.toMap()
}

/** One fast overlay per marker style: thousands of points without one view object each. */
internal fun treeOverlay(
    markers: List<MapMarker>,
    color: Int,
    radiusPx: Float,
    selectedColor: Int,
    sizes: MarkerSizes,
    onMarkerClick: (Long) -> Unit,
): SimpleFastPointOverlay {
    val points: List<IGeoPoint> = markers.map { it.position.toOsm() }
    val options =
        SimpleFastPointOverlayOptions
            .getDefaultStyle()
            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
            .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
            .setRadius(radiusPx)
            .setCellSize(sizes.cellSizePx)
            .setIsClickable(true)
            .setPointStyle(fill(color))
            .setSelectedPointStyle(fill(selectedColor))
            .setSelectedRadius(sizes.selectedRadiusPx)
    return SimpleFastPointOverlay(SimplePointTheme(points, false), options).apply {
        setSelectedPoint(selectedIndex(markers))
        setOnClickListener { _, index -> markerIdAt(index, markers)?.let(onMarkerClick) }
    }
}

/** Non-clickable dots of one [color] and [radiusPx]: pin rings, the selection halo, the user's position. */
internal fun dotsOverlay(
    points: List<GeoPoint>,
    color: Int,
    radiusPx: Float,
    sizes: MarkerSizes,
): SimpleFastPointOverlay {
    val options =
        SimpleFastPointOverlayOptions
            .getDefaultStyle()
            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
            .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
            .setRadius(radiusPx)
            .setCellSize(sizes.cellSizePx)
            .setIsClickable(false)
            .setPointStyle(fill(color))
    return SimpleFastPointOverlay(SimplePointTheme(points.map { it.toOsm() }, false), options)
}
