package fr.arbresdeparis.feature.map

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.domain.model.GeoPoint

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
    /** Only this genus is shown (chosen in the « Essences » sheet or opened from Herbier), or null. */
    val genusFilter: GenusFilterUi? = null,
    /** The open « Essences » sheet, or null. */
    val genusPicker: GenusPickerUi? = null,
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

/**
 * A genus filter: the dataset [genus] and its drawing. A drawn genus is named by its drawing's label, another one by
 * [commonName], the French name most of its trees carry (null until it is known).
 */
data class GenusFilterUi(
    val genus: String,
    val leaf: TreeLeaf,
    val commonName: String? = null,
)

/**
 * The « Essences » sheet: the genera with a leaf drawing as a grid, then the [others], most planted first. While only
 * remarkable trees are shown, it lists the genera that have some and counts those.
 */
data class GenusPickerUi(
    val drawn: List<GenusChoiceUi>,
    val others: List<GenusChoiceUi>,
    val remarkableOnly: Boolean,
)

/** One genus to choose: [count] trees (or remarkable trees), already formatted as [countLabel] (« 42 591 »). */
data class GenusChoiceUi(
    val genus: String,
    val commonName: String,
    val leaf: TreeLeaf,
    val count: Int,
    val countLabel: String,
    val selected: Boolean,
)

/** One-off messages shown in a snackbar. */
enum class MapMessage { OUT_OF_AREA, LOCATION_UNAVAILABLE, PERMISSION_DENIED, NO_TREE_NEARBY }
