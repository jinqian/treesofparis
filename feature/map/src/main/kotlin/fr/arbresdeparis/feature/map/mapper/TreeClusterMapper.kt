package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.domain.model.TreeCluster
import java.text.NumberFormat
import java.util.Locale

/** A group for the map, labelled with its count in French (« 1 234 », with a no-break space). */
fun TreeCluster.toMapCluster(id: Int) =
    MapCluster(
        id = id.toLong(),
        position = center,
        count = count,
        label = FRENCH_INTEGER.format(count).replace(NARROW_NO_BREAK_SPACE, NO_BREAK_SPACE),
        weight = weight,
    )

private val FRENCH_INTEGER: NumberFormat = NumberFormat.getIntegerInstance(Locale.FRANCE)

// The map's label font has the plain no-break space; French formatting uses the narrow one.
private const val NARROW_NO_BREAK_SPACE = '\u202F'
private const val NO_BREAK_SPACE = '\u00A0'
