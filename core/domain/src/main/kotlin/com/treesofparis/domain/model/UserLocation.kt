package com.treesofparis.domain.model

/** Result of locating the user, relative to the area the map covers ([GeoBounds.COVERAGE]). */
sealed interface UserLocation {
    data class InArea(
        val point: GeoPoint,
    ) : UserLocation

    data class OutOfArea(
        val point: GeoPoint,
    ) : UserLocation

    /** No position: location off, permission missing, or no fix in time. */
    data object Unavailable : UserLocation
}
