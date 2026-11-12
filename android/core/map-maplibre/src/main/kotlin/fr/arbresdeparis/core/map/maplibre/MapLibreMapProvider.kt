package fr.arbresdeparis.core.map.maplibre

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MapConfig
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.core.map.MapProvider
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import javax.inject.Inject
import android.graphics.Color as AndroidColor

/**
 * [MapProvider] backed by MapLibre Native: OpenFreeMap vector tiles drawn in the Herbier colours (light or dark with
 * the system), trees as GPU circles. See docs/adr/0005-maplibre-vector-map.md.
 */
class MapLibreMapProvider
    @Inject
    constructor() : MapProvider {
        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            clusters: List<MapCluster>,
            userLocation: GeoPoint?,
            config: MapConfig,
            palette: MapPalette,
            onMarkerClick: (Long) -> Unit,
            onClusterClick: (MapCluster) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) {
            val context = LocalContext.current
            val dark = isSystemInDarkTheme()
            val style =
                remember(dark, palette) {
                    herbierStyle(if (dark) BasemapColors.Dark else BasemapColors.Light, palette.toPinColors())
                }
            val tapRadiusPx = TAP_RADIUS_DP * LocalDensity.current.density
            // Retained, not remembered: while another tab is shown the map is only stopped, so coming back to the
            // Carte tab shows it at once instead of rebuilding it (and flashing) from nothing. It is destroyed when
            // its screen leaves the back stack, or with the activity (keyed on the context).
            val state =
                retain(context) {
                    MapLibreState(
                        context = context,
                        initialCamera = initialCamera,
                        config = config,
                        tapRadiusPx = tapRadiusPx,
                        loadColor =
                            (if (dark) BasemapColors.Dark else BasemapColors.Light).land.let(
                                AndroidColor::parseColor,
                            ),
                        onCameraChange = onCameraChange,
                        onMarkerClick = onMarkerClick,
                        onClusterClick = onClusterClick,
                    )
                }
            // The retained map outlives this composition: always call the latest callbacks.
            SideEffect {
                state.onCameraChange = onCameraChange
                state.onMarkerClick = onMarkerClick
                state.onClusterClick = onClusterClick
            }
            MapViewLifecycle(state.mapView)
            Box(modifier = modifier) {
                AndroidView(
                    factory = { state.mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = {
                        state.setStyle(style)
                        state.setConfig(config)
                        state.request(cameraRequest)
                        state.setMarkers(markers)
                        state.setClusters(clusters)
                        state.setUser(userLocation)
                    },
                )
                Attribution(palette, Modifier.align(Alignment.BottomStart).padding(8.dp))
            }
        }

        private companion object {
            /** How far from a pin a tap still opens it. */
            const val TAP_RADIUS_DP = 14f
        }
    }

/**
 * The map view and what it shows, retained while its screen is in the back stack. MapLibre loads asynchronously (map,
 * then style), so every input is stored and pushed to the map once it is ready. Until the first frame, the view is
 * painted [loadColor] (the land colour) rather than MapLibre's near-white default.
 */
private class MapLibreState(
    context: Context,
    initialCamera: MapCamera,
    private var config: MapConfig,
    private val tapRadiusPx: Float,
    loadColor: Int,
    var onCameraChange: (MapCamera, GeoBounds) -> Unit,
    var onMarkerClick: (Long) -> Unit,
    var onClusterClick: (MapCluster) -> Unit,
) : RetainObserver {
    private var destroyed = false

    val mapView: MapView
    private var map: MapLibreMap? = null
    private var style: Style? = null
    private var styleJson: String? = null
    private val cameraRequests = CameraRequestTracker()
    private var pendingRequest: CameraRequest? = null
    private var markers: List<MapMarker> = emptyList()
    private var markersShown: List<MapMarker>? = null
    private var clusters: List<MapCluster> = emptyList()
    private var clustersShown: List<MapCluster>? = null
    private var user: GeoPoint? = null
    private var userShown: Boolean = false
    private var userShownAt: GeoPoint? = null

    init {
        MapLibre.getInstance(context)
        val options =
            MapLibreMapOptions
                .createFromAttributes(context)
                .camera(initialCamera.toCameraPosition())
                .minZoomPreference(config.minZoom.toMapLibreZoom())
                .maxZoomPreference(config.maxZoom.toMapLibreZoom())
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false)
                .compassEnabled(false)
                .logoEnabled(false)
                .attributionEnabled(false)
                .foregroundLoadColor(loadColor)
        mapView = MapView(context, options)
        mapView.onCreate(null)
        mapView.getMapAsync { ready ->
            map = ready
            ready.setLatLngBoundsForCameraTarget(config.bounds.toLatLngBounds())
            ready.addOnCameraIdleListener { reportCamera(ready) }
            ready.addOnMapClickListener { point -> onTap(ready, point) }
            applyStyle()
            applyPendingRequest()
            reportCamera(ready)
        }
    }

    override fun onRetained() = Unit

    /**
     * Back on screen after another tab: Android gives the view a new surface, and MapLibre only draws on request, so
     * ask for a frame (posted, so it runs once the view is attached again).
     */
    override fun onEnteredComposition() {
        mapView.post { map?.triggerRepaint() }
    }

    override fun onExitedComposition() = Unit

    /** No longer kept (its screen left the back stack, or nothing retains it): free the map. */
    override fun onRetired() = destroy()

    override fun onUnused() = destroy()

    private fun destroy() {
        if (destroyed) return
        destroyed = true
        mapView.onDestroy()
    }

    fun setStyle(json: String) {
        if (json == styleJson) return
        styleJson = json
        applyStyle()
    }

    fun setConfig(newConfig: MapConfig) {
        if (newConfig == config) return
        config = newConfig
        map?.apply {
            setMinZoomPreference(newConfig.minZoom.toMapLibreZoom())
            setMaxZoomPreference(newConfig.maxZoom.toMapLibreZoom())
            setLatLngBoundsForCameraTarget(newConfig.bounds.toLatLngBounds())
        }
    }

    fun request(request: CameraRequest?) {
        if (!cameraRequests.shouldApply(request)) return
        pendingRequest = request
        applyPendingRequest()
    }

    fun setMarkers(newMarkers: List<MapMarker>) {
        markers = newMarkers
        pushData()
    }

    fun setClusters(newClusters: List<MapCluster>) {
        clusters = newClusters
        pushData()
    }

    fun setUser(location: GeoPoint?) {
        user = location
        pushData()
    }

    private fun applyStyle() {
        val ready = map ?: return
        val json = styleJson ?: return
        style = null
        ready.setStyle(Style.Builder().fromJson(json)) { loaded ->
            style = loaded
            // A new style starts with empty sources: send the pins again.
            markersShown = null
            clustersShown = null
            userShown = false
            pushData()
        }
    }

    private fun applyPendingRequest() {
        val ready = map ?: return
        val request = pendingRequest ?: return
        pendingRequest = null
        ready.animateCamera(
            CameraUpdateFactory.newLatLngZoom(request.camera.center.toLatLng(), request.camera.zoom.toMapLibreZoom()),
            ANIMATION_MS,
        )
    }

    private fun pushData() {
        val loaded = style ?: return
        if (markers != markersShown) {
            loaded.getSourceAs<GeoJsonSource>(PINS_SOURCE)?.setGeoJson(markersGeoJson(markers))
            markersShown = markers
        }
        if (clusters != clustersShown) {
            loaded.getSourceAs<GeoJsonSource>(CLUSTERS_SOURCE)?.setGeoJson(clustersGeoJson(clusters))
            clustersShown = clusters
        }
        if (!userShown || user != userShownAt) {
            loaded.getSourceAs<GeoJsonSource>(USER_SOURCE)?.setGeoJson(userGeoJson(user))
            userShown = true
            userShownAt = user
        }
    }

    private fun reportCamera(ready: MapLibreMap) {
        val position = ready.cameraPosition
        val target = position.target ?: return
        val visible = ready.projection.visibleRegion.latLngBounds
        onCameraChange(
            MapCamera(GeoPoint(target.latitude, target.longitude), position.zoom.toContractZoom()),
            GeoBounds(
                south = visible.latitudeSouth,
                west = visible.longitudeWest,
                north = visible.latitudeNorth,
                east = visible.longitudeEast,
            ),
        )
    }

    /** Opens the topmost pin near the tap (selected, then remarkable, then any tree), else the group under it. */
    private fun onTap(
        ready: MapLibreMap,
        point: LatLng,
    ): Boolean {
        val screen = ready.projection.toScreenLocation(point)
        val treeId = pinNear(ready, screen)
        val cluster = if (treeId == null) clusterAt(ready, screen) else null
        when {
            treeId != null -> onMarkerClick(treeId)
            cluster != null -> onClusterClick(cluster)
        }
        return treeId != null || cluster != null
    }

    private fun pinNear(
        ready: MapLibreMap,
        screen: PointF,
    ): Long? {
        val area = RectF(screen.x - tapRadiusPx, screen.y - tapRadiusPx, screen.x + tapRadiusPx, screen.y + tapRadiusPx)
        return PIN_LAYERS_TOP_FIRST.firstNotNullOfOrNull { layer ->
            ready
                .queryRenderedFeatures(area, layer)
                .firstOrNull()
                ?.getNumberProperty("id")
                ?.toLong()
        }
    }

    /** A group is a large disc: only a tap inside it counts. */
    private fun clusterAt(
        ready: MapLibreMap,
        screen: PointF,
    ): MapCluster? {
        val id =
            ready
                .queryRenderedFeatures(screen, CLUSTER_LAYER)
                .firstOrNull()
                ?.getNumberProperty("id")
                ?.toLong() ?: return null
        return clusters.firstOrNull { it.id == id }
    }

    private companion object {
        const val ANIMATION_MS = 600
    }
}

private fun GeoPoint.toLatLng() = LatLng(lat, lon)

private fun MapCamera.toCameraPosition(): CameraPosition =
    CameraPosition
        .Builder()
        .target(center.toLatLng())
        .zoom(zoom.toMapLibreZoom())
        .build()

private fun GeoBounds.toLatLngBounds(): LatLngBounds = LatLngBounds.from(north, east, south, west)

/**
 * Forwards the screen lifecycle to the map view (rendering, tile loading) while it is on screen, and stops it when it
 * leaves. [MapLibreState] destroys it once it is no longer retained.
 */
@Composable
private fun MapViewLifecycle(mapView: MapView) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        var started = false
        var resumed = false
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart().also { started = true }
                    Lifecycle.Event.ON_RESUME -> mapView.onResume().also { resumed = true }
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause().also { resumed = false }
                    Lifecycle.Event.ON_STOP -> mapView.onStop().also { started = false }
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (resumed) mapView.onPause()
            if (started) mapView.onStop()
        }
    }
}

/**
 * The attribution OpenFreeMap asks for, always visible: OpenFreeMap (optional, but fair), © OpenMapTiles and
 * © OpenStreetMap, each a link. Colours come from the palette, so it follows light and dark.
 */
@Composable
private fun Attribution(
    palette: MapPalette,
    modifier: Modifier = Modifier,
) {
    val links = TextLinkStyles(style = SpanStyle(color = palette.selected))
    val text =
        buildAnnotatedString {
            withLink(LinkAnnotation.Url("https://openfreemap.org", links)) { append("OpenFreeMap") }
            append(" · ")
            withLink(LinkAnnotation.Url("https://www.openmaptiles.org/", links)) { append("© OpenMapTiles") }
            append(" · ")
            withLink(LinkAnnotation.Url("https://www.openstreetmap.org/copyright", links)) { append("© OpenStreetMap") }
        }
    Text(
        text = text,
        fontSize = 11.sp,
        color = palette.selected,
        modifier =
            modifier
                .background(palette.ring.copy(alpha = ATTRIBUTION_ALPHA), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

private const val ATTRIBUTION_ALPHA = 0.8f
