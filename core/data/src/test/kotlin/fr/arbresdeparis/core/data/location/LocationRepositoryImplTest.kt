package fr.arbresdeparis.core.data.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.domain.model.GeoPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class LocationRepositoryImplTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val repository = LocationRepositoryImpl(app, clock = { NOW })

    @Test
    fun withoutPermission_returnsNull() =
        runTest {
            assertNull(repository.currentLocation())
        }

    @Test
    fun withPermission_returnsRecentLastKnownLocation() =
        runTest {
            shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            shadowOf(locationManager).simulateLocation(location(48.86, 2.34, NOW - 1_000))
            assertEquals(GeoPoint(48.86, 2.34), repository.currentLocation())
        }

    @Test
    fun noFreshFix_fallsBackToOlderLastKnownLocation() =
        runTest {
            shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            shadowOf(locationManager).simulateLocation(location(48.86, 2.34, NOW - 10 * 60_000))
            disableAllProviders()
            assertEquals(GeoPoint(48.86, 2.34), repository.currentLocation())
        }

    @Test
    fun noFreshFix_ignoresLastKnownLocationOlderThanAnHour() =
        runTest {
            shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            shadowOf(locationManager).simulateLocation(location(48.86, 2.34, NOW - 2 * 60 * 60_000))
            disableAllProviders()
            assertNull(repository.currentLocation())
        }

    @Test
    fun freshFix_prefersFusedThenNetworkThenGps() {
        assertEquals(LocationManager.FUSED_PROVIDER, bestFreshFixProvider(sdkInt = 33) { true })
        assertEquals(
            LocationManager.NETWORK_PROVIDER,
            bestFreshFixProvider(sdkInt = 33) { it != LocationManager.FUSED_PROVIDER },
        )
        assertEquals(LocationManager.NETWORK_PROVIDER, bestFreshFixProvider(sdkInt = 30) { true })
        assertEquals(
            LocationManager.GPS_PROVIDER,
            bestFreshFixProvider(sdkInt = 33) {
                it ==
                    LocationManager.GPS_PROVIDER
            },
        )
        assertNull(bestFreshFixProvider(sdkInt = 33) { false })
    }

    private fun disableAllProviders() {
        locationManager.allProviders.forEach { shadowOf(locationManager).setProviderEnabled(it, false) }
    }

    private fun location(
        lat: Double,
        lon: Double,
        time: Long,
    ) = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = lat
        longitude = lon
        this.time = time
    }

    private companion object {
        const val NOW = 1_800_000_000_000L
    }
}
