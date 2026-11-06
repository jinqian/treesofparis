package com.treesofparis.feature.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.treesofparis.core.designsystem.component.SectionHeader
import com.treesofparis.core.designsystem.component.TreesScaffold
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ABOUT_LIST_TAG = "about_list"
private const val DATASET_URL = "https://opendata.paris.fr/explore/dataset/les-arbres/information/"
private const val ODBL_URL = "https://opendatacommons.org/licenses/odbl/"
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
private const val SOURCE_URL = "https://github.com/jinqian/treesofparis"

/**
 * The À propos tab. [librariesResId] is the `R.raw.aboutlibraries` resource that the AboutLibraries Gradle plugin
 * generates in :app (the only module that sees every dependency).
 */
@Composable
fun AboutRoute(
    librariesResId: Int,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val libs by produceLibraries {
        withContext(Dispatchers.IO) {
            context.resources
                .openRawResource(librariesResId)
                .bufferedReader()
                .use { it.readText() }
        }
    }
    val libraries =
        remember(libs) {
            libs
                ?.libraries
                .orEmpty()
                .map { it.toUi() }
                .sortedBy { it.name.lowercase() }
        }
    val uriHandler = LocalUriHandler.current
    AboutScreen(
        state = viewModel.uiState,
        libraries = libraries,
        onOpenUrl = uriHandler::openUri,
        modifier = modifier,
    )
}

@Composable
fun AboutScreen(
    state: AboutUiState,
    libraries: List<LibraryUi>,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(title = stringResource(R.string.about_title), modifier = modifier) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).testTag(ABOUT_LIST_TAG)) {
            appSection(state)
            item { SectionHeader(stringResource(R.string.about_section_data)) }
            item {
                InfoRow(
                    stringResource(R.string.about_data_credit),
                    stringResource(R.string.about_data_license),
                ) { onOpenUrl(DATASET_URL) }
            }
            item {
                InfoRow(stringResource(R.string.about_data_license), ODBL_URL) { onOpenUrl(ODBL_URL) }
            }
            item { SectionHeader(stringResource(R.string.about_section_map)) }
            item {
                InfoRow(
                    stringResource(R.string.about_map_credit),
                    stringResource(R.string.about_map_engine),
                ) { onOpenUrl(OSM_COPYRIGHT_URL) }
            }
            item { SectionHeader(stringResource(R.string.about_section_project)) }
            item { InfoRow(stringResource(R.string.about_project_source), SOURCE_URL) { onOpenUrl(SOURCE_URL) } }
            item { SectionHeader(stringResource(R.string.about_section_libraries)) }
            items(libraries, key = { it.name }) { library ->
                InfoRow(library.name, library.detail, onClick = library.url?.let { url -> { onOpenUrl(url) } })
            }
        }
    }
}

private fun LazyListScope.appSection(state: AboutUiState) {
    item {
        ListItem(
            headlineContent = {
                Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.titleLarge)
            },
            supportingContent = {
                Text(stringResource(R.string.about_description), modifier = Modifier.padding(top = TreesSpacing.xs))
            },
            overlineContent = { Text(stringResource(R.string.about_version, state.versionName)) },
        )
    }
}

@Composable
private fun InfoRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    TreesTheme {
        AboutScreen(
            state = AboutUiState(versionName = "0.1.0"),
            libraries = listOf(LibraryUi("osmdroid", "6.1.20 · Apache License 2.0", null)),
            onOpenUrl = {},
        )
    }
}
