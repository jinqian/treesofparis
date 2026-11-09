package com.treesofparis.feature.map

import com.treesofparis.core.map.MapCamera
import com.treesofparis.domain.model.GeoPoint
import kotlin.math.cos
import kotlin.math.log2

/** Street level: close enough to tell the trees of a pavement apart. */
internal const val NEARBY_MAX_ZOOM = 19.5

/** Metres covered by one dp at zoom 0 on the equator (a 256 dp world). */
private const val METRES_PER_DP_AT_ZOOM_0 = 156_543.03

/** The user and the tree fit in this many dp, clear of the filters above and the card below. */
private const val FIT_SPAN_DP = 200.0

/**
 * Frames the user and a nearby [tree]: their midpoint, at the closest zoom that shows both (street level at most). When
 * showing both would need a zoom where trees are hidden, centres on the tree at [MapViewModel.MIN_TREE_ZOOM] instead.
 */
internal fun nearbyCamera(
    from: GeoPoint,
    tree: GeoPoint,
): MapCamera {
    val metresPerDpAtZoom0 = METRES_PER_DP_AT_ZOOM_0 * cos(Math.toRadians(tree.lat))
    val fitZoom = log2(metresPerDpAtZoom0 * FIT_SPAN_DP / from.distanceTo(tree))
    val zoom = fitZoom.coerceAtMost(NEARBY_MAX_ZOOM)
    if (zoom < MapViewModel.MIN_TREE_ZOOM) return MapCamera(tree, MapViewModel.MIN_TREE_ZOOM)
    return MapCamera(GeoPoint((from.lat + tree.lat) / 2, (from.lon + tree.lon) / 2), zoom)
}
