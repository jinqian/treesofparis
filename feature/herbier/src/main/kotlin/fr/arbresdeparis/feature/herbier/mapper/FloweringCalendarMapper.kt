package fr.arbresdeparis.feature.herbier.mapper

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.domain.format.formatMonth
import fr.arbresdeparis.domain.format.formatMonthRange
import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.feature.herbier.FloweringCalendarUi
import fr.arbresdeparis.feature.herbier.FloweringRowUi
import fr.arbresdeparis.feature.herbier.NextFloweringUi
import java.time.Month

/**
 * The calendar of the drawn genera in [now]. [plantingOrder] lists genera most planted first, so the season card
 * names the trees one is most likely to meet.
 */
fun List<FloweringSeason>.toFloweringCalendarUi(
    now: Month,
    plantingOrder: List<String>,
): FloweringCalendarUi {
    val drawn = filter { TreeLeaf.forGenus(it.genus) != TreeLeaf.GENERIC }
    val rank = { season: FloweringSeason -> plantingOrder.indexOf(season.genus).takeIf { it >= 0 } ?: Int.MAX_VALUE }
    val flowering = { month: Month -> drawn.filter { month in it.months }.sortedBy(rank) }
    val nextMonth =
        (1 until Month.entries.size)
            .map { now.plus(it.toLong()) }
            .firstOrNull { flowering(it).isNotEmpty() }
    return FloweringCalendarUi(
        currentMonth = now.value,
        currentMonthName = formatMonth(now),
        rows =
            drawn.map {
                FloweringRowUi(
                    leaf = TreeLeaf.forGenus(it.genus),
                    months = Month.entries.map { month -> month in it.months },
                    showyFlowers = it.showyFlowers,
                    range = formatMonthRange(it.months),
                )
            },
        inFlower = flowering(now).map { TreeLeaf.forGenus(it.genus) },
        next =
            nextMonth?.takeIf { flowering(now).isEmpty() }?.let { month ->
                NextFloweringUi(formatMonth(month), flowering(month).map { TreeLeaf.forGenus(it.genus) })
            },
    )
}
