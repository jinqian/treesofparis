package fr.arbresdeparis.domain.model

import java.time.Month

/**
 * When a genus flowers in Paris: the [months] of its guide sheets together, [showyFlowers] if any of them is an
 * « arbre à fleurs ». Approximate: a flowering moves by a few weeks from one year to the next.
 */
data class FloweringSeason(
    val genus: String,
    val months: Set<Month>,
    val showyFlowers: Boolean,
) {
    /** Whole months from [now] to the next flowering: 0 while it flowers. */
    fun monthsUntilFlowering(now: Month): Int = months.minOf { (it.value - now.value + MONTHS) % MONTHS }

    private companion object {
        const val MONTHS = 12
    }
}

/** One season per genus, in the order of the year (earliest first month, then earliest last month). */
fun List<FloweringSheet>.toSeasons(): List<FloweringSeason> =
    groupBy { it.genus }
        .map { (genus, sheets) ->
            FloweringSeason(
                genus = genus,
                months = sheets.flatMap { it.months }.toSet(),
                showyFlowers = sheets.any { it.showyFlowers },
            )
        }.sortedWith(compareBy({ it.months.min() }, { it.months.max() }, { it.genus }))
