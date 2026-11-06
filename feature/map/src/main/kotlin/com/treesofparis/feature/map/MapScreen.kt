package com.treesofparis.feature.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results -> viewModel.onLocationPermissionResult(granted = results.values.any { it }) }
    val locate = {
        if (context.hasLocationPermission()) {
            viewModel.onLocationPermissionResult(granted = true)
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }
    var askedForLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!askedForLocation) {
            askedForLocation = true
            locate()
        }
    }
    LaunchedEffect(treeId) { treeId?.let(viewModel::onOpenTree) }

    MapScreen(
        state = state,
        onCameraChange = viewModel::onCameraChange,
        onMarkerClick = viewModel::onMarkerClick,
        onDetailDismiss = viewModel::onDetailDismissed,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onLocateMe = locate,
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
    modifier: Modifier = Modifier,
) {
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
        SmallFloatingActionButton(
            onClick = onLocateMe,
            modifier = Modifier.align(Alignment.BottomEnd).padding(TreesSpacing.m),
        ) {
            Icon(TreesIcons.MyLocation, contentDescription = stringResource(R.string.map_locate_me))
        }
        state.selectedTree?.let { detail ->
            TreesBottomSheet(onDismiss = onDetailDismiss) {
                TreeDetailContent(detail = detail, onFavoriteToggle = onFavoriteToggle)
            }
        }
    }
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
