package com.treesofparis.feature.herbier

import com.treesofparis.core.designsystem.leaf.TreeLeaf

sealed interface HerbierUiState {
    data object Loading : HerbierUiState

    data class Content(
        val herbier: HerbierUi,
        /** The genus list shows the first [HerbierUi.COLLAPSED_GENERA] until the user asks for all. */
        val showAllGenera: Boolean = false,
    ) : HerbierUiState
}

/** Everything the Herbier screen shows, already formatted for French. */
data class HerbierUi(
    val totalTrees: String,
    val genusCount: String,
    val taxonCount: String,
    /** « 1 arbre sur 5 est un platane », or null when there is no data. */
    val headline: HeadlineUi?,
    /** Drawn genera only, most planted first. */
    val genera: List<GenusUi>,
    /** Known stages, youngest first. */
    val stages: List<StageUi>,
    val unknownStageCount: String?,
    val remarkable: RemarkableUi,
    val areas: List<AreaUi>,
) {
    companion object {
        const val COLLAPSED_GENERA = 10
    }
}

data class HeadlineUi(
    val leaf: TreeLeaf,
    /** One tree in [oneIn] belongs to [leaf]'s genus. */
    val oneIn: Int,
    val count: String,
    val runnerUp: GenusCountUi?,
)

data class GenusCountUi(
    val leaf: TreeLeaf,
    val count: String,
)

data class GenusUi(
    val leaf: TreeLeaf,
    /** Share of all trees: « 19,3 % ». */
    val share: String,
    val count: String,
    /** Bar length, relative to the most planted genus (1 for the first). */
    val fraction: Float,
)

data class StageUi(
    val label: String,
    val count: String,
    /** Bar length, relative to the most common stage. */
    val fraction: Float,
)

data class RemarkableUi(
    val count: String,
    val leading: LeadingGenusUi?,
    val tallest: RecordUi?,
    val widest: RecordUi?,
)

data class LeadingGenusUi(
    val leaf: TreeLeaf,
    val count: Int,
)

data class RecordUi(
    val name: String,
    /** « 45 m » for a height, « 7,40 m » for a girth. */
    val value: String,
    /** « Bois de Boulogne · Route du Lac A Bagatelle ». */
    val place: String,
)

data class AreaUi(
    val rank: Int,
    val name: String,
    val count: String,
)
