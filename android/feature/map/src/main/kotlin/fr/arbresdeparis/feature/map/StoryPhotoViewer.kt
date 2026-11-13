package fr.arbresdeparis.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing

const val PHOTO_VIEWER_TAG = "photo_viewer"

/** Night ink behind the photo, and the light inks on it (canvas « Photo plein écran — 1 Sombre »). */
private val ViewerGround = Color(0xFF12110F)
private val ViewerInk = Color(0xFFFBFAF6)
private val ViewerInkMuted = Color(0xFFC9C6BC)

private const val MAX_ZOOM = 4f
private const val DOUBLE_TAP_ZOOM = 2.5f
private val DISMISS_DRAG = 120.dp

/** The tree's photo over the whole screen, in a window of its own: back, × or a swipe down close it. */
@Composable
internal fun StoryPhotoDialog(
    detail: TreeDetailUi,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        StoryPhotoViewer(detail, onClose)
    }
}

/**
 * The photo alone on a dark ground: pinch or double-tap to zoom, drag to look around, swipe down to close. The name,
 * the place and the photographer's credit sit below it; a tap hides them and the close button, another brings them
 * back. The credit shows whenever they do.
 */
@Composable
internal fun StoryPhotoViewer(
    detail: TreeDetailUi,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val story = detail.story ?: return
    var showChrome by remember { mutableStateOf(true) }
    Box(modifier = modifier.fillMaxSize().background(ViewerGround).testTag(PHOTO_VIEWER_TAG)) {
        ZoomablePhoto(story, onClose, onTap = { showChrome = !showChrome })
        AnimatedVisibility(showChrome, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    shape = CircleShape,
                    color = ViewerInk.copy(alpha = 0.14f),
                    contentColor = ViewerInk,
                    modifier = Modifier.statusBarsPadding().padding(TreesSpacing.m).size(48.dp),
                ) {
                    IconButton(onClick = onClose) {
                        Icon(TreesIcons.Close, contentDescription = stringResource(R.string.map_story_photo_close))
                    }
                }
                PhotoCaption(detail, story, Modifier.align(Alignment.BottomStart))
            }
        }
    }
}

@Composable
private fun PhotoCaption(
    detail: TreeDetailUi,
    story: RemarkableStoryUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(ViewerGround.copy(alpha = 0.6f))
                .navigationBarsPadding()
                .padding(horizontal = TreesSpacing.l, vertical = TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            buildAnnotatedString {
                append(detail.name)
                detail.scientificName?.let {
                    append("  ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ViewerInkMuted)) { append(it) }
                }
            },
            style = MaterialTheme.typography.titleMedium,
            color = ViewerInk,
        )
        Text("${detail.address} · ${detail.area}", style = MaterialTheme.typography.bodySmall, color = ViewerInkMuted)
        story.photoCredit?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = ViewerInkMuted,
                modifier = Modifier.padding(top = TreesSpacing.xs),
            )
        }
    }
}

/**
 * The photo, fitted to the screen. Two fingers zoom (up to [MAX_ZOOM]), a double tap zooms in or back out, one finger
 * moves a zoomed photo within its bounds; at normal size, a swipe down past [DISMISS_DRAG] closes the viewer.
 */
@Composable
private fun ZoomablePhoto(
    story: RemarkableStoryUi,
    onClose: () -> Unit,
    onTap: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    val dismissDrag = with(LocalDensity.current) { DISMISS_DRAG.toPx() }

    fun bounded(
        target: Offset,
        zoom: Float,
    ): Offset {
        val maxX = (zoom - 1) * bounds.width / 2
        val maxY = (zoom - 1) * bounds.height / 2
        return Offset(target.x.coerceIn(-maxX, maxX), target.y.coerceIn(-maxY, maxY))
    }

    AsyncImage(
        model = story.photoUrl,
        contentDescription = story.photoCredit?.let { stringResource(R.string.map_story_photo, it) },
        contentScale = ContentScale.Fit,
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { bounds = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { tap ->
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                scale = DOUBLE_TAP_ZOOM
                                offset = bounded((center - tap) * (DOUBLE_TAP_ZOOM - 1), DOUBLE_TAP_ZOOM)
                            }
                        },
                    )
                }.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var dismissY = 0f
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val pinching = event.changes.count { it.pressed } > 1
                            if (pinching || scale > 1f) {
                                scale = (scale * zoom).coerceIn(1f, MAX_ZOOM)
                                offset = bounded(offset + pan, scale)
                                event.changes.forEach { if (it.positionChange() != Offset.Zero) it.consume() }
                            } else {
                                dismissY += pan.y
                                offset = Offset(0f, dismissY.coerceAtLeast(0f))
                                if (pan != Offset.Zero) event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                        if (scale == 1f) {
                            if (dismissY > dismissDrag) onClose() else offset = Offset.Zero
                        }
                    }
                }.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
    )
}
