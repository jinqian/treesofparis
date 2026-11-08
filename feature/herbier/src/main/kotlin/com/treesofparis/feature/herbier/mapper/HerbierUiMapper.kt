package com.treesofparis.feature.herbier.mapper

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.domain.format.formatArea
import com.treesofparis.domain.format.formatPlace
import com.treesofparis.domain.model.RemarkableStatistics
import com.treesofparis.domain.model.StageCount
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.feature.herbier.AreaUi
import com.treesofparis.feature.herbier.GenusCountUi
import com.treesofparis.feature.herbier.GenusUi
import com.treesofparis.feature.herbier.HeadlineUi
import com.treesofparis.feature.herbier.HerbierUi
import com.treesofparis.feature.herbier.LeadingGenusUi
import com.treesofparis.feature.herbier.RecordUi
import com.treesofparis.feature.herbier.RemarkableUi
import com.treesofparis.feature.herbier.StageUi
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val FRENCH = Locale.FRANCE

/** Dataset stages, youngest first. Unexpected stages follow, in dataset order. */
private val STAGE_ORDER = listOf("Jeune", "Jeune adulte", "Adulte", "Mature")

private const val PERCENT = 100.0
private const val CM_PER_M = 100.0

internal fun formatCount(count: Int): String = NumberFormat.getIntegerInstance(FRENCH).format(count)

fun TreeStatistics.toHerbierUi(): HerbierUi {
    val drawn = genera.filter { TreeLeaf.forGenus(it.genus) != TreeLeaf.GENERIC }
    val first = drawn.firstOrNull()?.count?.coerceAtLeast(1) ?: 1
    return HerbierUi(
        totalTrees = formatCount(total),
        genusCount = formatCount(genusCount),
        taxonCount = formatCount(taxonCount),
        headline =
            drawn.firstOrNull()?.takeIf { total > 0 }?.let { leader ->
                HeadlineUi(
                    leaf = TreeLeaf.forGenus(leader.genus),
                    oneIn = (total.toDouble() / leader.count).roundToInt(),
                    count = formatCount(leader.count),
                    runnerUp =
                        drawn
                            .getOrNull(
                                1,
                            )?.let { GenusCountUi(TreeLeaf.forGenus(it.genus), formatCount(it.count)) },
                )
            },
        genera =
            drawn.map {
                GenusUi(
                    leaf = TreeLeaf.forGenus(it.genus),
                    genus = it.genus,
                    share = String.format(FRENCH, "%.1f %%", shareOf(it) * PERCENT),
                    count = formatCount(it.count),
                    fraction = it.count.toFloat() / first,
                )
            },
        stages = stages.toStageUi(),
        unknownStageCount =
            stages
                .filter { it.stage == null }
                .sumOf { it.count }
                .takeIf { it > 0 }
                ?.let(::formatCount),
        remarkable = remarkable.toRemarkableUi(),
        areas =
            mostWoodedAreas.mapIndexed {
                index,
                area,
                ->
                AreaUi(index + 1, formatArea(area.area), formatCount(area.count))
            },
    )
}

private fun List<StageCount>.toStageUi(): List<StageUi> {
    val known =
        filter { it.stage != null }.sortedBy {
            STAGE_ORDER.indexOf(it.stage).let { i ->
                if (i <
                    0
                ) {
                    Int.MAX_VALUE
                } else {
                    i
                }
            }
        }
    val max = known.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    return known.map { StageUi(it.stage!!, formatCount(it.count), it.count.toFloat() / max) }
}

private fun RemarkableStatistics.toRemarkableUi() =
    RemarkableUi(
        count = formatCount(count),
        leading = leadingGenus?.let { LeadingGenusUi(TreeLeaf.forGenus(it.genus), it.count) },
        tallest = tallest?.let { tree -> tree.heightM?.let { tree.toRecord("$it m") } },
        widest =
            widest?.let { tree ->
                tree.circumferenceCm?.let { tree.toRecord(String.format(FRENCH, "%.2f m", it / CM_PER_M)) }
            },
    )

private fun Tree.toRecord(value: String) =
    RecordUi(name = commonName, value = value, place = "${formatArea(arrondissement)} · ${shortPlace(address)}")

/**
 * « PARC DES BUTTES CHAUMONT / 7 RUE BOTZARIS, 19-08 » → « Parc des Buttes Chaumont »: the place, without the street
 * or the plot code.
 */
private fun shortPlace(address: String): String = formatPlace(address.substringBefore(" / ").substringBefore(","))
