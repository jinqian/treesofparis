package com.treesofparis.core.data.repository

import com.treesofparis.core.data.db.TreeStatisticsDao
import com.treesofparis.core.data.mapper.toDomain
import com.treesofparis.domain.model.AreaCount
import com.treesofparis.domain.model.GenusCount
import com.treesofparis.domain.model.RemarkableStatistics
import com.treesofparis.domain.model.StageCount
import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.domain.repository.TreeStatisticsRepository
import javax.inject.Inject

/** Aggregates over the pre-packaged tree database. The data is read-only, so the figures never go stale. */
internal class TreeStatisticsRepositoryImpl
    @Inject
    constructor(
        private val dao: TreeStatisticsDao,
    ) : TreeStatisticsRepository {
        override suspend fun getStatistics(areaLimit: Int): TreeStatistics {
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
