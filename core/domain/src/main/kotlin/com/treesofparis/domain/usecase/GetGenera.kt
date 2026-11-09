package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GenusSummary
import com.treesofparis.domain.repository.TreeStatisticsRepository
import javax.inject.Inject

/**
 * The genera the map can filter on, most planted first. With [remarkableOnly], only those with remarkable trees, most
 * remarkable trees first: the map then shows remarkable trees only.
 */
class GetGenera
    @Inject
    constructor(
        private val statistics: TreeStatisticsRepository,
    ) {
        suspend operator fun invoke(remarkableOnly: Boolean): List<GenusSummary> {
            val genera = statistics.getGenera()
            if (!remarkableOnly) return genera
            return genera.filter { it.remarkableCount > 0 }.sortedByDescending { it.remarkableCount }
        }
    }
