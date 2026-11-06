package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.repository.LocationRepository
import javax.inject.Inject

/** The user's location if it is inside the dataset's coverage area, otherwise null (the map stays on Paris). */
class GetUserLocationInCoverage
    @Inject
    constructor(
        private val location: LocationRepository,
    ) {
        suspend operator fun invoke(): GeoPoint? {
            val current = location.currentLocation() ?: return null
            return current.takeIf { GeoBounds.COVERAGE.contains(it) }
        }
    }
