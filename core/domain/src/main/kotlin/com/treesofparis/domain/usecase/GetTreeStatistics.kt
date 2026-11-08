package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.domain.repository.TreeStatisticsRepository
import javax.inject.Inject

/** Statistics for the Herbier screen, with the three most wooded arrondissements. */
class GetTreeStatistics
    @Inject
    constructor(
        private val statistics: TreeStatisticsRepository,
    ) {
        suspend operator fun invoke(): TreeStatistics = statistics.getStatistics(areaLimit = TOP_AREAS)

        private companion object {
            const val TOP_AREAS = 3
        }
    }
