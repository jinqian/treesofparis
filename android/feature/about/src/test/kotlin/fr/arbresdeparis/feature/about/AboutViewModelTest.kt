package fr.arbresdeparis.feature.about

import app.cash.turbine.test
import fr.arbresdeparis.domain.model.AppInfo
import fr.arbresdeparis.domain.model.DatasetInfo
import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.repository.CrashReportingRepository
import fr.arbresdeparis.domain.repository.FloweringRepository
import fr.arbresdeparis.domain.repository.ThemeRepository
import fr.arbresdeparis.domain.usecase.GetFloweringSources
import fr.arbresdeparis.domain.usecase.ObserveCrashReporting
import fr.arbresdeparis.domain.usecase.ObserveThemeMode
import fr.arbresdeparis.domain.usecase.SetCrashReporting
import fr.arbresdeparis.domain.usecase.SetThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.Month

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeThemeRepository(
    mode: ThemeMode = ThemeMode.SYSTEM,
) : ThemeRepository {
    val mode = MutableStateFlow(mode)

    override fun observeThemeMode() = mode

    override suspend fun setThemeMode(mode: ThemeMode) {
        this.mode.value = mode
    }
}

class FakeCrashReporting(
    enabled: Boolean = false,
    answered: Boolean = true,
) : CrashReportingRepository {
    val enabled = MutableStateFlow(enabled)
    val answered = MutableStateFlow(answered)

    override fun observeEnabled() = enabled

    override fun observeAnswered() = answered

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled.value = enabled
        answered.value = true
    }
}

private object OneSheet : FloweringRepository {
    override fun sheets() =
        listOf(FloweringSheet("Tilia", "Tilia tomentosa", "Tilleul argenté", setOf(Month.JUNE), false, "https://x"))
}

class AboutViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val appInfo = AppInfo("0.1.0", "arbresdeparis@gmail.com", "https://arbresdeparis.fr")
    private val dataset = DatasetInfo(extractedAt = Instant.parse("2026-11-06T16:30:03Z"), treeCount = 220_339)

    private fun viewModel(
        crashReporting: FakeCrashReporting,
        theme: FakeThemeRepository = FakeThemeRepository(),
    ) = AboutViewModel(
        appInfo,
        dataset,
        GetFloweringSources(OneSheet),
        ObserveCrashReporting(crashReporting),
        SetCrashReporting(crashReporting),
        ObserveThemeMode(theme),
        SetThemeMode(theme),
    )

    @Test
    fun exposesVersionAndExtraction() {
        val ui = viewModel(FakeCrashReporting()).uiState.value
        assertEquals("0.1.0", ui.versionName)
        assertEquals("arbresdeparis@gmail.com", ui.contactEmail)
        assertEquals("arbresdeparis.fr", ui.websiteLabel)
        assertEquals(1, ui.floweringSheetCount)
        assertEquals(ExtractionUi(date = "6 novembre 2026", treeCount = "220 339"), ui.extraction)
    }

    @Test
    fun crashReporting_showsTheSavedChoice() =
        runTest(main.dispatcher) {
            viewModel(FakeCrashReporting(enabled = true)).uiState.test {
                assertFalse(awaitItem().crashReportingEnabled)
                assertTrue(awaitItem().crashReportingEnabled)
            }
        }

    @Test
    fun crashReporting_switchSavesTheChoice() =
        runTest(main.dispatcher) {
            val crashReporting = FakeCrashReporting(enabled = true)
            val viewModel = viewModel(crashReporting)
            viewModel.uiState.test {
                skipItems(1)
                assertTrue(awaitItem().crashReportingEnabled)
                viewModel.onCrashReportingChange(false)
                assertFalse(awaitItem().crashReportingEnabled)
                assertFalse(crashReporting.enabled.value)
            }
        }

    @Test
    fun theme_showsTheSavedChoice() =
        runTest(main.dispatcher) {
            viewModel(FakeCrashReporting(), FakeThemeRepository(ThemeMode.DARK)).uiState.test {
                assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
                assertEquals(ThemeMode.DARK, awaitItem().themeMode)
            }
        }

    @Test
    fun theme_choiceIsSaved() =
        runTest(main.dispatcher) {
            val theme = FakeThemeRepository()
            val viewModel = viewModel(FakeCrashReporting(), theme)
            viewModel.uiState.test {
                assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
                viewModel.onThemeModeChange(ThemeMode.LIGHT)
                assertEquals(ThemeMode.LIGHT, awaitItem().themeMode)
                assertEquals(ThemeMode.LIGHT, theme.mode.value)
            }
        }
}
