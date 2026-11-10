package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.TreeCluster
import fr.arbresdeparis.domain.repository.TreeRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Groups of trees for the zoomed-out map, placed where the trees gather.
 *
 * For each whole zoom level the groups are built once for the whole map ([GeoBounds.COVERAGE]) and kept: panning only
 * picks the groups on screen, so a group's count and place never change while the user moves at that zoom. They
 * change at the next level, where the groups split.
 *
 * Building a level: the trees are first counted in small cells (about [CELL_DP] screen points); then, densest cell
 * first, each cell not yet taken gathers every free cell within [RADIUS_DP] of it into one group, at the mean
 * position of its trees. Groups so sit on parks, avenues and squares rather than on a grid, and two groups are always
 * more than [RADIUS_DP] apart.
 */
@Singleton
class GetTreeClusters
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        private val levels = mutableMapOf<Int, List<TreeCluster>>()
        private val mutex = Mutex()

        /**
         * The groups of the [zoom]'s whole level whose centre lies in [bounds], widened by a group's reach; their
         * [TreeCluster.weight] compares them with every group of the level, so it does not change as the map pans.
         */
        suspend operator fun invoke(
            bounds: GeoBounds,
            zoom: Double,
        ): List<TreeCluster> {
            val level = floor(zoom).toInt()
            val groups = mutex.withLock { levels.getOrPut(level) { build(level) } }
            val (reachLat, reachLon) = degrees(RADIUS_DP, level, bounds.center.lat)
            val visible =
                GeoBounds(
                    south = bounds.south - reachLat,
                    west = bounds.west - reachLon,
                    north = bounds.north + reachLat,
                    east = bounds.east + reachLon,
                )
            return groups.filter { visible.contains(it.center) }
        }

        private suspend fun build(level: Int): List<TreeCluster> {
            val area = GeoBounds.COVERAGE
            val (cellLat, cellLon) = degrees(CELL_DP, level, area.center.lat)
            val (radiusLat, radiusLon) = degrees(RADIUS_DP, level, area.center.lat)
            return gather(trees.getTreeClusters(area, cellLat, cellLon), radiusLat, radiusLon)
        }

        companion object {
            /** Size of the cells the trees are first counted in, in screen points. */
            const val CELL_DP = 20.0

            /** How far a group reaches, in screen points: no two groups are closer than this. */
            const val RADIUS_DP = 60.0

            /** Metres per screen point at zoom 0 on the equator (web-map zoom on a 256 dp world). */
            private const val METRES_PER_DP_AT_ZOOM_0 = 156_543.03
            private const val METRES_PER_DEGREE_OF_LATITUDE = 111_320.0

            /** [dp] screen points at zoom [level] around latitude [lat], as (degrees of latitude, of longitude). */
            fun degrees(
                dp: Double,
                level: Int,
                lat: Double,
            ): Pair<Double, Double> {
                val latitude = Math.toRadians(lat)
                val metres = dp * METRES_PER_DP_AT_ZOOM_0 * cos(latitude) / 2.0.pow(level)
                val dLat = metres / METRES_PER_DEGREE_OF_LATITUDE
                return dLat to dLat / cos(latitude)
            }

            /**
             * Densest first, each free cell takes every free cell within the radius (an ellipse of [radiusLat] ×
             * [radiusLon] degrees) and becomes one group at the mean position of their trees. Deterministic: ties go
             * to the southernmost, then westernmost cell.
             */
            fun gather(
                cells: List<TreeCluster>,
                radiusLat: Double,
                radiusLon: Double,
            ): List<TreeCluster> {
                val ordered =
                    cells.sortedWith(
                        compareByDescending<TreeCluster> { it.count }
                            .thenBy { it.center.lat }
                            .thenBy { it.center.lon },
                    )
                val buckets = ordered.indices.groupBy { bucket(ordered[it].center, radiusLat, radiusLon) }
                val taken = BooleanArray(ordered.size)
                val groups = mutableListOf<TreeCluster>()
                for (seed in ordered.indices) {
                    if (taken[seed]) continue
                    val center = ordered[seed].center
                    val members =
                        nearby(buckets, bucket(center, radiusLat, radiusLon)).filter { index ->
                            val dLat = (ordered[index].center.lat - center.lat) / radiusLat
                            val dLon = (ordered[index].center.lon - center.lon) / radiusLon
                            !taken[index] && dLat * dLat + dLon * dLon <= 1
                        }
                    members.forEach { taken[it] = true }
                    groups += merge(members.map(ordered::get))
                }
                val largest = groups.maxOfOrNull { it.count } ?: return groups
                return groups.map { it.copy(weight = sqrt(it.count.toDouble() / largest)) }
            }

            /** The cells in [around]'s bucket and the eight next to it. */
            private fun nearby(
                buckets: Map<Pair<Long, Long>, List<Int>>,
                around: Pair<Long, Long>,
            ): List<Int> =
                (-1L..1L).flatMap { dRow ->
                    (-1L..1L).flatMap { dColumn -> buckets[around.first + dRow to around.second + dColumn].orEmpty() }
                }

            /** One group of [cells], at the mean position of their trees. */
            private fun merge(cells: List<TreeCluster>): TreeCluster {
                val count = cells.sumOf { it.count }
                val lat = cells.sumOf { it.center.lat * it.count } / count
                val lon = cells.sumOf { it.center.lon * it.count } / count
                return TreeCluster(GeoPoint(lat, lon), count)
            }

            private fun bucket(
                point: GeoPoint,
                radiusLat: Double,
                radiusLon: Double,
            ) = floor(point.lat / radiusLat).toLong() to floor(point.lon / radiusLon).toLong()
        }
    }
