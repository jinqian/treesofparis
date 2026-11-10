package fr.arbresdeparis.feature.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.model.AppInfo
import fr.arbresdeparis.domain.model.DatasetInfo
import fr.arbresdeparis.domain.usecase.GetFloweringSources
import fr.arbresdeparis.domain.usecase.ObserveCrashReporting
import fr.arbresdeparis.domain.usecase.SetCrashReporting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
    @Inject
    constructor(
        appInfo: AppInfo,
        datasetInfo: DatasetInfo,
        getFloweringSources: GetFloweringSources,
        observeCrashReporting: ObserveCrashReporting,
        private val setCrashReporting: SetCrashReporting,
    ) : ViewModel() {
        private val initial =
            AboutUiState(
                versionName = appInfo.versionName,
                contactEmail = appInfo.contactEmail,
                websiteUrl = appInfo.websiteUrl,
                extraction = datasetInfo.toExtractionUi(),
                floweringSheetCount = getFloweringSources().size,
            )

        val uiState: StateFlow<AboutUiState> =
            observeCrashReporting()
                .map { initial.copy(crashReportingEnabled = it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initial)

        fun onCrashReportingChange(enabled: Boolean) {
            viewModelScope.launch { setCrashReporting(enabled) }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
