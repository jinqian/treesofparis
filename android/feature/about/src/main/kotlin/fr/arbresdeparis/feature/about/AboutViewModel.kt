package fr.arbresdeparis.feature.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.model.AppInfo
import fr.arbresdeparis.domain.model.DatasetInfo
import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.usecase.GetFloweringSources
import fr.arbresdeparis.domain.usecase.ObserveCrashReporting
import fr.arbresdeparis.domain.usecase.ObserveThemeMode
import fr.arbresdeparis.domain.usecase.SetCrashReporting
import fr.arbresdeparis.domain.usecase.SetThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
        observeThemeMode: ObserveThemeMode,
        private val setThemeMode: SetThemeMode,
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
            combine(observeCrashReporting(), observeThemeMode()) { crashReporting, themeMode ->
                initial.copy(crashReportingEnabled = crashReporting, themeMode = themeMode)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initial)

        fun onCrashReportingChange(enabled: Boolean) {
            viewModelScope.launch { setCrashReporting(enabled) }
        }

        fun onThemeModeChange(mode: ThemeMode) {
            viewModelScope.launch { setThemeMode(mode) }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
