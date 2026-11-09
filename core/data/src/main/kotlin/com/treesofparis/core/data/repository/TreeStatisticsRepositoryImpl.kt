package com.treesofparis.core.data.repository

import com.treesofparis.core.data.db.GenusNameCountRow
import com.treesofparis.core.data.db.TreeStatisticsDao
import com.treesofparis.core.data.mapper.toDomain
import com.treesofparis.domain.model.AreaCount
import com.treesofparis.domain.model.GenusCount
import com.treesofparis.domain.model.GenusSummary
import com.treesofparis.domain.model.RemarkableStatistics
import com.treesofparis.domain.model.StageCount
import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates over the pre-packaged tree database. The data is read-only, so the figures never go stale: they are
 * computed once per [areaLimit] and kept, as every Herbier screen asks for them.
 */
@Singleton
internal class TreeStatisticsRepositoryImpl
    @Inject
    constructor(
        private val dao: TreeStatisticsDao,
    ) : TreeStatisticsRepository {
        private val mutex = Mutex()
        private val cache = mutableMapOf<Int, TreeStatistics>()
        private var genera: List<GenusSummary>? = null

        override suspend fun getStatistics(areaLimit: Int): TreeStatistics =
            mutex.withLock { cache.getOrPut(areaLimit) { compute(areaLimit) } }

        override suspend fun getGenera(): List<GenusSummary> =
            mutex.withLock { genera ?: summarizeGenera(dao.countByGenusAndName()).also { genera = it } }

        private suspend fun compute(areaLimit: Int): TreeStatistics {
            val genera = dao.countByGenus().map { GenusCount(it.genus, it.count) }
            return TreeStatistics(
                total = dao.count(),
                genusCount = genera.size,
                taxonCount = dao.countTaxa(),
                genera = genera,
                stages = dao.countByStage().map { StageCount(it.stage?.takeIf(String::isNotBlank), it.count) },
                remarkable =
                    RemarkableStatistics(
                        count = dao.countRemarkable(),
                        leadingGenus = dao.leadingRemarkableGenus()?.let { GenusCount(it.genus, it.count) },
                        tallest = dao.tallestRemarkable()?.toDomain(),
                        widest = dao.widestRemarkable()?.toDomain(),
                    ),
                mostWoodedAreas = dao.mostWoodedAreas(areaLimit).map { AreaCount(it.area, it.count) },
            )
        }
    }

/** One summary per genus, named by the French name most of its trees carry; most planted first, then by name. */
private fun summarizeGenera(rows: List<GenusNameCountRow>): List<GenusSummary> =
    rows
        .groupBy { it.genus }
        .map { (genus, names) ->
            GenusSummary(
                genus = genus,
                commonName =
                    names
                        .maxWith(compareBy<GenusNameCountRow> { it.count }.thenByDescending { it.commonName })
                        .commonName,
                treeCount = names.sumOf { it.count },
                remarkableCount = names.sumOf { it.remarkableCount },
            )
        }.sortedWith(compareByDescending<GenusSummary> { it.treeCount }.thenBy { it.genus })
