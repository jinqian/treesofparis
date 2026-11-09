package fr.arbresdeparis.feature.herbier

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf

sealed interface HerbierUiState {
    data object Loading : HerbierUiState

    data class Content(
        val herbier: HerbierUi,
        val flowering: FloweringCalendarUi,
        /** Every remarkable tree, by area. */
        val remarkableTrees: List<RemarkableAreaUi> = emptyList(),
    ) : HerbierUiState
}

/** The level-2 screens of the Herbier, one per card of the home screen. */
enum class HerbierSection { GENERA, FLOWERING, REMARKABLE, AGE_AND_AREAS }

/** Everything the Herbier screen shows, already formatted for French. */
data class HerbierUi(
    val totalTrees: String,
    val genusCount: String,
    val taxonCount: String,
    /** « 1 arbre sur 5 est un platane », or null when there is no data. */
    val headline: HeadlineUi?,
    /** Drawn genera only, most planted first. */
    val genera: List<GenusUi>,
    /** Share of all trees in a drawn genus: « 85 % ». */
    val drawnShare: String,
    /** Known stages, youngest first. */
    val stages: List<StageUi>,
    val unknownStageCount: String?,
    val remarkable: RemarkableUi,
    val areas: List<AreaUi>,
)

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
    /** The dataset genus (« Tilia »), to open it on the map. */
    val genus: String,
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
    /** « Bois de Boulogne · Route du Lac à Bagatelle ». */
    val place: String,
)

data class AreaUi(
    val rank: Int,
    val name: String,
    val count: String,
)

/** The flowering calendar of the drawn genera, seen from [currentMonth]. */
data class FloweringCalendarUi(
    /** 1 for January. */
    val currentMonth: Int,
    /** « novembre ». */
    val currentMonthName: String,
    /** In the order of the year. */
    val rows: List<FloweringRowUi>,
    /** The genera in flower this month, most planted first. */
    val inFlower: List<TreeLeaf>,
    /** When nothing flowers this month: the next month with flowers. */
    val next: NextFloweringUi?,
)

data class FloweringRowUi(
    val leaf: TreeLeaf,
    /** Twelve flags, January first. */
    val months: List<Boolean>,
    val showyFlowers: Boolean,
    /** « mai – juin ». */
    val range: String,
)

/** « Prochaines floraisons dès février : noisetier et orme ». */
data class NextFloweringUi(
    val monthName: String,
    val leaves: List<TreeLeaf>,
)

/** « Paris 6e » and its remarkable trees. */
data class RemarkableAreaUi(
    val name: String,
    val trees: List<RemarkableTreeUi>,
)

/** One remarkable tree: tapping it shows it on the map. */
data class RemarkableTreeUi(
    val id: Long,
    val name: String,
    /** « Parc Monceau · 22 m ». */
    val detail: String,
)
