package fr.arbresdeparis.feature.about

import app.cash.turbine.test
import fr.arbresdeparis.domain.usecase.ObserveCrashReportingAnswered
import fr.arbresdeparis.domain.usecase.SetCrashReporting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrashReportingQuestionViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private fun viewModel(crashReporting: FakeCrashReporting) =
        CrashReportingQuestionViewModel(
            ObserveCrashReportingAnswered(crashReporting),
            SetCrashReporting(crashReporting),
        )

    @Test
    fun notAnsweredYet_asksTheQuestion() =
        runTest(main.dispatcher) {
            viewModel(FakeCrashReporting(enabled = false, answered = false)).uiState.test {
                assertFalse(awaitItem().visible)
                assertTrue(awaitItem().visible)
            }
        }

    @Test
    fun alreadyAnswered_neverAsks() =
        runTest(main.dispatcher) {
            viewModel(FakeCrashReporting(enabled = false, answered = true)).uiState.test {
                assertFalse(awaitItem().visible)
                expectNoEvents()
            }
        }

    @Test
    fun yes_turnsReportsOnAndClosesTheQuestion() =
        runTest(main.dispatcher) {
            val crashReporting = FakeCrashReporting(enabled = false, answered = false)
            val viewModel = viewModel(crashReporting)
            viewModel.uiState.test {
                skipItems(1)
                assertTrue(awaitItem().visible)
                viewModel.onAnswer(send = true)
                assertFalse(awaitItem().visible)
                assertTrue(crashReporting.enabled.value)
            }
        }

    @Test
    fun no_keepsReportsOffAndClosesTheQuestion() =
        runTest(main.dispatcher) {
            val crashReporting = FakeCrashReporting(enabled = false, answered = false)
            val viewModel = viewModel(crashReporting)
            viewModel.uiState.test {
                skipItems(1)
                assertTrue(awaitItem().visible)
                viewModel.onAnswer(send = false)
                assertFalse(awaitItem().visible)
                assertFalse(crashReporting.enabled.value)
                assertTrue(crashReporting.answered.value)
            }
        }
}
