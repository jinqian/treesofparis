package com.treesofparis.feature.map

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
    val message: MapMessage? = null,
)

/** One-off messages shown in a snackbar. */
enum class MapMessage { OUT_OF_AREA, LOCATION_UNAVAILABLE, PERMISSION_DENIED }
