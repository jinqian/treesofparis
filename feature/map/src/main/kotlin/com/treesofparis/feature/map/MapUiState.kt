package com.treesofparis.feature.map

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.core.map.CameraRequest
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MapMarker
import com.treesofparis.domain.model.GeoPoint

data class MapUiState(
    val initialCamera: MapCamera,
    val cameraRequest: CameraRequest? = null,
    val markers: List<MapMarker> = emptyList(),
    val userLocation: GeoPoint? = null,
    val showZoomHint: Boolean = false,
    val selectedTree: TreeDetailUi? = null,
    val isLocating: Boolean = false,
    val remarkableOnly: Boolean = false,
    val showRemarkableInfo: Boolean = false,
    /** Only this genus is shown (opened from the Herbier tab), or null. */
    val genusFilter: GenusFilterUi? = null,
    val message: MapMessage? = null,
    /** The trees closest to the user after « Arbre le plus proche », or null. */
    val nearby: NearbyTreesUi? = null,
    val isFindingNearest: Boolean = false,
)

/** The trees closest to the user ([from]), closest first, shown one at a time: « Suivant » moves to the next. */
data class NearbyTreesUi(
    val from: GeoPoint,
    val trees: List<NearbyTreeUi>,
    val index: Int = 0,
) {
    val current: NearbyTreeUi get() = trees[index]

    /** 1 for the closest tree. */
    val rank: Int get() = index + 1
}

/** One of the closest trees: its names, its distance already formatted (« 5 m », « 1,2 km »), its leaf. */
data class NearbyTreeUi(
    val id: Long,
    val name: String,
    val scientificName: String?,
    val distance: String,
    val location: GeoPoint,
    val leaf: TreeLeaf,
)

/** A genus filter: the dataset [genus] and its drawing, whose label names it in French. */
data class GenusFilterUi(
    val genus: String,
    val leaf: TreeLeaf,
)

/** One-off messages shown in a snackbar. */
enum class MapMessage { OUT_OF_AREA, LOCATION_UNAVAILABLE, PERMISSION_DENIED, NO_TREE_NEARBY }
