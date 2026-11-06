package com.treesofparis.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Platform [LocationManager] based location, so the app needs no Google Play services. */
internal class LocationRepositoryImpl(
    private val context: Context,
    private val clock: () -> Long,
) : LocationRepository {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override suspend fun currentLocation(): GeoPoint? {
        if (!hasPermission()) return null
        val location = recentLastKnownLocation() ?: freshLocation()
        return location?.let { GeoPoint(lat = it.latitude, lon = it.longitude) }
    }

    private fun hasPermission() =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    @Suppress("MissingPermission") // checked in hasPermission()
    private fun recentLastKnownLocation(): Location? =
        PROVIDERS
            .filter { locationManager.isProviderEnabled(it) || locationManager.getLastKnownLocation(it) != null }
            .mapNotNull { locationManager.getLastKnownLocation(it) }
            .filter { clock() - it.time <= MAX_AGE_MS }
            .maxByOrNull { it.time }

    @Suppress("MissingPermission") // checked in hasPermission()
    private suspend fun freshLocation(): Location? {
        val provider = PROVIDERS.firstOrNull { locationManager.isProviderEnabled(it) }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || provider == null) return null
        return withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                locationManager.getCurrentLocation(provider, signal, context.mainExecutor) { continuation.resume(it) }
            }
        }
    }

    private companion object {
        val PROVIDERS =
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        const val MAX_AGE_MS = 2 * 60 * 1000L
        const val TIMEOUT_MS = 5_000L
    }
}
