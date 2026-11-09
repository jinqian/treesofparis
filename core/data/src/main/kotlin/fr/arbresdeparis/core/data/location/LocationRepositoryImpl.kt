package fr.arbresdeparis.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Platform [LocationManager] based location, so the app needs no Google Play services.
 *
 * Order: a last known position up to 2 minutes old; else a fresh fix from the best enabled provider (fused, then
 * network, then GPS — GPS alone rarely gets a fix indoors in time); else a last known position up to an hour old.
 */
internal class LocationRepositoryImpl(
    private val context: Context,
    private val clock: () -> Long,
) : LocationRepository {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override suspend fun currentLocation(): GeoPoint? {
        if (!hasPermission()) return null
        val location = lastKnownLocation(RECENT_MAX_AGE_MS) ?: freshLocation() ?: lastKnownLocation(STALE_MAX_AGE_MS)
        return location?.let { GeoPoint(lat = it.latitude, lon = it.longitude) }
    }

    private fun hasPermission() =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    @Suppress("MissingPermission") // checked in hasPermission()
    private fun lastKnownLocation(maxAgeMs: Long): Location? =
        locationManager.allProviders
            .mapNotNull { locationManager.getLastKnownLocation(it) }
            .filter { clock() - it.time <= maxAgeMs }
            .maxByOrNull { it.time }

    @Suppress("MissingPermission") // checked in hasPermission()
    private suspend fun freshLocation(): Location? {
        val provider = bestFreshFixProvider(Build.VERSION.SDK_INT, locationManager::isProviderEnabled)
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
        const val RECENT_MAX_AGE_MS = 2 * 60 * 1000L
        const val STALE_MAX_AGE_MS = 60 * 60 * 1000L
        const val TIMEOUT_MS = 10_000L
    }
}

/** The provider to ask for a fresh fix: fused (Android 12+), then network, then GPS, among the enabled ones. */
internal fun bestFreshFixProvider(
    sdkInt: Int,
    isEnabled: (String) -> Boolean,
): String? {
    val candidates =
        buildList {
            if (sdkInt >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }
    return candidates.firstOrNull(isEnabled)
}
