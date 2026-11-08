package com.treesofparis.feature.about

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.treesofparis.core.designsystem.component.InfoRow
import com.treesofparis.core.designsystem.component.InfoRowTrailing
import com.treesofparis.core.designsystem.component.SectionHeader
import com.treesofparis.core.designsystem.component.TreesCardGroup
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.icon.TreesIcons
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.util.Locale

const val ABOUT_LIST_TAG = "about_list"
private const val DATASET_URL = "https://opendata.paris.fr/explore/dataset/les-arbres/information/"
private const val ODBL_URL = "https://opendatacommons.org/licenses/odbl/"
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
private const val SOURCE_URL = "https://github.com/jinqian/treesofparis"
private const val OFL_URL = "https://openfontlicense.org"

/** The À propos tab. [onOpenLibraries] shows the open source libraries screen. */
@Composable
fun AboutRoute(
    onOpenLibraries: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    AboutScreen(
        state = viewModel.uiState,
        onOpenUrl = uriHandler::openUri,
        onOpenLibraries = onOpenLibraries,
        modifier = modifier,
    )
}

@Composable
fun AboutScreen(
    state: AboutUiState,
    onOpenUrl: (String) -> Unit,
    onOpenLibraries: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(title = stringResource(R.string.about_title), modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).testTag(ABOUT_LIST_TAG),
            contentPadding = PaddingValues(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        ) {
            item { AppCard(state) }
            section(R.string.about_section_data) {
                InfoRow(
                    title = stringResource(R.string.about_data_credit),
                    subtitle =
                        state.extraction?.let {
                            stringResource(R.string.about_data_extraction, it.date, it.treeCount)
                        },
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(DATASET_URL) },
                )
                TreesDivider()
                InfoRow(
                    title = stringResource(R.string.about_data_license),
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(ODBL_URL) },
                )
            }
            section(R.string.about_section_map) {
                InfoRow(
                    title = stringResource(R.string.about_map_credit),
                    subtitle = stringResource(R.string.about_map_engine),
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(OSM_COPYRIGHT_URL) },
                )
            }
            section(R.string.about_section_project) {
                InfoRow(
                    title = stringResource(R.string.about_project_source),
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(SOURCE_URL) },
                )
                TreesDivider()
                InfoRow(
                    title = stringResource(R.string.about_section_libraries),
                    trailing = InfoRowTrailing.CHEVRON,
                    onClick = onOpenLibraries,
                )
            }
            section(R.string.about_section_fonts) {
                InfoRow(
                    title = stringResource(R.string.about_fonts_credit),
                    subtitle = stringResource(R.string.about_fonts_license),
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(OFL_URL) },
                )
            }
        }
    }
}

/** A section header followed by its rows in one card. */
private fun LazyListScope.section(
    @StringRes title: Int,
    rows: @Composable ColumnScope.() -> Unit,
) {
    item(key = title) {
        Column(modifier = Modifier.padding(top = TreesSpacing.m)) {
            SectionHeader(stringResource(title))
            TreesCardGroup(content = rows)
        }
    }
}

@Composable
private fun AppCard(state: AboutUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(TreesSpacing.m),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TreesIcons.Leaf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.about_version, state.versionName).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    TreesTheme {
        AboutScreen(
            state = AboutUiState(versionName = "0.1.0"),
            onOpenUrl = {},
            onOpenLibraries = {},
        )
    }
}
