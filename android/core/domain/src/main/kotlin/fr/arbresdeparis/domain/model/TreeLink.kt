package fr.arbresdeparis.domain.model

import java.net.URI
import java.util.Locale
import kotlin.math.abs

/**
 * A shared tree (issue #88): `https://arbresdeparis.fr/arbre/?id=2002389&ll=48.86125,2.25321`. The app opens it on the
 * tree's sheet (an Android App Link on `/arbre/`); without the app, the website's `/arbre/` page opens the web map on
 * it. [treeId] finds the tree; [location] (about 1 m) lets the web map find its cell, and still shows the place if the
 * tree has left the data.
 */
data class TreeLink(
    val treeId: Long,
    val location: GeoPoint?,
) {
    val url: String
        get() =
            buildString {
                append("https://")
                    .append(HOST)
                    .append(PATH)
                    .append("?id=")
                    .append(treeId)
                location?.let { append("&ll=").append(coordinate(it.lat)).append(',').append(coordinate(it.lon)) }
            }

    companion object {
        const val HOST = "arbresdeparis.fr"
        const val PATH = "/arbre/"
        private val HOSTS = setOf(HOST, "www.$HOST")
        private const val MAX_LAT = 90.0
        private const val MAX_LON = 180.0

        private fun coordinate(value: Double) = String.format(Locale.ROOT, "%.5f", value)

        /** The tree a link points to, or null when [url] is not a tree link of arbresdeparis.fr. */
        fun parse(url: String): TreeLink? {
            val uri = runCatching { URI(url) }.getOrNull()
            val query =
                uri
                    ?.takeIf { it.scheme?.lowercase() in setOf("https", "http") && it.host?.lowercase() in HOSTS }
                    ?.takeIf { it.path?.trimEnd('/') == PATH.trimEnd('/') }
                    ?.rawQuery
                    ?.split('&')
                    ?.mapNotNull { pair -> pair.split('=', limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] } }
                    ?.toMap()
            val id = query?.get("id")?.toLongOrNull()?.takeIf { it > 0 }
            return id?.let { TreeLink(it, query["ll"]?.let(::location)) }
        }

        private fun location(raw: String): GeoPoint? {
            val parts = raw.split(',', limit = 2).map { it.toDoubleOrNull() }
            val lat = parts.getOrNull(0)
            val lon = parts.getOrNull(1)
            val point = if (lat != null && lon != null) GeoPoint(lat, lon) else null
            return point?.takeIf { abs(it.lat) <= MAX_LAT && abs(it.lon) <= MAX_LON }
        }
    }
}
