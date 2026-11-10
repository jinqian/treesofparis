package fr.arbresdeparis.feature.about

import fr.arbresdeparis.domain.format.formatMonthRange
import fr.arbresdeparis.domain.model.FloweringSheet

/** « Prunus avium · mars – avril · fleurs voyantes ». */
internal fun FloweringSheet.toUi(): FloweringSourceUi {
    val detail = listOfNotNull(scientificName, formatMonthRange(months), "fleurs voyantes".takeIf { showyFlowers })
    return FloweringSourceUi(commonName = commonName, detail = detail.joinToString(" · "), url = url)
}
