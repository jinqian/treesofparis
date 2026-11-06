package com.treesofparis.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treesofparis.core.map.CameraRequest
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MapConfig
import com.treesofparis.core.map.MapMarker
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.UserLocation
import com.treesofparis.domain.usecase.GetTree
import com.treesofparis.domain.usecase.GetTreesInBounds
import com.treesofparis.domain.usecase.LocateUser
import com.treesofparis.domain.usecase.ObserveIsFavorite
import com.treesofparis.domain.usecase.ToggleFavorite
import com.treesofparis.feature.map.mapper.toDetailUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val getTreesInBounds: GetTreesInBounds,
        private val getTree: GetTree,
        private val observeIsFavorite: ObserveIsFavorite,
        private val toggleFavorite: ToggleFavorite,
        private val locateUser: LocateUser,
        private val savedState: SavedStateHandle,
    ) : ViewModel() {
        private val state = MutableStateFlow(MapUiState(initialCamera = savedCamera() ?: DEFAULT_CAMERA))
        val uiState: StateFlow<MapUiState> = state.asStateFlow()

        private val cameraChanges = MutableStateFlow<Pair<MapCamera, GeoBounds>?>(null)
        private var selectionJob: Job? = null
        private var nextRequestId = 0L

        private val selectedId: Long? get() = savedState[KEY_SELECTED]

        init {
            observeCameraChanges()
            selectedId?.let(::select)
        }

        fun onCameraChange(
            camera: MapCamera,
            bounds: GeoBounds,
        ) {
            savedState[KEY_LAT] = camera.center.lat
            savedState[KEY_LON] = camera.center.lon
            savedState[KEY_ZOOM] = camera.zoom
            cameraChanges.value = camera to bounds
        }

        fun onMarkerClick(id: Long) = select(id)

        fun onDetailDismissed() {
            selectionJob?.cancel()
            savedState.remove<Long>(KEY_SELECTED)
            state.update { it.copy(selectedTree = null, markers = it.markers.withSelection(null)) }
        }

        fun onFavoriteToggle() {
            val id = selectedId ?: return
            viewModelScope.launch { toggleFavorite(id) }
        }

        /**
         * Outcome of the location permission request (also called when it was already granted). [userInitiated] is true
         * for the "Me localiser" button: then the user gets a message when the map cannot centre on them. The automatic
         * request at launch stays silent and simply keeps the map on Paris.
         */
        fun onLocationPermissionResult(
            granted: Boolean,
            userInitiated: Boolean = false,
        ) {
            if (!granted) {
                if (userInitiated) state.update { it.copy(message = MapMessage.PERMISSION_DENIED) }
                return
            }
            viewModelScope.launch {
                state.update { it.copy(isLocating = true) }
                val result = locateUser()
                state.update { current ->
                    when (result) {
                        is UserLocation.InArea -> {
                            current.copy(
                                userLocation = result.point,
                                cameraRequest = request(result.point, USER_ZOOM),
                            )
                        }

                        is UserLocation.OutOfArea -> {
                            current.copy(message = MapMessage.OUT_OF_AREA.takeIf { userInitiated })
                        }

                        UserLocation.Unavailable -> {
                            current.copy(message = MapMessage.LOCATION_UNAVAILABLE.takeIf { userInitiated })
                        }
                    }.copy(isLocating = false)
                }
            }
        }

        fun onMessageShown() {
            state.update { it.copy(message = null) }
        }

        /** Opens a tree coming from another screen (Favoris): centre on it and show its details, once per id. */
        fun onOpenTree(id: Long) {
            if (savedState.get<Long>(KEY_OPENED) == id) return
            savedState[KEY_OPENED] = id
            viewModelScope.launch {
                val tree = getTree(id) ?: return@launch
                state.update { it.copy(cameraRequest = request(tree.location, TREE_ZOOM)) }
                select(id)
            }
        }

        @OptIn(FlowPreview::class)
        private fun observeCameraChanges() {
            viewModelScope.launch {
                cameraChanges.filterNotNull().debounce(CAMERA_DEBOUNCE_MS).collectLatest { (camera, bounds) ->
                    if (camera.zoom < MIN_TREE_ZOOM) {
                        state.update { it.copy(markers = emptyList(), showZoomHint = true) }
                    } else {
                        val selected = selectedId
                        val markers =
                            getTreesInBounds(bounds, MAX_MARKERS).map {
                                MapMarker(id = it.id, position = it.location, selected = it.id == selected)
                            }
                        state.update { it.copy(markers = markers, showZoomHint = false) }
                    }
                }
            }
        }

        private fun select(id: Long) {
            savedState[KEY_SELECTED] = id
            state.update { it.copy(markers = it.markers.withSelection(id)) }
            selectionJob?.cancel()
            selectionJob =
                viewModelScope.launch {
                    val tree = getTree(id)
                    if (tree == null) {
                        onDetailDismissed()
                        return@launch
                    }
                    observeIsFavorite(id).collect { isFavorite ->
                        state.update { it.copy(selectedTree = tree.toDetailUi(isFavorite)) }
                    }
                }
        }

        private fun request(
            center: GeoPoint,
            zoom: Double,
        ) = CameraRequest(id = ++nextRequestId, MapCamera(center, zoom))

        private fun savedCamera(): MapCamera? {
            val lat = savedState.get<Double>(KEY_LAT)
            val lon = savedState.get<Double>(KEY_LON)
            if (lat == null || lon == null) return null
            return MapCamera(GeoPoint(lat, lon), savedState[KEY_ZOOM] ?: DEFAULT_CAMERA.zoom)
        }

        private fun List<MapMarker>.withSelection(id: Long?) = map { it.copy(selected = it.id == id) }

        companion object {
            /** Below this zoom a phone screen covers too many trees to show them usefully. */
            const val MIN_TREE_ZOOM = 16.0
            const val MAX_MARKERS = 1500
            const val CAMERA_DEBOUNCE_MS = 300L
            const val USER_ZOOM = 17.0
            const val TREE_ZOOM = 18.0
            val DEFAULT_CAMERA = MapCamera(GeoPoint.PARIS_CENTER, zoom = MIN_TREE_ZOOM)

            /** OSM tiles stop at zoom 19; osmdroid upscales them up to [MapConfig.maxZoom] for street-level detail. */
            val MAP_CONFIG = MapConfig(GeoBounds.COVERAGE, minZoom = 11.0, maxZoom = 21.0)

            private const val KEY_LAT = "camera_lat"
            private const val KEY_LON = "camera_lon"
            private const val KEY_ZOOM = "camera_zoom"
            private const val KEY_SELECTED = "selected_tree"
            private const val KEY_OPENED = "opened_tree"
        }
    }
