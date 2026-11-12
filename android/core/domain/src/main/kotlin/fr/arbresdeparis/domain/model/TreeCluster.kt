package fr.arbresdeparis.domain.model

/** [count] trees around [center] (their mean position): one disc on the map when it shows too many trees. */
data class TreeCluster(
    val center: GeoPoint,
    val count: Int,
    /**
     * How big the group is next to the others of its zoom level, from 0 to 1: the square root of [count] over the
     * largest group's count, so a disc's area follows its trees.
     */
    val weight: Double = 1.0,
)
