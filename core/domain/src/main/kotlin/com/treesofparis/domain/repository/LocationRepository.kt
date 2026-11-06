package com.treesofparis.domain.repository

import com.treesofparis.domain.model.GeoPoint

interface LocationRepository {
    /** The device location, or null when it is unavailable or the permission is missing. */
    suspend fun currentLocation(): GeoPoint?
}
