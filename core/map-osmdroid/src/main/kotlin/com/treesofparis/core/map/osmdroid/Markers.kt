package com.treesofparis.core.map.osmdroid

import android.graphics.Paint
import com.treesofparis.core.map.MapMarker
import com.treesofparis.domain.model.GeoPoint
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

private const val TREE_RADIUS_PX = 9f
private const val USER_RADIUS_PX = 12f
private const val CELL_SIZE_PX = 12

private fun fill(color: Int) =
    Paint().apply {
        style = Paint.Style.FILL
        this.color = color
        isAntiAlias = true
    }

/** One fast overlay for all tree markers: thousands of points without one view object each. */
internal fun treeOverlay(
    markers: List<MapMarker>,
    color: Int,
    selectedColor: Int,
    onMarkerClick: (Long) -> Unit,
): SimpleFastPointOverlay {
    val points: List<IGeoPoint> = markers.map { it.position.toOsm() }
    val options =
        SimpleFastPointOverlayOptions
            .getDefaultStyle()
            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
            .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
            .setRadius(TREE_RADIUS_PX)
            .setCellSize(CELL_SIZE_PX)
            .setIsClickable(true)
            .setPointStyle(fill(color))
            .setSelectedPointStyle(fill(selectedColor))
            .setSelectedRadius(TREE_RADIUS_PX * 1.6f)
    return SimpleFastPointOverlay(SimplePointTheme(points, false), options).apply {
        setSelectedPoint(selectedIndex(markers))
        setOnClickListener { _, index -> markerIdAt(index, markers)?.let(onMarkerClick) }
    }
}

/** A single non-clickable dot for the user's position. */
internal fun userLocationOverlay(
    location: GeoPoint,
    color: Int,
): SimpleFastPointOverlay {
    val options =
        SimpleFastPointOverlayOptions
            .getDefaultStyle()
            .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
            .setRadius(USER_RADIUS_PX)
            .setIsClickable(false)
            .setPointStyle(fill(color))
    return SimpleFastPointOverlay(SimplePointTheme(listOf<IGeoPoint>(location.toOsm()), false), options)
}
