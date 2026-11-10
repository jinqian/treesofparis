package fr.arbresdeparis.core.map

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint

/**
 * Where the map looks: centre and zoom level. The zoom is the usual web-map zoom on a 256 dp world (0 = the whole
 * world in 256 dp, each level doubles): providers convert it to their own scale (MapLibre draws 512 dp tiles).
 */
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

/**
 * A group of [count] trees drawn as one disc with [label] (the count, formatted by the feature) when the map shows
 * too many trees to draw each one. [id] is unique among the clusters on screen.
 */
data class MapCluster(
    val id: Long,
    val position: GeoPoint,
    val count: Int,
    val label: String,
)

/** Kinds of markers, in drawing order: later styles are drawn on top. Providers decide how each one looks. */
enum class MarkerStyle { TREE, REMARKABLE }

/** Limits the user cannot leave: panning stays inside [bounds], zoom stays in [minZoom]..[maxZoom]. */
data class MapConfig(
    val bounds: GeoBounds,
    val minZoom: Double,
    val maxZoom: Double,
)

/**
 * Colours of what the map draws itself, chosen by the feature from its theme. Every pin gets a [ring] so it reads on
 * any tile; [selected] highlights the tree whose sheet is open.
 */
@Immutable
data class MapPalette(
    val tree: Color,
    val remarkable: Color,
    val selected: Color,
    val ring: Color,
    val userLocation: Color,
    /** The disc of a [MapCluster]... */
    val treeGroup: Color,
    /** ...and its count. */
    val treeGroupLabel: Color,
)
