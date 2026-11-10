package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.TreeCluster
import fr.arbresdeparis.domain.repository.TreeRepository
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow

/**
 * The trees inside [bounds] grouped on a grid whose cells span about [SPACING_DP] screen points at [zoom], so groups
 * sit 70 to 140 dp apart whatever the zoom. The grid only changes at whole zoom levels: groups stay put while the
 * user pans, and split as they zoom in.
 */
class GetTreeClusters
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(
            bounds: GeoBounds,
            zoom: Double,
        ): List<TreeCluster> {
            val (cellLat, cellLon) = cellSize(zoom, bounds.center.lat)
            return trees.getTreeClusters(bounds, cellLat, cellLon)
        }

        companion object {
            /** Screen points between two groups at a whole zoom level (twice that just before the next level). */
            const val SPACING_DP = 70.0

            /** Metres per screen point at zoom 0 on the equator (web-map zoom on a 256 dp world). */
            private const val METRES_PER_DP_AT_ZOOM_0 = 156_543.03
            private const val METRES_PER_DEGREE_OF_LATITUDE = 111_320.0

            /** The grid cell (degrees of latitude, degrees of longitude) for [zoom] around latitude [lat]. */
            fun cellSize(
                zoom: Double,
                lat: Double,
            ): Pair<Double, Double> {
                val latitude = Math.toRadians(lat)
                val metres = SPACING_DP * METRES_PER_DP_AT_ZOOM_0 * cos(latitude) / 2.0.pow(floor(zoom))
                val cellLat = metres / METRES_PER_DEGREE_OF_LATITUDE
                return cellLat to cellLat / cos(latitude)
            }
        }
    }
