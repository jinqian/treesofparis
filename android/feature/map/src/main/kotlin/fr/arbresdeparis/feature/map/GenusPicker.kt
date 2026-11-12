package fr.arbresdeparis.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.leaf.LeafDrawing
import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.core.designsystem.theme.ScientificNameStyle
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.feature.map.mapper.matches

internal const val GENUS_PILL_TAG = "genus_pill"

internal const val LEAF_CARD_TAG = "leaf_card"

/** Both genus pills (« Toutes les essences » and « Seulement : Tilleul ») share this height, the touch-target size. */
internal val GENUS_PILL_HEIGHT = 48.dp

/** « Toutes les essences ⌄ » under the filter segments: opens the « Essences » sheet. */
@Composable
internal fun GenusFilterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.testTag(GENUS_PILL_TAG).height(GENUS_PILL_HEIGHT),
        shape = CircleShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TreesIcons.Leaf, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.map_genus_all), style = MaterialTheme.typography.labelLarge)
            Icon(
                TreesIcons.ChevronDown,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * The « Essences » sheet body: a search field, the drawn genera as a grid of leaves, then the other genera as a list.
 * The search runs on the names shown, the French names and the Latin genus.
 */
@Composable
internal fun GenusPickerContent(
    picker: GenusPickerUi,
    onPick: (GenusChoiceUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val drawn = picker.drawn.filter { it.matches(query, it.displayName()) }
    val others = picker.others.filter { it.matches(query, it.displayName()) }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = leafCardMinWidth()),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = TreesSpacing.l, end = TreesSpacing.l, bottom = TreesSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
    ) {
        fullWidth { PickerHeader(picker.remarkableOnly) }
        fullWidth {
            SearchField(query = query, onQueryChange = { query = it }, modifier = Modifier.padding(top = 6.dp))
        }
        if (drawn.isEmpty() && others.isEmpty()) {
            fullWidth { NoMatch(query) }
        }
        if (drawn.isNotEmpty()) {
            fullWidth { SectionLabel(stringResource(R.string.map_genus_drawn)) }
            items(drawn, key = { it.genus }) { LeafCard(it, onPick) }
        }
        if (others.isNotEmpty()) {
            fullWidth { SectionLabel(stringResource(R.string.map_genus_others, others.size)) }
            items(others, key = { it.genus }, span = { GridItemSpan(maxLineSpan) }) { OtherGenusRow(it, onPick) }
        }
    }
}

/**
 * Three leaf cards a row on most phones, two on a narrow screen or with large text, so a long name (« Micocoulier »)
 * stays on one line instead of breaking mid-word. Grows with the font scale.
 */
@Composable
private fun leafCardMinWidth(): Dp = LEAF_CARD_MIN_WIDTH * LocalDensity.current.fontScale.coerceAtLeast(1f)

private val LEAF_CARD_MIN_WIDTH = 96.dp

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { content() }

/** The name the app shows: the drawing's label for a drawn genus, else the French name most of its trees carry. */
@Composable
private fun GenusChoiceUi.displayName(): String =
    if (leaf == TreeLeaf.GENERIC) commonName else stringResource(leaf.label)

@Composable
private fun PickerHeader(remarkableOnly: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.map_genus_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text =
                stringResource(
                    if (remarkableOnly) R.string.map_genus_subtitle_remarkable else R.string.map_genus_subtitle,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = CircleShape,
        label = { Text(stringResource(R.string.map_genus_search)) },
        placeholder = { Text(stringResource(R.string.map_genus_search_hint)) },
        leadingIcon = { Icon(TreesIcons.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceContainerLowest,
                unfocusedContainerColor = colors.surfaceContainerLowest,
                unfocusedBorderColor = colors.outlineVariant,
            ),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = TreesSpacing.m),
    )
}

@Composable
private fun NoMatch(query: String) {
    Text(
        text = stringResource(R.string.map_genus_none, query.trim()),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = TreesSpacing.l),
    )
}

/**
 * A drawn genus: its leaf, name and number of trees. The current filter is outlined. The name always takes two lines,
 * centred, so « Cerisier, prunier » does not make its card taller than the others.
 */
@Composable
private fun LeafCard(
    choice: GenusChoiceUi,
    onPick: (GenusChoiceUi) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (choice.selected) colors.primaryContainer else colors.surfaceContainerLowest,
        border =
            if (choice.selected) BorderStroke(1.5.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
        modifier =
            Modifier
                .testTag(LEAF_CARD_TAG)
                .selectable(selected = choice.selected, role = Role.RadioButton, onClick = { onPick(choice) }),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LeafDrawing(choice.leaf, Modifier.size(44.dp), strokeWidth = 1.3.dp)
            val nameStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp)
            Box(
                modifier = Modifier.height(with(LocalDensity.current) { (nameStyle.lineHeight * 2).toDp() }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = choice.displayName(),
                    style = nameStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = treeCount(choice),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** A genus without drawing: the French name over the Latin genus, and the number of trees. */
@Composable
private fun OtherGenusRow(
    choice: GenusChoiceUi,
    onPick: (GenusChoiceUi) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .selectable(selected = choice.selected, role = Role.RadioButton, onClick = { onPick(choice) })
                    .padding(vertical = TreesSpacing.s),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeafDrawing(
                TreeLeaf.GENERIC,
                Modifier.size(28.dp),
                color = if (choice.selected) colors.primary else colors.outline,
                strokeWidth = 1.3.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(choice.displayName(), style = MaterialTheme.typography.titleMedium)
                Text(
                    choice.genus,
                    style = ScientificNameStyle.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                    color = colors.onSurfaceVariant,
                )
            }
            Text(treeCount(choice), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        TreesDivider()
    }
}

@Composable
private fun treeCount(choice: GenusChoiceUi): String =
    pluralStringResource(R.plurals.map_genus_trees, choice.count, choice.countLabel)

@PreviewLightDark
@Composable
@Suppress("MagicNumber") // sample counts
private fun GenusPickerPreview() {
    fun choice(
        genus: String,
        name: String,
        count: Int,
        selected: Boolean = false,
    ) = GenusChoiceUi(genus, name, TreeLeaf.forGenus(genus), count, "%,d".format(count), selected)
    TreesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            GenusPickerContent(
                picker =
                    GenusPickerUi(
                        drawn =
                            listOf(
                                choice("Platanus", "Platane", 42_591),
                                choice("Aesculus", "Marronnier", 24_795),
                                choice("Tilia", "Tilleul", 23_072, selected = true),
                            ),
                        others = listOf(choice("Ginkgo", "Arbre aux quarante écus", 1_154)),
                        remarkableOnly = false,
                    ),
                onPick = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GenusFilterButtonPreview() {
    TreesTheme { GenusFilterButton(onClick = {}, modifier = Modifier.padding(TreesSpacing.m)) }
}
