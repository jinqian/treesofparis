package fr.arbresdeparis.feature.about

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.arbresdeparis.core.designsystem.component.InfoRow
import fr.arbresdeparis.core.designsystem.component.InfoRowTrailing
import fr.arbresdeparis.core.designsystem.component.SectionHeader
import fr.arbresdeparis.core.designsystem.component.SwitchRow
import fr.arbresdeparis.core.designsystem.component.TreesCardGroup
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.component.TreesScaffold
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import java.util.Locale

const val ABOUT_LIST_TAG = "about_list"
private const val DATASET_URL = "https://opendata.paris.fr/explore/dataset/les-arbres/information/"
private const val GPSO_DATASET_URL = "https://data.seineouest.fr/explore/dataset/arbres-v2/information/"
private const val GUIDE_URL =
    "https://opendata.paris.fr/explore/dataset/fiches-essences-du-guide-des-essences-de-paris/information/"
private const val ODBL_URL = "https://opendatacommons.org/licenses/odbl/"
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
private const val OPENMAPTILES_URL = "https://www.openmaptiles.org/"
private const val OPENFREEMAP_URL = "https://openfreemap.org"
private const val SOURCE_URL = "https://github.com/jinqian/treesofparis"
private const val OFL_URL = "https://openfontlicense.org"
private const val PRIVACY_URL = "https://arbresdeparis.fr/confidentialite/"

/** The À propos tab. [onOpenLibraries] shows the open source libraries screen. */
@Composable
fun AboutRoute(
    onOpenLibraries: () -> Unit,
    onOpenFloweringSources: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AboutScreen(
        state = state,
        onOpenUrl = uriHandler::openUri,
        onWriteToUs = { context.writeToUs(state.contactEmail, state.versionName) },
        onOpenLibraries = onOpenLibraries,
        onOpenFloweringSources = onOpenFloweringSources,
        onCrashReportingChange = viewModel::onCrashReportingChange,
        modifier = modifier,
    )
}

@Composable
fun AboutScreen(
    state: AboutUiState,
    onOpenUrl: (String) -> Unit,
    onWriteToUs: () -> Unit,
    onOpenLibraries: () -> Unit,
    onOpenFloweringSources: () -> Unit,
    onCrashReportingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(title = stringResource(R.string.about_title), modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).testTag(ABOUT_LIST_TAG),
            contentPadding = PaddingValues(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
            verticalArrangement = Arrangement.spacedBy(TreesSpacing.s),
        ) {
            item { AppCard(state) }
            section(R.string.about_section_contact) {
                InfoRow(
                    title = stringResource(R.string.about_contact_write),
                    subtitle = stringResource(R.string.about_contact_write_detail),
                    leadingIcon = TreesIcons.Mail,
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = onWriteToUs,
                )
                TreesDivider()
                InfoRow(
                    title = stringResource(R.string.about_contact_website),
                    subtitle = state.websiteLabel,
                    leadingIcon = TreesIcons.Website,
                    trailing = InfoRowTrailing.EXTERNAL,
                    onClick = { onOpenUrl(state.websiteUrl) },
                )
            }
            dataSections(state, onOpenUrl, onOpenFloweringSources)
            mapSection(onOpenUrl)
            privacySection(state.crashReportingEnabled, onCrashReportingChange, onOpenUrl)
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

/** The tree data and the flowering calendar, with their sources. */
private fun LazyListScope.dataSections(
    state: AboutUiState,
    onOpenUrl: (String) -> Unit,
    onOpenFloweringSources: () -> Unit,
) {
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
        TreesDivider()
        InfoRow(
            title = stringResource(R.string.about_data_gpso_credit),
            subtitle = stringResource(R.string.about_data_gpso_detail),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(GPSO_DATASET_URL) },
        )
    }
    section(R.string.about_section_flowering) {
        InfoRow(
            title = stringResource(R.string.about_flowering_credit),
            subtitle = stringResource(R.string.about_flowering_license),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(GUIDE_URL) },
        )
        TreesDivider()
        InfoRow(
            title =
                pluralStringResource(
                    R.plurals.about_flowering_sheets,
                    state.floweringSheetCount,
                    state.floweringSheetCount,
                ),
            subtitle = stringResource(R.string.about_flowering_sheets_detail),
            trailing = InfoRowTrailing.CHEVRON,
            onClick = onOpenFloweringSources,
        )
    }
}

/** The map's credits: the OpenStreetMap data, the OpenMapTiles schema and the OpenFreeMap tiles. */
private fun LazyListScope.mapSection(onOpenUrl: (String) -> Unit) {
    section(R.string.about_section_map) {
        InfoRow(
            title = stringResource(R.string.about_map_credit),
            subtitle = stringResource(R.string.about_map_data),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(OSM_COPYRIGHT_URL) },
        )
        TreesDivider()
        InfoRow(
            title = stringResource(R.string.about_map_tiles),
            subtitle = stringResource(R.string.about_map_tiles_detail),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(OPENMAPTILES_URL) },
        )
        TreesDivider()
        InfoRow(
            title = stringResource(R.string.about_map_host),
            subtitle = stringResource(R.string.about_map_host_detail),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(OPENFREEMAP_URL) },
        )
    }
}

/** The crash-report switch and the link to the privacy policy. */
private fun LazyListScope.privacySection(
    crashReportingEnabled: Boolean,
    onCrashReportingChange: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    section(R.string.about_section_privacy) {
        SwitchRow(
            title = stringResource(R.string.about_crash_reports),
            subtitle = stringResource(R.string.about_crash_reports_detail),
            checked = crashReportingEnabled,
            onCheckedChange = onCrashReportingChange,
        )
        TreesDivider()
        InfoRow(
            title = stringResource(R.string.about_privacy_policy),
            trailing = InfoRowTrailing.EXTERNAL,
            onClick = { onOpenUrl(PRIVACY_URL) },
        )
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
                    imageVector = TreesIcons.AppLogo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp),
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
            state =
                AboutUiState(
                    versionName = "0.1.0",
                    contactEmail = "arbresdeparis@gmail.com",
                    websiteUrl = "https://arbresdeparis.fr",
                    floweringSheetCount = 27,
                ),
            onOpenUrl = {},
            onWriteToUs = {},
            onOpenLibraries = {},
            onOpenFloweringSources = {},
            onCrashReportingChange = {},
        )
    }
}
