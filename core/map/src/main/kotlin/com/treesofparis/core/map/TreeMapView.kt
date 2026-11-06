package com.treesofparis.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint

/** The map composable features use. Delegates to the [MapProvider] supplied by the app. */
@Composable
@Suppress("LongParameterList") // mirrors MapProvider.Content
fun TreeMapView(
    initialCamera: MapCamera,
    cameraRequest: CameraRequest?,
    markers: List<MapMarker>,
    userLocation: GeoPoint?,
    config: MapConfig,
    onMarkerClick: (Long) -> Unit,
    onCameraChange: (MapCamera, GeoBounds) -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalMapProvider.current.Content(
        initialCamera = initialCamera,
        cameraRequest = cameraRequest,
        markers = markers,
        userLocation = userLocation,
        config = config,
        onMarkerClick = onMarkerClick,
        onCameraChange = onCameraChange,
        modifier = modifier,
    )
}
