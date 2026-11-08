package com.treesofparis.feature.herbier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treesofparis.domain.usecase.GetFloweringSeasons
import com.treesofparis.domain.usecase.GetRemarkableTrees
import com.treesofparis.domain.usecase.GetTreeStatistics
import com.treesofparis.feature.herbier.mapper.toFloweringCalendarUi
import com.treesofparis.feature.herbier.mapper.toHerbierUi
import com.treesofparis.feature.herbier.mapper.toRemarkableAreasUi
import dagger.hilt.android.lifecycle.HiltViewModel
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
                val remarkable = getRemarkableTrees().toRemarkableAreasUi()
                emit(HerbierUiState.Content(statistics.toHerbierUi(), calendar, remarkable))
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HerbierUiState.Loading)

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
