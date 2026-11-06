package com.treesofparis.core.map.osmdroid

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.treesofparis.core.map.MapConfig
import com.treesofparis.domain.model.GeoBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.MapView
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapConfigApplyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun applyTo_setsScrollLimitsAndZoomLimits() {
        configureOsmdroid(context)
        val mapView = MapView(context)

        MapConfig(GeoBounds.COVERAGE, minZoom = 11.0, maxZoom = 19.0).applyTo(mapView)

        assertTrue(mapView.isScrollableAreaLimitLatitude)
        assertTrue(mapView.isScrollableAreaLimitLongitude)
        assertEquals(11.0, mapView.minZoomLevel, 0.0)
        assertEquals(19.0, mapView.maxZoomLevel, 0.0)
        assertFalse(mapView.isHorizontalMapRepetitionEnabled)
        assertFalse(mapView.isVerticalMapRepetitionEnabled)
    }

    @Test
    fun boundsConversion_roundTrips() {
        val bounds = GeoBounds(south = 48.8, west = 2.2, north = 48.9, east = 2.5)
        assertEquals(bounds, bounds.toBoundingBox().toGeoBounds())
    }
}
