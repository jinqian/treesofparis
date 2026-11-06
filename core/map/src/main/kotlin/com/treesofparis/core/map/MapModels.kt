package com.treesofparis.core.map

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint

/** Where the map looks: centre and zoom level (OSM zoom scale, 0 = whole world). */
data class MapCamera(
    val center: GeoPoint,
    val zoom: Double,
)

/**
 * A one-off programmatic camera move (e.g. "centre on me"). The provider applies each distinct [id] once, so the
 * same request surviving recomposition does not move the map again.
 */
data class CameraRequest(
    val id: Long,
    val camera: MapCamera,
)

/** A point on the map. Providers decide how to draw it; [selected] markers are highlighted. */
data class MapMarker(
    val id: Long,
    val position: GeoPoint,
    val selected: Boolean = false,
    val style: MarkerStyle = MarkerStyle.TREE,
)

/** Kinds of markers, in drawing order: later styles are drawn on top. Providers decide how each one looks. */
enum class MarkerStyle { TREE, REMARKABLE }

/** Limits the user cannot leave: panning stays inside [bounds], zoom stays in [minZoom]..[maxZoom]. */
data class MapConfig(
    val bounds: GeoBounds,
    val minZoom: Double,
    val maxZoom: Double,
)
