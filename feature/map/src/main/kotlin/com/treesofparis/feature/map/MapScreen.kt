package com.treesofparis.feature.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        onRemarkableFilterToggle = viewModel::onRemarkableFilterToggle,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
    onRemarkableFilterToggle: () -> Unit,
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
        Column(
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            ) {
                BrandPill()
                Spacer(Modifier.weight(1f))
                RemarkableFilterChip(selected = state.remarkableOnly, onClick = onRemarkableFilterToggle)
            }
            if (state.showZoomHint) ZoomHint()
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
private fun BrandPill(modifier: Modifier = Modifier) {
    FloatingPill(modifier) {
        Row(
            modifier = Modifier.height(44.dp).padding(start = 12.dp, end = TreesSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        ) {
            Icon(
                imageVector = TreesIcons.Leaf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.map_brand),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                modifier = Modifier.semantics { heading() },
            )
        }
    }
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

/** Shows only the remarkable trees; they keep their gold pin either way. */
@Composable
private fun RemarkableFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(R.string.map_filter_remarkable), style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                if (selected) TreesIcons.Remarkable else TreesIcons.RemarkableOutlined,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        modifier = modifier.height(44.dp),
        shape = CircleShape,
        // Opaque on top of the map, whether selected or not.
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = colors.surfaceContainerLow,
                labelColor = colors.onSurface,
                iconColor = colors.onSurface,
                selectedContainerColor = colors.primary,
                selectedLabelColor = colors.onPrimary,
                selectedLeadingIconColor = colors.onPrimary,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = colors.outlineVariant,
                selectedBorderColor = colors.primary,
            ),
        elevation = FilterChipDefaults.filterChipElevation(elevation = 4.dp),
    )
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
