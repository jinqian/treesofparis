package com.treesofparis.core.map.osmdroid

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.treesofparis.core.map.CameraRequest
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MapConfig
import com.treesofparis.core.map.MapMarker
import com.treesofparis.core.map.MapPalette
import com.treesofparis.core.map.MapProvider
import com.treesofparis.core.map.MarkerStyle
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import javax.inject.Inject

/** [MapProvider] backed by osmdroid and the OpenStreetMap standard tiles. */
class OsmdroidMapProvider
    @Inject
    constructor() : MapProvider {
        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            userLocation: GeoPoint?,
            config: MapConfig,
            palette: MapPalette,
            onMarkerClick: (Long) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) {
            val context = LocalContext.current
            val currentOnCameraChange by rememberUpdatedState(onCameraChange)
            val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
            val colors =
                MarkerColors(
                    tree = palette.tree.toArgb(),
                    remarkable = palette.remarkable.toArgb(),
                    selected = palette.selected.toArgb(),
                    ring = palette.ring.toArgb(),
                    user = palette.userLocation.toArgb(),
                    sizes = markerSizes(LocalDensity.current.density),
                )
            val state =
                remember {
                    OsmdroidState(
                        mapView =
                            createMapView(context, initialCamera, config) { camera, bounds ->
                                currentOnCameraChange(camera, bounds)
                            },
                    )
                }
            MapViewLifecycle(state.mapView)
            AndroidView(
                factory = { state.mapView },
                modifier = modifier,
                update = { mapView ->
                    config.applyTo(mapView)
                    if (state.cameraRequests.shouldApply(cameraRequest)) {
                        cameraRequest?.camera?.let {
                            mapView.controller.animateTo(
                                it.center.toOsm(),
                                it.zoom,
                                ANIMATION_MS,
                            )
                        }
                    }
                    state.updateTrees(markers, colors) { id -> currentOnMarkerClick(id) }
                    state.updateUserLocation(userLocation, colors)
                    mapView.invalidate()
                },
            )
        }

        private companion object {
            const val ANIMATION_MS = 600L
        }
    }

private data class MarkerColors(
    val tree: Int,
    val remarkable: Int,
    val selected: Int,
    val ring: Int,
    val user: Int,
    val sizes: MarkerSizes,
)

/** The map view and the overlays it owns, kept across recompositions. */
private class OsmdroidState(
    val mapView: MapView,
) {
    val cameraRequests = CameraRequestTracker()
    private var trees: List<SimpleFastPointOverlay> = emptyList()
    private var treesFor: List<MapMarker>? = null
    private var user: List<SimpleFastPointOverlay> = emptyList()
    private var userFor: GeoPoint? = null

    fun updateTrees(
        markers: List<MapMarker>,
        colors: MarkerColors,
        onMarkerClick: (Long) -> Unit,
    ) {
        if (markers == treesFor) return
        mapView.overlays.removeAll(trees)
        val sizes = colors.sizes
        val layers =
            markerLayers(markers).map { (style, layer) ->
                val (color, radius) =
                    when (style) {
                        MarkerStyle.TREE -> colors.tree to sizes.treeRadiusPx
                        MarkerStyle.REMARKABLE -> colors.remarkable to sizes.remarkableRadiusPx
                    }
                Triple(layer, color, radius)
            }
        // Rings first, then the selection halo, then the pins, so every pin keeps a clean outline.
        val rings =
            layers.map { (layer, _, radius) ->
                dotsOverlay(layer.map { it.position }, colors.ring, sizes.ringRadius(radius), sizes)
            }
        val halo =
            markers
                .firstOrNull { it.selected }
                ?.let { dotsOverlay(listOf(it.position), colors.ring, sizes.ringRadius(sizes.selectedRadiusPx), sizes) }
        val pins =
            layers.map { (layer, color, radius) ->
                treeOverlay(layer, color, radius, colors.selected, sizes, onMarkerClick)
            }
        trees = rings + listOfNotNull(halo) + pins
        mapView.overlays.addAll(trees)
        // Keep the user's position drawn above the trees.
        mapView.overlays.removeAll(user)
        mapView.overlays.addAll(user)
        treesFor = markers
    }

    fun updateUserLocation(
        location: GeoPoint?,
        colors: MarkerColors,
    ) {
        if (location == userFor) return
        mapView.overlays.removeAll(user)
        val sizes = colors.sizes
        user =
            location
                ?.let {
                    listOf(
                        dotsOverlay(listOf(it), colors.ring, sizes.ringRadius(sizes.userRadiusPx), sizes),
                        dotsOverlay(listOf(it), colors.user, sizes.userRadiusPx, sizes),
                    )
                }.orEmpty()
        mapView.overlays.addAll(user)
        userFor = location
    }
}

private const val CAMERA_EVENT_DELAY_MS = 150L

private fun createMapView(
    context: Context,
    initialCamera: MapCamera,
    config: MapConfig,
    onCameraChange: (MapCamera, GeoBounds) -> Unit,
): MapView {
    configureOsmdroid(context)
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        config.applyTo(this)
        controller.setZoom(initialCamera.zoom)
        controller.setCenter(initialCamera.center.toOsm())
        overlays.add(CopyrightOverlay(context))
        val report = { onCameraChange(MapCamera(mapCenter.toDomain(), zoomLevelDouble), boundingBox.toGeoBounds()) }
        addOnFirstLayoutListener { _, _, _, _, _ -> report() }
        addMapListener(
            DelayedMapListener(
                object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean = true.also { report() }

                    override fun onZoom(event: ZoomEvent?): Boolean = true.also { report() }
                },
                CAMERA_EVENT_DELAY_MS,
            ),
        )
    }
}

/** Forwards the screen lifecycle to the map view (tile loading, sensors) and releases it when it leaves. */
@Composable
private fun MapViewLifecycle(mapView: MapView) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
}
