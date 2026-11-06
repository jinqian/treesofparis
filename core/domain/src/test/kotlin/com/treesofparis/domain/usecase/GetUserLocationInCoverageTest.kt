package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeLocationRepository
import com.treesofparis.domain.model.GeoPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetUserLocationInCoverageTest {
    @Test
    fun insideParis_returnsLocation() =
        runTest {
            val louvre = GeoPoint(48.8606, 2.3376)
            assertEquals(louvre, GetUserLocationInCoverage(FakeLocationRepository(louvre))())
        }

    @Test
    fun inPantinCemetery_returnsLocation() =
        runTest {
            val pantin = GeoPoint(48.9059, 2.4101)
            assertEquals(pantin, GetUserLocationInCoverage(FakeLocationRepository(pantin))())
        }

    @Test
    fun outsideCoverage_returnsNull() =
        runTest {
            assertNull(GetUserLocationInCoverage(FakeLocationRepository(GeoPoint(45.764, 4.8357)))())
        }

    @Test
    fun unavailable_returnsNull() =
        runTest {
            assertNull(GetUserLocationInCoverage(FakeLocationRepository(null))())
        }
}
