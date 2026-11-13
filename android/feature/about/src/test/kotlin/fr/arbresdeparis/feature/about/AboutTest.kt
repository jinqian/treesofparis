package fr.arbresdeparis.feature.about

import android.app.Application
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.core.designsystem.theme.TreesTheme
import fr.arbresdeparis.domain.model.DatasetInfo
import fr.arbresdeparis.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class AboutTest {
    @get:Rule
    val compose = createComposeRule()

    private val state =
        AboutUiState(
            versionName = "0.1.0",
            contactEmail = "arbresdeparis@gmail.com",
            websiteUrl = "https://arbresdeparis.fr",
            extraction = ExtractionUi(date = "6 novembre 2026", treeCount = "220\u202F339"),
            floweringSheetCount = 27,
        )

    @Test
    fun extraction_isTheParisDate() {
        // 23:30 UTC on 5 November is already 6 November in Paris.
        val late = DatasetInfo(extractedAt = Instant.parse("2026-11-05T23:30:00Z"), treeCount = 1)
        assertEquals("6 novembre 2026", late.toExtractionUi()?.date)
        assertNull(DatasetInfo(extractedAt = null, treeCount = 1).toExtractionUi())
    }

    @Test
    fun aboutScreen_contact_writesToUsAndOpensTheWebsite() {
        val urls = mutableListOf<String>()
        var writes = 0
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = { urls += it },
                    onWriteToUs = { writes++ },
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose.onNodeWithText("CONTACT").assertIsDisplayed()
        compose.onNodeWithText("Nous écrire").performClick()
        compose.onNodeWithText("arbresdeparis.fr").performClick()
        assertEquals(1, writes)
        assertEquals(listOf("https://arbresdeparis.fr"), urls)
    }

    @Test
    fun appearance_showsTheChoiceAndSavesANewOne() {
        val choices = mutableListOf<ThemeMode>()
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state.copy(themeMode = ThemeMode.SYSTEM),
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                    onThemeModeChange = { choices += it },
                )
            }
        }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Sombre"))
        compose.onNodeWithText("APPARENCE").assertIsDisplayed()
        compose.onNodeWithText("Comme le téléphone").assertIsSelected()
        compose.onNodeWithText("Clair").assertIsNotSelected()
        compose.onNodeWithText("Sombre").assertIsNotSelected().performClick()
        assertEquals(listOf(ThemeMode.DARK), choices)
    }

    @Test
    fun writeToUs_opensTheEmailAppSignedWithTheVersion() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.writeToUs("arbresdeparis@gmail.com", "0.1.0")

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("Arbres de Paris", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        val body = intent.getStringExtra(Intent.EXTRA_TEXT)!!
        assertTrue(body, body.startsWith("Bonjour,"))
        assertTrue(body, body.contains("Arbres de Paris 0.1.0 · Android "))
    }

    @Test
    fun aboutScreen_showsTheDataExtraction() {
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose
            .onNodeWithTag(
                ABOUT_LIST_TAG,
            ).performScrollToNode(hasText("Extraction du 6 novembre 2026 · 220\u202F339 arbres"))
        compose.onNodeWithText("Extraction du 6 novembre 2026 · 220\u202F339 arbres").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_showsVersionAndCredits() {
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose.onNodeWithText("VERSION 0.1.0").assertIsDisplayed()
        compose
            .onNodeWithTag(
                ABOUT_LIST_TAG,
            ).performScrollToNode(hasText("« Les arbres », Paris Data — Ville de Paris"))
        compose.onNodeWithText("« Les arbres », Paris Data — Ville de Paris").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_showsOsmAttribution() {
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("© les contributeurs d’OpenStreetMap"))
        compose.onNodeWithText("© les contributeurs d’OpenStreetMap").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_creditsTheMapDataTilesAndHost() {
        val urls = mutableListOf<String>()
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = { urls += it },
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        listOf("© les contributeurs d’OpenStreetMap", "© OpenMapTiles", "OpenFreeMap").forEach {
            compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText(it))
            compose.onNodeWithText(it).performClick()
        }
        assertEquals(
            listOf(
                "https://www.openstreetmap.org/copyright",
                "https://www.openmaptiles.org/",
                "https://openfreemap.org",
            ),
            urls,
        )
    }

    @Test
    fun aboutScreen_creditsBundledFonts() {
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Newsreader et Inter"))
        compose.onNodeWithText("SIL Open Font License 1.1").assertIsDisplayed()
    }

    @Test
    fun aboutScreen_creditsTheGuideAndOpensItsSheets() {
        val urls = mutableListOf<String>()
        var opened = 0
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = { urls += it },
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = { opened++ },
                    onCrashReportingChange = {},
                )
            }
        }
        val guide = "« Guide des Essences de Paris » — Ville de Paris, Cerema"
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText(guide))
        compose.onNodeWithText(guide).performClick()
        assertEquals(
            listOf(
                "https://opendata.paris.fr/explore/dataset/fiches-essences-du-guide-des-essences-de-paris/information/",
            ),
            urls,
        )
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Les 27 fiches consultées"))
        compose.onNodeWithText("Les 27 fiches consultées").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun aboutScreen_opensTheLibrariesScreen() {
        var opened = 0
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state,
                    onOpenUrl = {},
                    onWriteToUs = {},
                    onOpenLibraries = { opened++ },
                    onOpenFloweringSources = {},
                    onCrashReportingChange = {},
                )
            }
        }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Bibliothèques open source"))
        compose.onNodeWithText("Bibliothèques open source").performClick()
        assertEquals(1, opened)
        compose.onNodeWithText("MapLibre Native").assertDoesNotExist()
    }

    @Test
    fun librariesScreen_listsLibrariesAndOpensTheirPage() {
        var opened: String? = null
        val libs =
            listOf(LibraryUi(name = "MapLibre Native", detail = "13.7.1 · BSD 2-Clause", url = "https://maplibre.org"))
        compose.setContent {
            TreesTheme { LibrariesScreen(libraries = libs, onOpenUrl = { opened = it }, onBack = {}) }
        }
        compose.onNodeWithText("Bibliothèques open source").assertIsDisplayed()
        compose.onNodeWithTag(LIBRARIES_LIST_TAG).performScrollToNode(hasText("MapLibre Native"))
        compose.onNodeWithText("MapLibre Native").performClick()
        assertEquals("https://maplibre.org", opened)
    }

    @Test
    fun librariesScreen_backButtonGoesBack() {
        var backs = 0
        compose.setContent {
            TreesTheme {
                LibrariesScreen(
                    libraries = emptyList(),
                    onOpenUrl = {},
                    onBack = { backs++ },
                )
            }
        }
        compose.onNodeWithContentDescription("Retour").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun aboutScreen_privacy_switchesCrashReportsAndOpensThePolicy() {
        val choices = mutableListOf<Boolean>()
        val urls = mutableListOf<String>()
        compose.setContent {
            TreesTheme {
                AboutScreen(
                    state.copy(crashReportingEnabled = true),
                    onOpenUrl = { urls += it },
                    onWriteToUs = {},
                    onOpenLibraries = {},
                    onOpenFloweringSources = {},
                    onCrashReportingChange = { choices += it },
                )
            }
        }
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Envoyer les rapports de plantage"))
        compose.onNodeWithText("Envoyer les rapports de plantage").assertIsOn().performClick()
        compose.onNodeWithTag(ABOUT_LIST_TAG).performScrollToNode(hasText("Politique de confidentialité"))
        compose.onNodeWithText("Politique de confidentialité").performClick()
        assertEquals(listOf(false), choices)
        assertEquals(listOf("https://arbresdeparis.fr/confidentialite/"), urls)
    }
}
