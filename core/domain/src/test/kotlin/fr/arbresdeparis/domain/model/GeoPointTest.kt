package fr.arbresdeparis.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoPointTest {
    private val paris = GeoPoint.PARIS_CENTER

    @Test
    fun distanceTo_sameSpot_isZero() {
        assertEquals(0.0, paris.distanceTo(paris), 0.0)
    }

    @Test
    fun distanceTo_oneThousandthOfADegreeNorth_isAbout111Metres() {
        assertEquals(111.2, paris.distanceTo(paris.copy(lat = paris.lat + 0.001)), 0.5)
    }

    @Test
    fun distanceTo_eastIsShorterThanNorthAtParisLatitude() {
        // A degree of longitude spans cos(48.86°) ≈ 0.66 of a degree of latitude.
        assertEquals(73.1, paris.distanceTo(paris.copy(lon = paris.lon + 0.001)), 0.5)
    }

    @Test
    fun distanceTo_hotelDeVilleToEiffelTower_isAbout4Kilometres() {
        val eiffel = GeoPoint(lat = 48.8584, lon = 2.2945)
        val distance = paris.distanceTo(eiffel)
        assertTrue("was $distance", distance in 4_150.0..4_300.0)
    }
}
