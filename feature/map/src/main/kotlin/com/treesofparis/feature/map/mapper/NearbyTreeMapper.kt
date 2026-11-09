package com.treesofparis.feature.map.mapper

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.domain.model.NearbyTree
import com.treesofparis.feature.map.NearbyTreeUi
import java.util.Locale
import kotlin.math.roundToInt

fun NearbyTree.toNearbyUi() =
    NearbyTreeUi(
        id = tree.id,
        name = tree.commonName,
        scientificName = tree.scientificName(),
        distance = formatDistance(distanceM),
        location = tree.location,
        leaf = TreeLeaf.forGenus(tree.genus),
    )

private const val METRES_PER_KM = 1_000

/** « 5 m » under a kilometre, then « 1,2 km »; a non-breaking space keeps the unit with its figure. */
internal fun formatDistance(metres: Double): String {
    val rounded = metres.roundToInt()
    return if (rounded < METRES_PER_KM) {
        "$rounded m"
    } else {
        String.format(Locale.FRANCE, "%.1f km", metres / METRES_PER_KM)
    }
}
