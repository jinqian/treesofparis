package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.TreeStatistics
import fr.arbresdeparis.domain.repository.TreeStatisticsRepository
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
