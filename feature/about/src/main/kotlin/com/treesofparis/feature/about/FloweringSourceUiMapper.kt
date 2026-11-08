package com.treesofparis.feature.about

import com.treesofparis.domain.model.FloweringSheet
import java.time.format.TextStyle
import java.util.Locale

/** « Prunus avium · mars – avril · fleurs voyantes ». The guide's months are always one run. */
internal fun FloweringSheet.toUi(): FloweringSourceUi {
    val sorted = months.sorted()
    val names =
        listOf(
            sorted.first(),
            sorted.last(),
        ).distinct().map { it.getDisplayName(TextStyle.FULL, Locale.FRENCH) }
    val detail = listOfNotNull(scientificName, names.joinToString(" – "), "fleurs voyantes".takeIf { showyFlowers })
    return FloweringSourceUi(commonName = commonName, detail = detail.joinToString(" · "), url = url)
}
