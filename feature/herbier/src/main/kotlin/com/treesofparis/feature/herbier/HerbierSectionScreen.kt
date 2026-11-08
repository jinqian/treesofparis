package com.treesofparis.feature.herbier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.treesofparis.core.designsystem.component.LoadingState
import com.treesofparis.core.designsystem.component.SectionHeader
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.time.Month

/**
 * A level-2 Herbier screen, pushed from a card of the home. [onGenusClick] shows a genus on the map, [onTreeClick] a
 * remarkable tree.
 */
@Composable
fun HerbierSectionRoute(
    section: HerbierSection,
    onBack: () -> Unit,
    onGenusClick: (String) -> Unit,
    onTreeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HerbierViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HerbierSectionScreen(section, state, onBack, onGenusClick, modifier, onTreeClick)
}

@Composable
fun HerbierSectionScreen(
    section: HerbierSection,
    state: HerbierUiState,
    onBack: () -> Unit,
    onGenusClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onTreeClick: (Long) -> Unit = {},
) {
    val content = state as? HerbierUiState.Content
    TreesScaffold(
        title = stringResource(section.title),
        subtitle = content?.let { subtitle(section, it) },
        onBack = onBack,
        modifier = modifier,
    ) { padding ->
        if (content == null) {
            LoadingState(Modifier.padding(padding))
            return@TreesScaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).testTag(HERBIER_LIST_TAG),
            contentPadding = PaddingValues(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.l),
        ) {
            when (section) {
                HerbierSection.GENERA -> generaContent(content.herbier, onGenusClick)
                HerbierSection.FLOWERING -> floweringContent(content.flowering)
                HerbierSection.REMARKABLE -> remarkableContent(content, onTreeClick)
                HerbierSection.AGE_AND_AREAS -> ageAndAreasContent(content.herbier)
            }
        }
    }
}

private val HerbierSection.title
    get() =
        when (this) {
            HerbierSection.GENERA -> R.string.herbier_section_genera
            HerbierSection.FLOWERING -> R.string.herbier_section_flowering
            HerbierSection.REMARKABLE -> R.string.herbier_section_remarkable
            HerbierSection.AGE_AND_AREAS -> R.string.herbier_section_age_areas
        }

@Composable
private fun subtitle(
    section: HerbierSection,
    content: HerbierUiState.Content,
): String? =
    when (section) {
        HerbierSection.GENERA -> {
            stringResource(R.string.herbier_genera_subtitle, content.herbier.genera.size, content.herbier.drawnShare)
        }

        HerbierSection.FLOWERING -> {
            stringResource(R.string.herbier_flowering_subtitle, content.flowering.rows.size)
        }

        HerbierSection.REMARKABLE, HerbierSection.AGE_AND_AREAS -> {
            null
        }
    }

private fun LazyListScope.generaContent(
    herbier: HerbierUi,
    onGenusClick: (String) -> Unit,
) {
    item { Note(stringResource(R.string.herbier_genera_hint)) }
    item { GeneraList(herbier.genera, onGenusClick) }
}

private fun LazyListScope.floweringContent(flowering: FloweringCalendarUi) {
    item { Note(stringResource(R.string.herbier_flowering_intro)) }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(stringResource(R.string.herbier_flowering_calendar))
                Text(
                    stringResource(R.string.herbier_flowering_approx),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = TreesSpacing.s),
                )
            }
            FloweringCalendar(flowering)
            FloweringLegend()
        }
    }
    item { Note(stringResource(R.string.herbier_flowering_source)) }
}

private fun LazyListScope.remarkableContent(
    content: HerbierUiState.Content,
    onTreeClick: (Long) -> Unit,
) {
    item { RemarkableCard(content.herbier.remarkable) }
    if (content.remarkableTrees.isNotEmpty()) item { Note(stringResource(R.string.herbier_remarkable_hint)) }
    content.remarkableTrees.forEach { area ->
        item(key = area.name) { RemarkableAreaGroup(area, onTreeClick) }
    }
}

private fun LazyListScope.ageAndAreasContent(herbier: HerbierUi) {
    item { StagesSection(herbier.stages, herbier.unknownStageCount) }
    if (herbier.areas.isNotEmpty()) item { AreasSection(herbier.areas) }
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = TreesSpacing.s),
    )
}

@PreviewLightDark
@Composable
private fun FloweringSectionPreview() {
    TreesTheme {
        HerbierSectionScreen(HerbierSection.FLOWERING, previewContent(Month.NOVEMBER), onBack = {}, onGenusClick = {})
    }
}
