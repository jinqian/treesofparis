package com.treesofparis.domain.model

/** A WGS84 coordinate. */
data class GeoPoint(
    val lat: Double,
    val lon: Double,
) {
    companion object {
        /** Default map centre (Hôtel de Ville). */
        val PARIS_CENTER = GeoPoint(lat = 48.8566, lon = 2.3522)
    }
}
