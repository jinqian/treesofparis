package com.treesofparis.domain.repository

import com.treesofparis.domain.model.TreeStatistics

interface TreeStatisticsRepository {
    /** Figures about the whole dataset, with the [areaLimit] most wooded Paris arrondissements. */
    suspend fun getStatistics(areaLimit: Int): TreeStatistics
}
