package com.treesofparis.domain.model

/** A tree near a point, [distanceM] metres away from it. */
data class NearbyTree(
    val tree: Tree,
    val distanceM: Double,
)
