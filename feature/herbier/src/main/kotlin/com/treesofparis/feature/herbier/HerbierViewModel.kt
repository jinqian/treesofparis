package com.treesofparis.feature.herbier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treesofparis.domain.usecase.GetTreeStatistics
import com.treesofparis.feature.herbier.mapper.toHerbierUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HerbierViewModel
    @Inject
    constructor(
        getTreeStatistics: GetTreeStatistics,
    ) : ViewModel() {
        private val showAllGenera = MutableStateFlow(false)

        // The data ships with the app and never changes, so the figures are computed once per screen.
        private val herbier = flow { emit(getTreeStatistics().toHerbierUi()) }

        val uiState: StateFlow<HerbierUiState> =
            combine(herbier, showAllGenera) { content, showAll -> HerbierUiState.Content(content, showAll) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HerbierUiState.Loading)

        fun onShowAllGenera() {
            showAllGenera.value = true
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
