package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.repository.LocationRepository
import javax.inject.Inject

/** The user's location if it is inside Paris, otherwise null (the map then stays on Paris). */
class GetUserLocationInParis
    @Inject
    constructor(
        private val location: LocationRepository,
    ) {
        suspend operator fun invoke(): GeoPoint? = location.currentLocation()?.takeIf { GeoBounds.PARIS.contains(it) }
    }
