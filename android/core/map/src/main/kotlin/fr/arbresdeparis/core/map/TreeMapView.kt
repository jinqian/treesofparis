package fr.arbresdeparis.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint

/** The map composable features use. Delegates to the [MapProvider] supplied by the app. */
@Composable
@Suppress("LongParameterList") // mirrors MapProvider.Content
fun TreeMapView(
    initialCamera: MapCamera,
    cameraRequest: CameraRequest?,
    markers: List<MapMarker>,
    userLocation: GeoPoint?,
    config: MapConfig,
    palette: MapPalette,
    onMarkerClick: (Long) -> Unit,
    onCameraChange: (MapCamera, GeoBounds) -> Unit,
    modifier: Modifier = Modifier,
    clusters: List<MapCluster> = emptyList(),
    onClusterClick: (MapCluster) -> Unit = {},
) {
    LocalMapProvider.current.Content(
        initialCamera = initialCamera,
        cameraRequest = cameraRequest,
        markers = markers,
        clusters = clusters,
        userLocation = userLocation,
        config = config,
        palette = palette,
        onMarkerClick = onMarkerClick,
        onClusterClick = onClusterClick,
        onCameraChange = onCameraChange,
        modifier = modifier,
    )
}
