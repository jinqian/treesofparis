package com.treesofparis.domain.model

/** An axis-aligned WGS84 bounding box. Edges are inclusive. */
data class GeoBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    val center: GeoPoint get() = GeoPoint(lat = (south + north) / 2, lon = (west + east) / 2)

    fun contains(point: GeoPoint): Boolean = point.lat in south..north && point.lon in west..east

    companion object {
        /** The City of Paris, including the Bois de Boulogne and the Bois de Vincennes. */
        val PARIS = GeoBounds(south = 48.815, west = 2.224, north = 48.902, east = 2.470)
    }
}
