package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.NearbyTree
import com.treesofparis.domain.repository.TreeRepository
import javax.inject.Inject

/**
 * The [count] trees closest to a point, closest first, among remarkable trees only or one genus when asked. Searches a
 * small box first and widens it while it holds fewer than [count] trees; empty when none is within the last radius.
 */
class GetNearestTrees
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(
            from: GeoPoint,
            count: Int,
            remarkableOnly: Boolean = false,
            genus: String? = null,
        ): List<NearbyTree> {
            var found = emptyList<NearbyTree>()
            for (radius in SEARCH_RADII_M) {
                found =
                    trees
                        .getTreesInBounds(GeoBounds.around(from, radius), CANDIDATES, remarkableOnly, genus)
                        .map { NearbyTree(it, from.distanceTo(it.location)) }
                        // A tree in a corner of the box can be farther than one just outside it: keep the circle.
                        .filter { it.distanceM <= radius }
                        .sortedBy { it.distanceM }
                if (found.size >= count) break
            }
            return found.take(count)
        }

        private companion object {
            /** From the trees of a street to the whole city (a genus or remarkable tree can be far away). */
            val SEARCH_RADII_M = listOf(100.0, 300.0, 1_000.0, 3_000.0, 10_000.0)

            /** The repository returns the trees closest to the box centre first; the exact order is set here. */
            const val CANDIDATES = 50
        }
    }
