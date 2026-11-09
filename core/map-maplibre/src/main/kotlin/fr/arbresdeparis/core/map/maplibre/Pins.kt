package fr.arbresdeparis.core.map.maplibre

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.domain.model.GeoPoint
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** The trees as GeoJSON points with the properties the pin layers filter on: `id`, `style`, `selected`. */
internal fun markersGeoJson(markers: List<MapMarker>): String =
    GeoJson
        .featureCollection(
            markers.map { marker ->
                point(marker.position) {
                    put("id", marker.id)
                    put("style", marker.style.name)
                    put("selected", marker.selected)
                }
            },
        ).toString()

/** The user's position as one point, or an empty collection when it is unknown. */
internal fun userGeoJson(location: GeoPoint?): String =
    GeoJson.featureCollection(listOfNotNull(location?.let { point(it) {} })).toString()

private fun point(
    position: GeoPoint,
    properties: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit,
): JsonObject =
    buildJsonObject {
        put("type", "Feature")
        putJsonObject("geometry") {
            put("type", "Point")
            // GeoJSON is longitude first.
            putJsonArray("coordinates") {
                add(kotlinx.serialization.json.JsonPrimitive(position.lon))
                add(kotlinx.serialization.json.JsonPrimitive(position.lat))
            }
        }
        putJsonObject("properties", properties)
    }

/** The palette the feature chose from its theme, as the style's hex colours. */
internal fun MapPalette.toPinColors() =
    PinColors(
        tree = tree.toHex(),
        remarkable = remarkable.toHex(),
        selected = selected.toHex(),
        ring = ring.toHex(),
        user = userLocation.toHex(),
    )

/** Keeps red, green and blue, drops alpha: the style gets opaque « #RRGGBB » colours. */
private const val RGB_MASK = 0xFFFFFF

private fun Color.toHex(): String = "#%06X".format(toArgb() and RGB_MASK)

/**
 * [fr.arbresdeparis.core.map.MapCamera] zooms are web-map zooms on a 256 dp world (OSM and Google numbering);
 * MapLibre draws 512 dp tiles, so the same view is one zoom level lower.
 */
internal fun Double.toMapLibreZoom(): Double = this - 1

internal fun Double.toContractZoom(): Double = this + 1
