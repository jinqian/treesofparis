package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.fake.FakeLocationRepository
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.UserLocation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocateUserTest {
    @Test
    fun insideParis_isInArea() =
        runTest {
            val louvre = GeoPoint(48.8606, 2.3376)
            assertEquals(UserLocation.InArea(louvre), LocateUser(FakeLocationRepository(louvre))())
        }

    @Test
    fun inPantinCemetery_isInArea() =
        runTest {
            val pantin = GeoPoint(48.9059, 2.4101)
            assertEquals(UserLocation.InArea(pantin), LocateUser(FakeLocationRepository(pantin))())
        }

    @Test
    fun outsideCoverage_isOutOfArea() =
        runTest {
            val lyon = GeoPoint(45.764, 4.8357)
            assertEquals(UserLocation.OutOfArea(lyon), LocateUser(FakeLocationRepository(lyon))())
        }

    @Test
    fun noPosition_isUnavailable() =
        runTest {
            assertEquals(UserLocation.Unavailable, LocateUser(FakeLocationRepository(null))())
        }
}
