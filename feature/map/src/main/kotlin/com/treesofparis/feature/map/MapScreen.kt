package com.treesofparis.feature.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.treesofparis.core.designsystem.component.TreesBottomSheet
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MapPalette
import com.treesofparis.core.map.TreeMapView
import com.treesofparis.domain.model.GeoBounds

/**
 * The Carte tab. [treeId] opens a tree coming from another screen (Favoris); [genus] shows only that genus (from
 * Herbier). Asks for the location permission once; the map works without it.
 */
@Composable
fun MapRoute(
    treeId: Long?,
    modifier: Modifier = Modifier,
    genus: String? = null,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Whether the pending permission request came from the "Me localiser" button (feedback) or from launch (silent).
    var userInitiatedRequest by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            viewModel.onLocationPermissionResult(
                granted = results.values.any { it },
                userInitiated = userInitiatedRequest,
            )
        }
    val locate = { userInitiated: Boolean ->
        if (context.hasLocationPermission()) {
            viewModel.onLocationPermissionResult(granted = true, userInitiated = userInitiated)
        } else {
            userInitiatedRequest = userInitiated
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }
    var askedForLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!askedForLocation) {
            askedForLocation = true
            locate(false)
        }
    }
    LaunchedEffect(treeId) { treeId?.let(viewModel::onOpenTree) }
    LaunchedEffect(genus) { genus?.let(viewModel::onGenusFilterRequest) }

    MapScreen(
        state = state,
        onCameraChange = viewModel::onCameraChange,
        onMarkerClick = viewModel::onMarkerClick,
        onDetailDismiss = viewModel::onDetailDismissed,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onLocateMe = { locate(true) },
        onMessageDismiss = viewModel::onMessageShown,
        onOpenSettings = { context.openAppSettings() },
        onRemarkableFilterChange = viewModel::onRemarkableFilterChange,
        onRemarkableInfoOpen = viewModel::onRemarkableInfoOpen,
        onRemarkableInfoDismiss = viewModel::onRemarkableInfoDismiss,
        onOpenWikipedia = { url -> context.openInCustomTab(url) },
        onGenusFilterClear = viewModel::onGenusFilterClear,
        modifier = modifier,
    )
}

@Composable
fun MapScreen(
    state: MapUiState,
    onCameraChange: (MapCamera, GeoBounds) -> Unit,
    onMarkerClick: (Long) -> Unit,
    onDetailDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLocateMe: () -> Unit,
    onMessageDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onRemarkableInfoOpen: () -> Unit,
    onRemarkableInfoDismiss: () -> Unit,
    onOpenWikipedia: (String) -> Unit,
    onGenusFilterClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(state.message, snackbarHostState, onMessageDismiss, onOpenSettings)
    Box(modifier = modifier.fillMaxSize()) {
        TreeMapView(
            initialCamera = state.initialCamera,
            cameraRequest = state.cameraRequest,
            markers = state.markers,
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
            modifier = Modifier.align(Alignment.TopStart),
        )
        LocateButton(
            isLocating = state.isLocating,
            onClick = onLocateMe,
            modifier = Modifier.align(Alignment.BottomEnd).padding(TreesSpacing.m),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = TreesSpacing.xl * 2),
        )
        state.selectedTree?.let { detail ->
            TreesBottomSheet(onDismiss = onDetailDismiss) {
                TreeDetailContent(
                    detail = detail,
                    onFavoriteToggle = onFavoriteToggle,
                    onOpenWikipedia = onOpenWikipedia,
                )
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

/** The filter segments and, under them, the active filter's pill and the zoom hint. */
@Composable
private fun MapFilters(
    state: MapUiState,
    onRemarkableFilterChange: (Boolean) -> Unit,
    onRemarkableInfoOpen: () -> Unit,
    onGenusFilterClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.statusBarsPadding().padding(TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
    ) {
        RemarkableFilterBar(
            remarkableOnly = state.remarkableOnly,
            genusFilterActive = state.genusFilter != null,
            onRemarkableFilterChange = onRemarkableFilterChange,
            onInfoClick = onRemarkableInfoOpen,
        )
        if (state.remarkableOnly) RemarkableOnlyCaption(Modifier.align(Alignment.CenterHorizontally))
        state.genusFilter?.let {
            GenusFilterPill(
                it,
                onGenusFilterClear,
                Modifier.align(Alignment.CenterHorizontally),
            )
        }
        if (state.showZoomHint) ZoomHint(Modifier.align(Alignment.CenterHorizontally))
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

@Composable
private fun LocateButton(
    isLocating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = { if (!isLocating) onClick() },
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 6.dp),
    ) {
        if (isLocating) {
            val description = stringResource(R.string.map_locating)
            CircularProgressIndicator(
                modifier = Modifier.size(TreesSpacing.l).semantics { contentDescription = description },
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                TreesIcons.MyLocation,
                contentDescription = stringResource(R.string.map_locate_me),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Shows [message] once in a snackbar, then reports it consumed. Permission errors offer a « Réglages » action. */
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
                actionLabel = settings.takeIf { message == MapMessage.PERMISSION_DENIED },
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
        }

@Composable
private fun ZoomHint(modifier: Modifier = Modifier) {
    FloatingPill(modifier) {
        Text(
            text = stringResource(R.string.map_zoom_hint),
            modifier = Modifier.padding(horizontal = TreesSpacing.m, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
