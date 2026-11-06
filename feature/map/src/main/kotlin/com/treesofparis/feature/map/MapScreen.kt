package com.treesofparis.feature.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
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
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.TreeMapView
import com.treesofparis.domain.model.GeoBounds

/**
 * The Carte tab. [treeId] opens a tree coming from another screen (Favoris). Asks for the location permission once;
 * the map works without it.
 */
@Composable
fun MapRoute(
    treeId: Long?,
    modifier: Modifier = Modifier,
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

    MapScreen(
        state = state,
        onCameraChange = viewModel::onCameraChange,
        onMarkerClick = viewModel::onMarkerClick,
        onDetailDismiss = viewModel::onDetailDismissed,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onLocateMe = { locate(true) },
        onMessageDismiss = viewModel::onMessageShown,
        onOpenSettings = { context.openAppSettings() },
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
            onMarkerClick = onMarkerClick,
            onCameraChange = onCameraChange,
            modifier = Modifier.fillMaxSize(),
        )
        if (state.showZoomHint) {
            ZoomHint(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(TreesSpacing.m))
        }
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
                TreeDetailContent(detail = detail, onFavoriteToggle = onFavoriteToggle)
            }
        }
    }
}

@Composable
private fun LocateButton(
    isLocating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmallFloatingActionButton(onClick = { if (!isLocating) onClick() }, modifier = modifier) {
        if (isLocating) {
            val description = stringResource(R.string.map_locating)
            CircularProgressIndicator(
                modifier = Modifier.size(TreesSpacing.l).semantics { contentDescription = description },
                strokeWidth = 2.dp,
            )
        } else {
            Icon(TreesIcons.MyLocation, contentDescription = stringResource(R.string.map_locate_me))
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
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = TreesSpacing.xs,
    ) {
        Text(
            text = stringResource(R.string.map_zoom_hint),
            modifier = Modifier.padding(horizontal = TreesSpacing.m, vertical = TreesSpacing.s),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
