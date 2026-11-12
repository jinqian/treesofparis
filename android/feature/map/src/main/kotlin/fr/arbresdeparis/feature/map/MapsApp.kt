package fr.arbresdeparis.feature.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import fr.arbresdeparis.domain.model.GeoPoint
import java.util.Locale

/** The `geo:` link that shows [location] as a pin in the user's Maps app, from which they can start a route. */
internal fun geoUri(location: GeoPoint): String {
    val coordinates = String.format(Locale.US, "%.6f,%.6f", location.lat, location.lon)
    return "geo:$coordinates?q=$coordinates"
}

/** Opens [location] in the user's Maps app. Does nothing when no app handles `geo:` links. */
internal fun Context.openInMapsApp(location: GeoPoint) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, geoUri(location).toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // No maps app installed: nothing sensible to do.
    }
}
