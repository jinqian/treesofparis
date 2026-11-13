package fr.arbresdeparis.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.map.CameraRequest
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MapConfig
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MarkerStyle
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.UserLocation
import fr.arbresdeparis.domain.usecase.GetFloweringSeason
import fr.arbresdeparis.domain.usecase.GetGenera
import fr.arbresdeparis.domain.usecase.GetNearestTrees
import fr.arbresdeparis.domain.usecase.GetRemarkableStory
import fr.arbresdeparis.domain.usecase.GetTree
import fr.arbresdeparis.domain.usecase.GetTreeClusters
import fr.arbresdeparis.domain.usecase.GetTreesInBounds
import fr.arbresdeparis.domain.usecase.LocateUser
import fr.arbresdeparis.domain.usecase.ObserveIsFavorite
import fr.arbresdeparis.domain.usecase.ToggleFavorite
import fr.arbresdeparis.feature.map.mapper.toDetailUi
import fr.arbresdeparis.feature.map.mapper.toFloweringUi
import fr.arbresdeparis.feature.map.mapper.toGenusPicker
import fr.arbresdeparis.feature.map.mapper.toMapCluster
import fr.arbresdeparis.feature.map.mapper.toNearbyUi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions") // one handler per user event, as the ViewModel convention asks
class MapViewModel
    @Inject
    constructor(
        private val getTreesInBounds: GetTreesInBounds,
        private val getTreeClusters: GetTreeClusters,
        private val getTree: GetTree,
        private val observeIsFavorite: ObserveIsFavorite,
        private val toggleFavorite: ToggleFavorite,
        private val locateUser: LocateUser,
        private val getNearestTrees: GetNearestTrees,
        private val getFloweringSeason: GetFloweringSeason,
        private val getRemarkableStory: GetRemarkableStory,
        private val getGenera: GetGenera,
        private val clock: Clock,
        private val savedState: SavedStateHandle,
    ) : ViewModel() {
        private val state =
            MutableStateFlow(
                MapUiState(
                    initialCamera = savedCamera() ?: DEFAULT_CAMERA,
                    remarkableOnly = savedState[KEY_REMARKABLE] ?: false,
                    genusFilter = savedState.get<String>(KEY_GENUS)?.toGenusFilter(),
                ),
            )
        val uiState: StateFlow<MapUiState> = state.asStateFlow()

        private val cameraChanges = MutableStateFlow<Pair<MapCamera, GeoBounds>?>(null)
        private val remarkableOnly = MutableStateFlow(savedState[KEY_REMARKABLE] ?: false)
        private val genus = MutableStateFlow(savedState.get<String>(KEY_GENUS))
        private var selectionJob: Job? = null
        private var nextRequestId = 0L

        private val selectedId: Long? get() = savedState[KEY_SELECTED]

        /** The marker drawn as selected: the tree whose sheet is open, else the nearby tree on show. */
        private val highlightedId: Long? get() =
            selectedId ?: state.value.nearby
                ?.current
                ?.id

        init {
            genus.value?.let(::nameGenus)
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

        /** Zooms into a group, two levels closer, at most to just past street level where its trees show. */
        fun onClusterClick(cluster: MapCluster) {
            val zoom = cameraChanges.value?.first?.zoom ?: DEFAULT_CAMERA.zoom
            val target = minOf(zoom + CLUSTER_ZOOM_STEP, MIN_TREE_ZOOM + CLUSTER_ZOOM_PAST_STREET_LEVEL)
            state.update { it.copy(cameraRequest = request(cluster.position, target)) }
        }

        /** « Zoomez pour voir les arbres »: zooms in on the same spot, just past the level where trees show. */
        fun onZoomHintClick() {
            val center = cameraChanges.value?.first?.center ?: DEFAULT_CAMERA.center
            state.update { it.copy(cameraRequest = request(center, MIN_TREE_ZOOM + CLUSTER_ZOOM_PAST_STREET_LEVEL)) }
        }

        fun onDetailDismissed() {
            selectionJob?.cancel()
            savedState.remove<Long>(KEY_SELECTED)
            state.update { it.copy(selectedTree = null) }
            state.update { it.copy(markers = it.markers.withSelection(highlightedId)) }
        }

        /** « Son histoire » opens over the map: the sheet closes, so coming back shows the map itself. */
        fun onStoryOpen() = onDetailDismissed()

        fun onFavoriteToggle() {
            val id = selectedId ?: return
            viewModelScope.launch { toggleFavorite(id) }
        }

        /**
         * Outcome of the location button « Me localiser » ([userInitiated]), or of the launch check when location is
         * already granted. A user tap gets a message when the map cannot centre on them; the launch check stays silent
         * and simply keeps the map on Paris.
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

        /**
         * « Arbre le plus proche »: frames the closest tree (of the active filter) and the user, and highlights it; the
         * card then offers its sheet and the next closest trees. [granted] is false when the user refused to share
         * their location (the button shares it first, like « Me localiser »).
         */
        fun onNearestTreeRequest(granted: Boolean) {
            if (!granted) {
                state.update { it.copy(message = MapMessage.PERMISSION_DENIED) }
                return
            }
            if (state.value.isFindingNearest) return
            viewModelScope.launch {
                state.update { it.copy(isFindingNearest = true) }
                when (val location = locateUser()) {
                    is UserLocation.InArea -> showNearestTrees(location.point)
                    is UserLocation.OutOfArea -> state.update { it.copy(message = MapMessage.OUT_OF_AREA) }
                    UserLocation.Unavailable -> state.update { it.copy(message = MapMessage.LOCATION_UNAVAILABLE) }
                }
                state.update { it.copy(isFindingNearest = false) }
            }
        }

        private suspend fun showNearestTrees(from: GeoPoint) {
            val found = getNearestTrees(from, NEARBY_COUNT, remarkableOnly.value, genus.value).map { it.toNearbyUi() }
            if (found.isEmpty()) {
                state.update { it.copy(userLocation = from, message = MapMessage.NO_TREE_NEARBY) }
                return
            }
            showNearby(NearbyTreesUi(from, found), userLocation = from)
        }

        /** Shows the next closest tree, back to the closest after the last one. */
        fun onNearbyNext() {
            val nearby = state.value.nearby ?: return
            showNearby(nearby.copy(index = (nearby.index + 1) % nearby.trees.size))
        }

        /** Opens the sheet of the nearby tree on show. */
        fun onNearbyOpen() {
            state.value.nearby?.let { select(it.current.id) }
        }

        fun onNearbyDismiss() {
            state.update { it.copy(nearby = null) }
            state.update { it.copy(markers = it.markers.withSelection(highlightedId)) }
        }

        private fun showNearby(
            nearby: NearbyTreesUi,
            userLocation: GeoPoint = nearby.from,
        ) {
            state.update {
                it.copy(
                    nearby = nearby,
                    userLocation = userLocation,
                    cameraRequest = request(nearbyCamera(nearby.from, nearby.current.location)),
                )
            }
            state.update { it.copy(markers = it.markers.withSelection(highlightedId)) }
        }

        /**
         * Shows only remarkable trees (at every zoom level) when [enabled], or all trees. It combines with the genus
         * filter (the remarkable plane trees), unless that genus has no remarkable tree: then the genus filter goes.
         * Choosing also closes the « Qu'est-ce qu'un arbre remarquable ? » sheet, which offers that choice, and ends
         * a nearby search, whose trees may now be hidden.
         */
        fun onRemarkableFilterChange(enabled: Boolean) {
            setRemarkable(enabled)
            state.update { it.copy(showRemarkableInfo = false, nearby = null) }
            val current = genus.value ?: return
            if (!enabled) return
            viewModelScope.launch {
                val hasRemarkable = getGenera(remarkableOnly = true).any { it.genus == current }
                if (!hasRemarkable && genus.value == current) setGenus(null)
            }
        }

        /**
         * A genus asked for by navigation (from Herbier): all its trees, so the remarkable filter goes. Applied once,
         * not again when the screen is recreated.
         */
        fun onGenusFilterRequest(genus: String) {
            if (savedState.get<Boolean>(KEY_GENUS_REQUEST_APPLIED) == true) return
            savedState[KEY_GENUS_REQUEST_APPLIED] = true
            setRemarkable(false)
            setGenus(genus)
        }

        fun onGenusFilterClear() = setGenus(null)

        /** Opens the « Essences » sheet, listing the genera for the current remarkable filter. */
        fun onGenusPickerOpen() {
            viewModelScope.launch {
                val onlyRemarkable = remarkableOnly.value
                val picker = getGenera(onlyRemarkable).toGenusPicker(genus.value, onlyRemarkable)
                state.update { it.copy(genusPicker = picker) }
            }
        }

        fun onGenusPickerDismiss() {
            state.update { it.copy(genusPicker = null) }
        }

        /** Shows only the trees of [genus] (a dataset genus, e.g. « Tilia »), at every zoom level, with its name. */
        fun onGenusPick(choice: GenusChoiceUi) {
            setGenus(choice.genus, choice.commonName)
            onGenusPickerDismiss()
        }

        private fun setRemarkable(enabled: Boolean) {
            savedState[KEY_REMARKABLE] = enabled
            remarkableOnly.value = enabled
            state.update { it.copy(remarkableOnly = enabled) }
        }

        /** Every genus change goes through here; it also ends a nearby search, whose trees may now be hidden. */
        private fun setGenus(
            value: String?,
            commonName: String? = null,
        ) {
            savedState[KEY_GENUS] = value
            genus.value = value
            state.update { it.copy(genusFilter = value?.toGenusFilter(commonName), nearby = null) }
            if (value != null && commonName == null) nameGenus(value)
        }

        /** Fills in the French name of the filtered genus (from Herbier or a restored state), for the pill. */
        private fun nameGenus(value: String) {
            viewModelScope.launch {
                val name =
                    getGenera(remarkableOnly = false).firstOrNull { it.genus == value }?.commonName ?: return@launch
                state.update { current ->
                    current.genusFilter
                        ?.takeIf { it.genus == value }
                        ?.let { current.copy(genusFilter = it.copy(commonName = name)) }
                        ?: current
                }
            }
        }

        private fun String.toGenusFilter(commonName: String? = null) =
            GenusFilterUi(genus = this, leaf = TreeLeaf.forGenus(this), commonName = commonName)

        fun onRemarkableInfoOpen() {
            state.update { it.copy(showRemarkableInfo = true) }
        }

        fun onRemarkableInfoDismiss() {
            state.update { it.copy(showRemarkableInfo = false) }
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
                // The camera is debounced; a filter change reloads right away with the last camera.
                val camera = cameraChanges.filterNotNull().debounce(CAMERA_DEBOUNCE_MS)
                combine(camera, remarkableOnly, genus, ::Triple)
                    .collectLatest { (cameraAndBounds, onlyRemarkable, onlyGenus) ->
                        loadMarkers(cameraAndBounds.first, cameraAndBounds.second, onlyRemarkable, onlyGenus)
                    }
            }
        }

        /**
         * Below [MIN_TREE_ZOOM] there are too many trees to show: the map shows groups of trees with their count
         * instead, except with a filter: there are only a few hundred remarkable trees, and a genus is shown at every
         * zoom too, capped at the [MAX_MARKERS] trees closest to the centre, so its spread across the city stays
         * visible.
         */
        private suspend fun loadMarkers(
            camera: MapCamera,
            bounds: GeoBounds,
            onlyRemarkable: Boolean,
            onlyGenus: String?,
        ) {
            if (camera.zoom < MIN_TREE_ZOOM && !onlyRemarkable && onlyGenus == null) {
                val clusters =
                    getTreeClusters(bounds, camera.zoom).mapIndexed {
                        index,
                        group,
                        ->
                        group.toMapCluster(index)
                    }
                state.update { it.copy(markers = emptyList(), clusters = clusters, showZoomHint = true) }
                return
            }
            val selected = highlightedId
            val markers =
                getTreesInBounds(bounds, MAX_MARKERS, onlyRemarkable, onlyGenus).map {
                    MapMarker(
                        id = it.id,
                        position = it.location,
                        selected = it.id == selected,
                        style = if (it.remarkable) MarkerStyle.REMARKABLE else MarkerStyle.TREE,
                    )
                }
            state.update { it.copy(markers = markers, clusters = emptyList(), showZoomHint = false) }
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
                    val flowering = getFloweringSeason(tree.genus)?.toFloweringUi(LocalDate.now(clock).month)
                    val story = if (tree.remarkable) getRemarkableStory(id) else null
                    observeIsFavorite(id).collect { isFavorite ->
                        state.update { it.copy(selectedTree = tree.toDetailUi(isFavorite, flowering, story)) }
                    }
                }
        }

        private fun request(
            center: GeoPoint,
            zoom: Double,
        ) = request(MapCamera(center, zoom))

        private fun request(camera: MapCamera) = CameraRequest(id = ++nextRequestId, camera)

        private fun savedCamera(): MapCamera? {
            val lat = savedState.get<Double>(KEY_LAT)
            val lon = savedState.get<Double>(KEY_LON)
            if (lat == null || lon == null) return null
            return MapCamera(GeoPoint(lat, lon), savedState[KEY_ZOOM] ?: DEFAULT_CAMERA.zoom)
        }

        private fun List<MapMarker>.withSelection(id: Long?) = map { it.copy(selected = it.id == id) }

        companion object {
            /**
             * Below this zoom a phone screen covers too many trees to show them usefully. At 15.5 a phone shows about
             * 870 × 1,560 m: 2,300 to 2,700 trees in the densest areas (Hôtel de Ville, Marais, Champs-Élysées).
             */
            const val MIN_TREE_ZOOM = 15.5

            /** At most this many pins, the closest to the centre: room for the densest views at [MIN_TREE_ZOOM]. */
            const val MAX_MARKERS = 3000
            const val CAMERA_DEBOUNCE_MS = 300L

            /** A tap on a group zooms in by this much... */
            const val CLUSTER_ZOOM_STEP = 2.0

            /** ...but no further than this past [MIN_TREE_ZOOM], where the group's trees show one by one. */
            const val CLUSTER_ZOOM_PAST_STREET_LEVEL = 0.5
            const val USER_ZOOM = 17.0
            const val TREE_ZOOM = 18.0

            /** How many of the closest trees « Suivant » goes through. */
            const val NEARBY_COUNT = 5
            val DEFAULT_CAMERA = MapCamera(GeoPoint.PARIS_CENTER, zoom = MIN_TREE_ZOOM)

            /** Vector tiles stop at zoom 14 (MapLibre scale); the map draws them sharp up to [MapConfig.maxZoom]. */
            val MAP_CONFIG = MapConfig(GeoBounds.COVERAGE, minZoom = 11.0, maxZoom = 21.0)

            private const val KEY_LAT = "camera_lat"
            private const val KEY_LON = "camera_lon"
            private const val KEY_ZOOM = "camera_zoom"
            private const val KEY_SELECTED = "selected_tree"
            private const val KEY_OPENED = "opened_tree"
            private const val KEY_REMARKABLE = "remarkable_only"
            private const val KEY_GENUS = "genus_filter"
            private const val KEY_GENUS_REQUEST_APPLIED = "genus_request_applied"
        }
    }
