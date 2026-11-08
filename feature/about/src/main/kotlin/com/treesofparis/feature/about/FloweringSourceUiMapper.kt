package com.treesofparis.feature.about

import com.treesofparis.domain.format.formatMonthRange
import com.treesofparis.domain.model.FloweringSheet

/** « Prunus avium · mars – avril · fleurs voyantes ». */
internal fun FloweringSheet.toUi(): FloweringSourceUi {
    val detail = listOfNotNull(scientificName, formatMonthRange(months), "fleurs voyantes".takeIf { showyFlowers })
    return FloweringSourceUi(commonName = commonName, detail = detail.joinToString(" · "), url = url)
}
