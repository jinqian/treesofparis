package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.UserLocation
import fr.arbresdeparis.domain.repository.LocationRepository
import javax.inject.Inject

/** Locates the user and tells whether they are inside the map's coverage area. */
class LocateUser
    @Inject
    constructor(
        private val location: LocationRepository,
    ) {
        suspend operator fun invoke(): UserLocation {
            val point = location.currentLocation() ?: return UserLocation.Unavailable
            return if (GeoBounds.COVERAGE.contains(point)) UserLocation.InArea(point) else UserLocation.OutOfArea(point)
        }
    }
