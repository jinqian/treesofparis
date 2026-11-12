package fr.arbresdeparis.feature.map

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.locationbutton.compose.LocationButton
import androidx.core.locationbutton.compose.LocationButtonTextType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.arbresdeparis.core.designsystem.component.TreesBottomSheet
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.core.map.MapCamera
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.core.map.TreeMapView
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint

/**
 * The Carte tab. [treeId] opens a tree coming from another screen (Favoris); [genus] shows only that genus (from
 * Herbier). Never asks for location by itself: the user shares it with the location button (ADR 0006), and the map
 * centres on them at launch only when location is already granted.
 *
 * [inFront] is false while the Carte screen is not settled on screen (a tab transition): the system location buttons
 * are drawn by Android on a layer above the whole window, so they give way to look-alikes until then.
 */
@Composable
fun MapRoute(
    treeId: Long?,
    modifier: Modifier = Modifier,
    genus: String? = null,
    inFront: Boolean = true,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var checkedLocationAtLaunch by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!checkedLocationAtLaunch) {
            checkedLocationAtLaunch = true
            if (context.hasLocationPermission()) viewModel.onLocationPermissionResult(true, userInitiated = false)
        }
    }
    // The system grants precise location for a session: check again whenever the screen comes back.
    var preciseLocation by remember { mutableStateOf(context.hasPreciseLocationPermission()) }
    LifecycleResumeEffect(Unit) {
        preciseLocation = context.hasPreciseLocationPermission()
        onPauseOrDispose {}
    }
    LaunchedEffect(treeId) { treeId?.let(viewModel::onOpenTree) }
    LaunchedEffect(genus) { genus?.let(viewModel::onGenusFilterRequest) }

    MapScreen(
        state = state,
        onCameraChange = viewModel::onCameraChange,
        onMarkerClick = viewModel::onMarkerClick,
        onDetailDismiss = viewModel::onDetailDismissed,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onLocateResult = { granted ->
            preciseLocation = context.hasPreciseLocationPermission()
            viewModel.onLocationPermissionResult(granted, userInitiated = true)
        },
        onMessageDismiss = viewModel::onMessageShown,
        onOpenSettings = { context.openAppSettings() },
        onRemarkableFilterChange = viewModel::onRemarkableFilterChange,
        onRemarkableInfoOpen = viewModel::onRemarkableInfoOpen,
        onRemarkableInfoDismiss = viewModel::onRemarkableInfoDismiss,
        onOpenWikipedia = { url -> context.openInCustomTab(url) },
        onOpenDirections = { location -> context.openInMapsApp(location) },
        onGenusFilterClear = viewModel::onGenusFilterClear,
        onGenusPickerOpen = viewModel::onGenusPickerOpen,
        onGenusPick = viewModel::onGenusPick,
        onGenusPickerDismiss = viewModel::onGenusPickerDismiss,
        onZoomHintClick = viewModel::onZoomHintClick,
        onFindNearestTree = { viewModel.onNearestTreeRequest(context.hasLocationPermission()) },
        onNearbyNext = viewModel::onNearbyNext,
        onNearbyOpen = viewModel::onNearbyOpen,
        onNearbyDismiss = viewModel::onNearbyDismiss,
        modifier = modifier,
        onClusterClick = viewModel::onClusterClick,
        preciseLocation = preciseLocation,
        showSystemLocationButtons = inFront,
        onNearestLocationResult = { granted ->
            preciseLocation = context.hasPreciseLocationPermission()
            viewModel.onNearestTreeRequest(granted)
        },
    )
}

@Composable
fun MapScreen(
    state: MapUiState,
    onCameraChange: (MapCamera, GeoBounds) -> Unit,
    onMarkerClick: (Long) -> Unit,
    onDetailDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLocateResult: (Boolean) -> Unit,
    onMessageDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onRemarkableInfoOpen: () -> Unit,
    onRemarkableInfoDismiss: () -> Unit,
    onOpenWikipedia: (String) -> Unit,
    onGenusFilterClear: () -> Unit,
    onGenusPickerOpen: () -> Unit,
    onGenusPick: (GenusChoiceUi) -> Unit,
    onGenusPickerDismiss: () -> Unit,
    onFindNearestTree: () -> Unit,
    onNearbyNext: () -> Unit,
    onNearbyOpen: () -> Unit,
    onNearbyDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onClusterClick: (MapCluster) -> Unit = {},
    preciseLocation: Boolean = true,
    onNearestLocationResult: (Boolean) -> Unit = {},
    showSystemLocationButtons: Boolean = true,
    onZoomHintClick: () -> Unit = {},
    onOpenDirections: (GeoPoint) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(state.message, snackbarHostState, onMessageDismiss, onOpenSettings)
    Box(modifier = modifier.fillMaxSize()) {
        TreeMapView(
            initialCamera = state.initialCamera,
            cameraRequest = state.cameraRequest,
            markers = state.markers,
            clusters = state.clusters,
            onClusterClick = onClusterClick,
            userLocation = state.userLocation,
            config = MapViewModel.MAP_CONFIG,
            palette = mapPalette(),
            onMarkerClick = onMarkerClick,
            onCameraChange = onCameraChange,
            modifier = Modifier.fillMaxSize(),
        )
        MapFilters(
            state = state,
            onRemarkableFilterChange = onRemarkableFilterChange,
            onRemarkableInfoOpen = onRemarkableInfoOpen,
            onGenusFilterClear = onGenusFilterClear,
            onGenusPickerOpen = onGenusPickerOpen,
            onZoomHintClick = onZoomHintClick,
            modifier = Modifier.align(Alignment.TopStart),
        )
        LocationControls(
            state = state,
            onLocateResult = onLocateResult,
            onFindNearestTree = onFindNearestTree,
            preciseLocation = preciseLocation,
            onNearestLocationResult = onNearestLocationResult,
            showSystemLocationButtons = showSystemLocationButtons,
            onNearbyNext = onNearbyNext,
            onNearbyOpen = onNearbyOpen,
            onNearbyDismiss = onNearbyDismiss,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = TreesSpacing.xl * 2),
        )
        TreeSheet(state.selectedTree, onDetailDismiss, onFavoriteToggle, onOpenWikipedia, onOpenDirections)
        state.genusPicker?.let { picker ->
            TreesBottomSheet(onDismiss = onGenusPickerDismiss) {
                GenusPickerContent(picker = picker, onPick = onGenusPick)
            }
        }
        if (state.showRemarkableInfo) {
            TreesBottomSheet(onDismiss = onRemarkableInfoDismiss) {
                RemarkableInfoContent(
                    onShowRemarkableOnly = { onRemarkableFilterChange(true) },
                    onClose = onRemarkableInfoDismiss,
                )
            }
        }
    }
}

/** The open tree's sheet: its details, favorite and Wikipédia link. */
@Composable
private fun TreeSheet(
    detail: TreeDetailUi?,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onOpenWikipedia: (String) -> Unit,
    onOpenDirections: (GeoPoint) -> Unit,
) {
    if (detail == null) return
    TreesBottomSheet(onDismiss = onDismiss) {
        TreeDetailContent(
            detail = detail,
            onFavoriteToggle = onFavoriteToggle,
            onOpenWikipedia = onOpenWikipedia,
            onOpenDirections = onOpenDirections,
        )
    }
}

/**
 * « Arbre le plus proche » over « Me localiser »; the nearby tree's card takes the button's place under them. Until
 * precise location is shared ([preciseLocation] false), « Arbre le plus proche » is the system location button itself
 * (« Près de ma position exacte »): one tap shares the location and finds the tree, like « Me localiser ».
 * Without [showSystemLocationButtons], our own buttons take the system ones' place (same size, no effect).
 */
@Composable
@Suppress("LongParameterList") // the controls' state and events
private fun LocationControls(
    state: MapUiState,
    onLocateResult: (Boolean) -> Unit,
    onFindNearestTree: () -> Unit,
    onNearbyNext: () -> Unit,
    onNearbyOpen: () -> Unit,
    onNearbyDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    preciseLocation: Boolean = true,
    onNearestLocationResult: (Boolean) -> Unit = {},
    showSystemLocationButtons: Boolean = true,
) {
    Column(
        modifier = modifier.padding(TreesSpacing.m),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
    ) {
        if (state.nearby == null) {
            if (preciseLocation || state.isFindingNearest) {
                NearestTreeButton(isSearching = state.isFindingNearest, onClick = onFindNearestTree)
            } else if (!showSystemLocationButtons) {
                NearestTreeButton(isSearching = false, onClick = {})
            } else {
                NearestTreeLocationButton(onResult = onNearestLocationResult)
            }
        }
        LocateButton(
            isLocating = state.isLocating,
            onResult = onLocateResult,
            systemButton = showSystemLocationButtons,
        )
        state.nearby?.let {
            NearbyTreeCard(
                nearby = it,
                onOpen = onNearbyOpen,
                onNext = onNearbyNext,
                onDismiss = onNearbyDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The filter segments and, under them, the remarkable caption, the genus pill (« Toutes les essences » or
 * « Seulement : Tilleul ») and the zoom hint.
 */
@Composable
private fun MapFilters(
    state: MapUiState,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onRemarkableInfoOpen: () -> Unit,
    onGenusFilterClear: () -> Unit,
    onGenusPickerOpen: () -> Unit,
    onZoomHintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.statusBarsPadding().padding(TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
    ) {
        RemarkableFilterBar(
            remarkableOnly = state.remarkableOnly,
            onRemarkableFilterChange = onRemarkableFilterChange,
            onInfoClick = onRemarkableInfoOpen,
        )
        if (state.remarkableOnly) RemarkableOnlyCaption(Modifier.align(Alignment.CenterHorizontally))
        val genusFilter = state.genusFilter
        if (genusFilter == null) {
            GenusFilterButton(onClick = onGenusPickerOpen, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            GenusFilterPill(
                filter = genusFilter,
                onOpen = onGenusPickerOpen,
                onClear = onGenusFilterClear,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        if (state.showZoomHint) {
            ZoomHint(
                onClick = onZoomHintClick,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/** Pin colours from the theme: moss trees, gold remarkable ones, an ink selection, paper rings. */
@Composable
private fun mapPalette() =
    MapPalette(
        tree = TreesTheme.colors.treePin,
        remarkable = TreesTheme.colors.remarkable,
        selected = MaterialTheme.colorScheme.onSurface,
        ring = TreesTheme.colors.pinRing,
        userLocation = TreesTheme.colors.userLocation,
        treeGroup = TreesTheme.colors.treeGroup,
        treeGroupLabel = TreesTheme.colors.treeGroupLabel,
    )

/** A paper pill floating over the map, softly shadowed. */
@Composable
private fun FloatingPill(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
        content = content,
    )
}

/**
 * « Me localiser »: the system location button (Android 17+), icon only, in the round paper look of the map controls.
 * It shares the precise location for this session; on Android 16 and lower the same button shows the usual permission
 * prompt. [onResult] tells whether location was granted. While locating, a spinner takes its place. Without
 * [systemButton], the same circle shows our own location icon and does nothing.
 */
@Composable
private fun LocateButton(
    isLocating: Boolean,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    systemButton: Boolean = true,
) {
    val description = stringResource(if (isLocating) R.string.map_locating else R.string.map_locate_me)
    Surface(
        // The icon-only button has no label of its own: name it for screen readers.
        modifier =
            modifier.size(LOCATE_BUTTON_SIZE).semantics(mergeDescendants = true) {
                contentDescription =
                    description
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(TreesSpacing.l), strokeWidth = 2.dp)
            } else if (!systemButton) {
                Icon(
                    TreesIcons.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(LOCATE_ICON_SIZE),
                )
            } else {
                LocationButton(
                    modifier = Modifier.fillMaxSize().testTag(SYSTEM_LOCATION_BUTTON_TAG),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    iconTint = MaterialTheme.colorScheme.primary,
                    textType = LocationButtonTextType.None,
                    cornerRadius = LOCATE_BUTTON_SIZE / 2,
                    pressedCornerRadius = LOCATE_BUTTON_SIZE / 2,
                    onError = { onResult(false) },
                    onPermissionResult = onResult,
                )
            }
        }
    }
}

private val LOCATE_BUTTON_SIZE = 52.dp
private val LOCATE_ICON_SIZE = 24.dp

/** Marks the system location buttons, which Android 17 draws above the window (see [MapRoute]). */
internal const val SYSTEM_LOCATION_BUTTON_TAG = "system_location_button"

/**
 * Shows [message] once in a snackbar, then reports it consumed. A refused permission offers « Réglages » on Android 16
 * and lower; from Android 17 precise location only comes from the location button, so there is nothing to change there.
 */
@Composable
private fun MessageEffect(
    message: MapMessage?,
    snackbarHostState: SnackbarHostState,
    onMessageDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val text = message?.let { stringResource(it.textRes) }
    val settings = stringResource(R.string.map_location_settings)
    val currentOnMessageShown by rememberUpdatedState(onMessageDismiss)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    LaunchedEffect(message) {
        if (message == null || text == null) return@LaunchedEffect
        val result =
            snackbarHostState.showSnackbar(
                message = text,
                actionLabel =
                    settings.takeIf {
                        message == MapMessage.PERMISSION_DENIED &&
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN
                    },
                duration = SnackbarDuration.Short,
            )
        currentOnMessageShown()
        if (result == SnackbarResult.ActionPerformed) currentOnOpenSettings()
    }
}

private val MapMessage.textRes: Int
    get() =
        when (this) {
            MapMessage.OUT_OF_AREA -> R.string.map_location_outside
            MapMessage.LOCATION_UNAVAILABLE -> R.string.map_location_unavailable
            MapMessage.PERMISSION_DENIED -> R.string.map_location_denied
            MapMessage.NO_TREE_NEARBY -> R.string.map_nearest_none
        }

/** « Zoomez pour voir les arbres » with a magnifier: a tap zooms in until the trees show (issue #42). */
@Composable
private fun ZoomHint(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = ZOOM_HINT_MIN_HEIGHT),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = TreesSpacing.m),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                TreesIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp).testTag(ZOOM_HINT_ICON_TAG),
            )
            Text(text = stringResource(R.string.map_zoom_hint), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** A touch target, like the other map controls. */
private val ZOOM_HINT_MIN_HEIGHT = 48.dp

internal const val ZOOM_HINT_ICON_TAG = "zoom_hint_icon"
