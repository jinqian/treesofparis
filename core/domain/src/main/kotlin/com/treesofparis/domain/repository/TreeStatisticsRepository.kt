package com.treesofparis.domain.repository

import com.treesofparis.domain.model.GenusSummary
import com.treesofparis.domain.model.TreeStatistics

interface TreeStatisticsRepository {
    /** Figures about the whole dataset, with the [areaLimit] most wooded Paris arrondissements. */
    suspend fun getStatistics(areaLimit: Int): TreeStatistics

    /** Every genus of the dataset, most planted first; trees without a genus are left out. */
    suspend fun getGenera(): List<GenusSummary>
}
