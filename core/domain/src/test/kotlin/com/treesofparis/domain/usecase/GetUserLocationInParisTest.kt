package com.treesofparis.domain.usecase

import com.treesofparis.domain.fake.FakeLocationRepository
import com.treesofparis.domain.model.GeoPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetUserLocationInParisTest {
    @Test
    fun inside_returnsLocation() =
        runTest {
            val louvre = GeoPoint(48.8606, 2.3376)
            assertEquals(louvre, GetUserLocationInParis(FakeLocationRepository(louvre))())
        }

    @Test
    fun outsideParis_returnsNull() =
        runTest {
            assertNull(GetUserLocationInParis(FakeLocationRepository(GeoPoint(45.764, 4.8357)))())
        }

    @Test
    fun unavailable_returnsNull() =
        runTest {
            assertNull(GetUserLocationInParis(FakeLocationRepository(null))())
        }
}
