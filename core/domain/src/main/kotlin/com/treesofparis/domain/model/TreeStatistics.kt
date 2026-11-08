package com.treesofparis.domain.model

/** Figures about the whole tree dataset, for the Herbier screen. */
data class TreeStatistics(
    val total: Int,
    /** Distinct genera (trees without a genus are not counted). */
    val genusCount: Int,
    /** Distinct genus + species pairs, i.e. species and varieties. */
    val taxonCount: Int,
    /** Trees per genus, most planted first. */
    val genera: List<GenusCount>,
    /** Trees per development stage; a null stage means it is unknown in the dataset. */
    val stages: List<StageCount>,
    val remarkable: RemarkableStatistics,
    /** Paris arrondissements with the most trees, most first (raw dataset area names). */
    val mostWoodedAreas: List<AreaCount>,
) {
    /** Fraction of all trees that [genus] represents, 0 when there are no trees. */
    fun shareOf(genus: GenusCount): Double = if (total == 0) 0.0 else genus.count.toDouble() / total
}

data class GenusCount(
    val genus: String,
    val count: Int,
)

data class StageCount(
    val stage: String?,
    val count: Int,
)

data class AreaCount(
    val area: String,
    val count: Int,
)

/**
 * The remarkable trees: how many, the genus with the most of them, and the tallest and widest. Records only look at
 * remarkable trees, whose measurements the City checks; the full dataset contains obvious errors (a 112 m tree).
 */
data class RemarkableStatistics(
    val count: Int,
    val leadingGenus: GenusCount?,
    val tallest: Tree?,
    val widest: Tree?,
)
