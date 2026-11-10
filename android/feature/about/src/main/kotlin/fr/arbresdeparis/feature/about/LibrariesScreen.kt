package fr.arbresdeparis.feature.about

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import fr.arbresdeparis.core.designsystem.component.InfoRow
import fr.arbresdeparis.core.designsystem.component.InfoRowTrailing
import fr.arbresdeparis.core.designsystem.component.LoadingState
import fr.arbresdeparis.core.designsystem.component.TreesCardGroup
import fr.arbresdeparis.core.designsystem.component.TreesDivider
import fr.arbresdeparis.core.designsystem.component.TreesScaffold
import fr.arbresdeparis.core.designsystem.theme.TreesSpacing
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val LIBRARIES_LIST_TAG = "libraries_list"

/**
 * The open source libraries, reached from À propos. [librariesResId] is the `R.raw.aboutlibraries` resource that the
 * AboutLibraries Gradle plugin generates in :app (the only module that sees every dependency).
 */
@Composable
fun LibrariesRoute(
    librariesResId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
            libs?.libraries?.map { it.toUi() }?.sortedBy { it.name.lowercase() }
        }
    val uriHandler = LocalUriHandler.current
    LibrariesScreen(libraries = libraries, onOpenUrl = uriHandler::openUri, onBack = onBack, modifier = modifier)
}

/** [libraries] is null while they load. */
@Composable
fun LibrariesScreen(
    libraries: List<LibraryUi>?,
    onOpenUrl: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TreesScaffold(
        title = stringResource(R.string.about_section_libraries),
        onBack = onBack,
        modifier = modifier,
    ) { padding ->
        if (libraries == null) {
            LoadingState(Modifier.padding(padding))
            return@TreesScaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).testTag(LIBRARIES_LIST_TAG),
            contentPadding = PaddingValues(start = TreesSpacing.m, end = TreesSpacing.m, bottom = TreesSpacing.l),
        ) {
            item {
                TreesCardGroup {
                    libraries.forEachIndexed { index, library ->
                        LibraryRow(library, onOpenUrl, divider = index < libraries.lastIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    library: LibraryUi,
    onOpenUrl: (String) -> Unit,
    divider: Boolean,
) {
    InfoRow(
        title = library.name,
        subtitle = library.detail,
        trailing = if (library.url != null) InfoRowTrailing.EXTERNAL else InfoRowTrailing.NONE,
        onClick = library.url?.let { url -> { onOpenUrl(url) } },
    )
    if (divider) TreesDivider()
}

@PreviewLightDark
@Composable
private fun LibrariesScreenPreview() {
    TreesTheme {
        LibrariesScreen(
            libraries =
                listOf(
                    LibraryUi("AboutLibraries", "13.1.0 · Apache License 2.0", null),
                    LibraryUi("MapLibre Native", "13.7.1 · BSD 2-Clause", null),
                ),
            onOpenUrl = {},
            onBack = {},
        )
    }
}
