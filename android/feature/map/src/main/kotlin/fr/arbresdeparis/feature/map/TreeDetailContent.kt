package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.ScientificNameStyle
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.TreeSource
import java.util.Locale

/** The body of the tree detail bottom sheet, laid out like a herbarium label. */
@Composable
fun TreeDetailContent(
    detail: TreeDetailUi,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenWikipedia: (String) -> Unit = {},
    onOpenDirections: (GeoPoint) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        LeafDrawing(
            leaf = detail.leaf,
            color = MaterialTheme.colorScheme.outlineVariant,
            strokeWidth = 1.4.dp,
            modifier = Modifier.align(Alignment.TopEnd).size(132.dp).offset(x = 28.dp, y = (-4).dp),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TreesSpacing.l)
                    .padding(top = TreesSpacing.xs, bottom = TreesSpacing.l)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Header(detail, onFavoriteToggle)
            Location(detail)
            DirectionsButton(onClick = { onOpenDirections(detail.location) })
            Stats(detail)
            detail.flowering?.let { FloweringSection(it) }
            detail.wikipedia?.let { WikipediaRow(it, onOpenWikipedia) }
            Text(
                text =
                    stringResource(
                        when (detail.source) {
                            TreeSource.PARIS -> R.string.map_detail_source
                            TreeSource.GPSO -> R.string.map_detail_source_gpso
                        },
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Header(
    detail: TreeDetailUi,
    onFavoriteToggle: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (detail.remarkable) RemarkableEyebrow()
            Text(detail.name, style = MaterialTheme.typography.displaySmall)
            Text(
                text = detail.scientificName ?: stringResource(R.string.map_detail_unknown_species),
                style = ScientificNameStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FavoriteButton(isFavorite = detail.isFavorite, onClick = onFavoriteToggle)
    }
}

@Composable
private fun RemarkableEyebrow() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
        Icon(
            imageVector = TreesIcons.Remarkable,
            contentDescription = null,
            tint = TreesTheme.colors.remarkable,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.map_detail_remarkable).uppercase(Locale.FRENCH),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (isFavorite) colors.primaryContainer else colors.surfaceContainerLow,
        border = if (isFavorite) null else BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.padding(top = TreesSpacing.xs).size(48.dp),
    ) {
        IconButton(onClick = onClick, colors = IconButtonDefaults.iconButtonColors(contentColor = colors.primary)) {
            Icon(
                imageVector = if (isFavorite) TreesIcons.Favorite else TreesIcons.FavoriteOutlined,
                contentDescription =
                    stringResource(if (isFavorite) R.string.map_favorite_remove else R.string.map_favorite_add),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun Location(detail: TreeDetailUi) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = TreesIcons.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        val area = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        Text(
            text =
                buildAnnotatedString {
                    append(detail.address)
                    append(" · ")
                    withStyle(area) { append(detail.area) }
                },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Hands the tree's position to the user's Maps app, to walk or drive there. */
@Composable
private fun DirectionsButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val openLabel = stringResource(R.string.map_directions_open)
    Surface(
        shape = CircleShape,
        color = colors.primaryContainer,
        modifier =
            Modifier
                .clip(CircleShape)
                .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = TreesSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TreesIcons.Place, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.map_directions),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun Stats(detail: TreeDetailUi) {
    Column {
        TreesDivider()
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Stat(
                label = stringResource(R.string.map_detail_height),
                measure = detail.height,
                placeholder = stringResource(R.string.map_detail_unknown_height),
                modifier = Modifier.weight(1f),
            )
            StatSeparator()
            Stat(
                label = stringResource(R.string.map_detail_circumference),
                measure = detail.circumference,
                placeholder = stringResource(R.string.map_detail_unknown_circumference),
                // « Circonférence » is the longest label: a little more room keeps it on one line on small phones.
                modifier = Modifier.weight(CIRCUMFERENCE_WEIGHT).padding(start = TreesSpacing.m),
            )
            StatSeparator()
            Stat(
                label = stringResource(R.string.map_detail_stage),
                measure = detail.stage?.let { MeasureUi(it, unit = "") },
                placeholder = stringResource(R.string.map_detail_unknown_stage),
                modifier = Modifier.weight(1f).padding(start = TreesSpacing.m),
            )
        }
        TreesDivider()
    }
}

/** Option A of the design: a soft card under the figures that opens the article outside the app. */
@Composable
private fun WikipediaRow(
    wikipedia: WikipediaUi,
    onOpen: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val openLabel = stringResource(R.string.map_wikipedia_open)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.background,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClickLabel = openLabel, role = Role.Button) { onOpen(wikipedia.url) },
    ) {
        Row(
            modifier = Modifier.heightIn(min = 64.dp).padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(TreesIcons.Book, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text =
                        wikipedia.genus?.let { stringResource(R.string.map_wikipedia_genus, it) }
                            ?: stringResource(R.string.map_wikipedia_more),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.map_wikipedia_site, wikipedia.title),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(
                TreesIcons.OpenExternal,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private const val CIRCUMFERENCE_WEIGHT = 1.25f
private val STAT_LABEL_MIN_FONT_SIZE = 9.sp

@Composable
private fun StatSeparator() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight().width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/** A figure in large serif with a small unit, or a muted placeholder when the dataset has no value. */
@Composable
private fun Stat(
    label: String,
    measure: MeasureUi?,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = TreesSpacing.m),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            autoSize =
                TextAutoSize.StepBased(
                    minFontSize = STAT_LABEL_MIN_FONT_SIZE,
                    maxFontSize = MaterialTheme.typography.labelSmall.fontSize,
                ),
        )
        when {
            measure == null -> {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            measure.unit.isEmpty() -> {
                Text(measure.value, style = MaterialTheme.typography.titleLarge)
            }

            else -> {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(measure.value, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = measure.unit,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TreeDetailContentPreview() {
    TreesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            TreeDetailContent(
                detail =
                    TreeDetailUi(
                        id = 1,
                        location = GeoPoint.PARIS_CENTER,
                        name = "Platane",
                        scientificName = "Platanus x hispanica",
                        area = "Paris 19e",
                        address = "Quai de la Seine",
                        height = MeasureUi("16", "m"),
                        circumference = MeasureUi("165", "cm"),
                        stage = "Adulte",
                        remarkable = true,
                        isFavorite = false,
                        leaf = TreeLeaf.PLATANUS,
                    ),
                onFavoriteToggle = {},
            )
        }
    }
}
