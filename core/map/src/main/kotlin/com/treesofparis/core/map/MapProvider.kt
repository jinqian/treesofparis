package com.treesofparis.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint

/**
 * A map implementation (osmdroid today). Implementations live in their own module (`:core:map-<provider>`); the app
 * provides one through [LocalMapProvider]. Features only use [TreeMapView].
 *
 * Camera contract: [initialCamera] is applied once when the map is created; [cameraRequest] is applied once per
 * distinct id; user gestures are only reported through [onCameraChange] (with the visible bounds) and never fed
 * back as input.
 */
interface MapProvider {
    @Composable
    @Suppress("LongParameterList") // the contract is the parameter list
    fun Content(
        initialCamera: MapCamera,
        cameraRequest: CameraRequest?,
        markers: List<MapMarker>,
        userLocation: GeoPoint?,
        config: MapConfig,
        onMarkerClick: (Long) -> Unit,
        onCameraChange: (MapCamera, GeoBounds) -> Unit,
        modifier: Modifier,
    )
}

val LocalMapProvider: ProvidableCompositionLocal<MapProvider> =
    staticCompositionLocalOf {
        error("No MapProvider provided: :app must provide LocalMapProvider (see TreesApp).")
    }
