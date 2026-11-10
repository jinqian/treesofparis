package fr.arbresdeparis.feature.about

import fr.arbresdeparis.domain.format.formatParisDate
import fr.arbresdeparis.domain.model.DatasetInfo
import java.text.NumberFormat
import java.util.Locale

internal fun DatasetInfo.toExtractionUi(): ExtractionUi? =
    extractedAt?.let {
        ExtractionUi(
            date = formatParisDate(it),
            treeCount = NumberFormat.getIntegerInstance(Locale.FRANCE).format(treeCount),
        )
    }
