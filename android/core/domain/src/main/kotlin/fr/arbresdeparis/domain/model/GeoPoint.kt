package fr.arbresdeparis.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A WGS84 coordinate. */
data class GeoPoint(
    val lat: Double,
    val lon: Double,
) {
    /** Great-circle (haversine) distance to [other], in metres. */
    fun distanceTo(other: GeoPoint): Double {
        val dLat = Math.toRadians(other.lat - lat)
        val dLon = Math.toRadians(other.lon - lon)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat)) * cos(Math.toRadians(other.lat)) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_M * asin(sqrt(h))
    }

    companion object {
        /** Default map centre (Hôtel de Ville). */
        val PARIS_CENTER = GeoPoint(lat = 48.8566, lon = 2.3522)

        /** Mean Earth radius. */
        const val EARTH_RADIUS_M = 6_371_000.0
    }
}
