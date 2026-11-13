package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.ScientificNameStyle
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.domain.usecase.GetRemarkableStory
import fr.arbresdeparis.domain.usecase.GetTree
import fr.arbresdeparis.feature.map.mapper.toDetailUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val STORY_CARD_TAG = "story_card"

/**
 * In the tree sheet (option B of #74): the City's photo, the one-sentence summary and « Son histoire › », which opens
 * the [RemarkableStoryScreen]. Kept to one row so the sheet barely grows.
 */
@Composable
internal fun StoryCard(
    story: RemarkableStoryUi,
    onOpenStory: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            Modifier
                .testTag(STORY_CARD_TAG)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.tertiaryContainer)
                .clickable(role = Role.Button, onClick = onOpenStory)
                .padding(start = 12.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StoryPhoto(story, MaterialTheme.shapes.small, showCredit = false, modifier = Modifier.size(64.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            story.summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onTertiaryContainer,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.map_story_open),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onTertiaryContainer,
            )
        }
        Icon(TreesIcons.ChevronRight, null, tint = colors.onTertiaryContainer, modifier = Modifier.size(18.dp))
    }
}

/**
 * The City's photo of the tree, loaded from its server, over a sage ground with a leaf (shown while it loads, or
 * offline). [showCredit]: the photographer's credit on the photo, as the City asks.
 */
@Composable
private fun StoryPhoto(
    story: RemarkableStoryUi,
    shape: Shape,
    showCredit: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.clip(shape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
        LeafDrawing(TreeLeaf.GENERIC, color = colors.primary.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
        story.photoUrl?.let {
            AsyncImage(
                model = it,
                contentDescription =
                    story.photoCredit?.let { credit ->
                        stringResource(R.string.map_story_photo, credit)
                    },
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (showCredit && story.photoCredit != null) {
            Text(
                text = story.photoCredit,
                style = MaterialTheme.typography.labelSmall,
                color = colors.surfaceContainerLow,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(TreesSpacing.s)
                        .background(colors.onSurface.copy(alpha = 0.55f), MaterialTheme.shapes.small)
                        .padding(horizontal = TreesSpacing.s, vertical = 2.dp),
            )
        }
    }
}

/**
 * « Son histoire »: the City's photo, why the tree is remarkable, its story, its measures and planting year, its
 * national label and council decision, its sign (PDF) and Wikipedia. Opens from the tree sheet.
 */
@Composable
fun RemarkableStoryScreen(
    detail: TreeDetailUi,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val story = detail.story ?: return
    val colors = MaterialTheme.colorScheme
    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Box {
            StoryBody(detail, story, onOpenUrl)
            // Pinned over the page, so it stays at hand however far the story scrolls.
            Surface(
                shape = CircleShape,
                color = colors.surfaceContainerLow.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                modifier = Modifier.statusBarsPadding().padding(TreesSpacing.m).size(48.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(TreesIcons.Back, contentDescription = stringResource(R.string.map_story_back))
                }
            }
        }
    }
}

@Composable
private fun StoryBody(
    detail: TreeDetailUi,
    story: RemarkableStoryUi,
    onOpenUrl: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        StoryPhoto(
            story,
            RectangleShapeCompat,
            showCredit = true,
            modifier = Modifier.fillMaxWidth().height(PHOTO_HEIGHT),
        )
        Column(
            modifier =
                Modifier
                    .padding(horizontal = TreesSpacing.l)
                    .padding(top = 20.dp, bottom = TreesSpacing.l)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            StoryHeader(detail, story)
            story.summary?.let { WhyRemarkable(it) }
            story.story?.let {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.map_story_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
            StoryFacts(detail, story)
            story.signUrl?.let { SignRow(onOpen = { onOpenUrl(it) }) }
            detail.wikipedia?.let { WikipediaRow(it, onOpenUrl) }
            Text(
                stringResource(R.string.map_detail_source_remarkable),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/** « ★ ARBRE REMARQUABLE · PAYSAGER », the name, the Latin name and where it stands. */
@Composable
private fun StoryHeader(
    detail: TreeDetailUi,
    story: RemarkableStoryUi,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        RemarkableEyebrow(story.qualification)
        Text(detail.name, style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
        detail.scientificName?.let { Text(it, style = ScientificNameStyle, color = colors.onSurfaceVariant) }
        Text(
            "${detail.address} · ${detail.area}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}

/** The measures, then the planting year, the national label and the council decision when there is one. */
@Composable
private fun StoryFacts(
    detail: TreeDetailUi,
    story: RemarkableStoryUi,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        Stats(detail)
        Fact(
            stringResource(R.string.map_story_planted),
            story.plantingYear ?: stringResource(R.string.map_story_planted_unknown),
        )
        story.label?.let { Fact(stringResource(R.string.map_story_label), it) }
        story.councilDecision?.let {
            Fact(stringResource(R.string.map_story_council), stringResource(R.string.map_story_council_value, it))
        }
    }
}

private val PHOTO_HEIGHT: Dp = 280.dp
private val RectangleShapeCompat: Shape = androidx.compose.ui.graphics.RectangleShape

@Composable
private fun WhyRemarkable(summary: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.tertiaryContainer, MaterialTheme.shapes.medium)
                .padding(TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            stringResource(R.string.map_story_why).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onTertiaryContainer,
        )
        Text(summary, style = MaterialTheme.typography.bodyLarge, color = colors.onTertiaryContainer)
    }
}

/** « Label national  A.R.B.R.E.S en 2015 »: a soft tag and its value. */
@Composable
private fun Fact(
    label: String,
    value: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                    .padding(horizontal = TreesSpacing.s, vertical = 3.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** « Le panneau de l’arbre · PDF · Ville de Paris ↗ ». */
@Composable
private fun SignRow(onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.background,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    MaterialTheme.shapes.medium,
                ).clickable(role = Role.Button, onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(TreesIcons.Flag, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.map_story_sign),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    stringResource(R.string.map_story_sign_detail),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(TreesIcons.OpenExternal, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

/** The page's state: the tree with its story, or nothing while it loads (or when the tree has no story). */
data class RemarkableStoryUiState(
    val detail: TreeDetailUi? = null,
)

@HiltViewModel
class RemarkableStoryViewModel
    @Inject
    constructor(
        private val getTree: GetTree,
        private val getRemarkableStory: GetRemarkableStory,
    ) : ViewModel() {
        private val state = MutableStateFlow(RemarkableStoryUiState())
        val uiState: StateFlow<RemarkableStoryUiState> = state.asStateFlow()

        fun onOpen(treeId: Long) {
            if (state.value.detail?.id == treeId) return
            viewModelScope.launch {
                val tree = getTree(treeId) ?: return@launch
                val story = getRemarkableStory(treeId) ?: return@launch
                state.value = RemarkableStoryUiState(tree.toDetailUi(isFavorite = false, story = story))
            }
        }
    }

/** « Son histoire » for the remarkable tree [treeId]; opens the sign and Wikipedia in a Custom Tab. */
@Composable
fun RemarkableStoryRoute(
    treeId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RemarkableStoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(treeId) { viewModel.onOpen(treeId) }
    state.detail?.let {
        RemarkableStoryScreen(it, onBack = onBack, onOpenUrl = context::openInCustomTab, modifier = modifier)
    }
}
