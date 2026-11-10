package fr.arbresdeparis.domain.model

/** [count] trees around [center] (their mean position): one disc on the map when it shows too many trees. */
data class TreeCluster(
    val center: GeoPoint,
    val count: Int,
)
