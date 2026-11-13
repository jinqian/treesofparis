package fr.arbresdeparis.feature.herbier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.arbresdeparis.domain.usecase.GetFloweringSeasons
import fr.arbresdeparis.domain.usecase.GetRemarkableStories
import fr.arbresdeparis.domain.usecase.GetRemarkableTrees
import fr.arbresdeparis.domain.usecase.GetTreeStatistics
import fr.arbresdeparis.feature.herbier.mapper.toFloweringCalendarUi
import fr.arbresdeparis.feature.herbier.mapper.toHerbierUi
import fr.arbresdeparis.feature.herbier.mapper.toRemarkableAreasUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Shared by the Herbier home and its level-2 screens: each entry has its own, the figures are cached below. */
@HiltViewModel
class HerbierViewModel
    @Inject
    constructor(
        getTreeStatistics: GetTreeStatistics,
        getFloweringSeasons: GetFloweringSeasons,
        getRemarkableTrees: GetRemarkableTrees,
        getRemarkableStories: GetRemarkableStories,
        clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<HerbierUiState> =
            flow {
                val statistics = getTreeStatistics()
                val calendar =
                    getFloweringSeasons().toFloweringCalendarUi(
                        now = LocalDate.now(clock).month,
                        plantingOrder = statistics.genera.map { it.genus },
                    )
                val remarkable = getRemarkableTrees().toRemarkableAreasUi(getRemarkableStories())
                emit(HerbierUiState.Content(statistics.toHerbierUi(), calendar, remarkable))
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HerbierUiState.Loading)

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
