package fr.arbresdeparis.feature.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.usecase.ObserveCrashReportingAnswered
import fr.arbresdeparis.domain.usecase.SetCrashReporting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** « Envoyer les rapports de plantage ? », asked once at first launch (ADR 0007). */
data class CrashReportingQuestionUiState(
    val visible: Boolean = false,
)

@HiltViewModel
class CrashReportingQuestionViewModel
    @Inject
    constructor(
        observeAnswered: ObserveCrashReportingAnswered,
        private val setCrashReporting: SetCrashReporting,
    ) : ViewModel() {
        /** Hidden until the saved answer is read, so it never flashes for someone who already answered. */
        val uiState: StateFlow<CrashReportingQuestionUiState> =
            observeAnswered()
                .map { answered -> CrashReportingQuestionUiState(visible = !answered) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                    CrashReportingQuestionUiState(),
                )

        fun onAnswer(send: Boolean) {
            viewModelScope.launch { setCrashReporting(send) }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
