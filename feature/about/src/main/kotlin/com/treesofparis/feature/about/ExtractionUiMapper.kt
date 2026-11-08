package com.treesofparis.feature.about

import com.treesofparis.domain.model.DatasetInfo
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Paris time: an extraction late in the evening UTC already belongs to the next day in Paris. */
private val PARIS = ZoneId.of("Europe/Paris")
private val DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

internal fun DatasetInfo.toExtractionUi(): ExtractionUi? =
    extractedAt?.let {
        ExtractionUi(
            date = DATE.format(it.atZone(PARIS)),
            treeCount = NumberFormat.getIntegerInstance(Locale.FRANCE).format(treeCount),
        )
    }
