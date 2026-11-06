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
        /**
         * The area covered by the dataset: Paris, the two Bois and the sites the City manages in the neighbouring
         * departments (cemeteries of Pantin, Saint-Ouen, Thiais, Ivry, Bagneux...). Extent of all trees in 2026-11
         * plus a small margin; the data script reports trees that fall outside it.
         */
        val COVERAGE = GeoBounds(south = 48.735, west = 2.200, north = 48.920, east = 2.480)
    }
}
