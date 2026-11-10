@file:Suppress("MagicNumber") // a map style is data: zoom stops, line widths and pin sizes read best inline

package fr.arbresdeparis.core.map.maplibre

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * The basemap colours of the « Herbier » map, light and dark (the app's HerbierPalette and the design canvas): paper
 * land, sage parks, a soft blue Seine, paper streets, muted labels. No points of interest, shields or borders.
 */
data class BasemapColors(
    val land: String,
    val green: String,
    val water: String,
    val building: String,
    val road: String,
    val casing: String,
    val rail: String,
    val label: String,
    val labelHalo: String,
    val place: String,
) {
    val all: Set<String> get() = setOf(land, green, water, building, road, casing, rail, label, labelHalo, place)

    companion object {
        val Light =
            BasemapColors(
                land = "#F4F3EC",
                green = "#DCE4D2",
                water = "#CFDDE0",
                building = "#E8E6DC",
                road = "#FBFAF6",
                casing = "#E2E1D8",
                rail = "#D3D2C8",
                label = "#5C665D",
                labelHalo = "#F4F3EC",
                place = "#1E2620",
            )
        val Dark =
            BasemapColors(
                land = "#1A1F1B",
                green = "#20291E",
                water = "#1B2A2E",
                building = "#222823",
                road = "#272D28",
                casing = "#2E352F",
                rail = "#2E352F",
                label = "#A3AB9F",
                labelHalo = "#1A1F1B",
                place = "#E6E8E1",
            )
    }
}

/** The pin colours, from the feature's MapPalette (see [toPinColors]). */
data class PinColors(
    val tree: String,
    val remarkable: String,
    val selected: String,
    val ring: String,
    val user: String,
    val treeGroup: String,
    val treeGroupLabel: String,
) {
    val all: Set<String> get() = setOf(tree, remarkable, selected, ring, user, treeGroup, treeGroupLabel)
}

internal const val PINS_SOURCE = "pins"
internal const val CLUSTERS_SOURCE = "clusters"
internal const val CLUSTER_LAYER = "clusters"
internal const val CLUSTER_COUNT_LAYER = "clusters-count"
internal const val USER_SOURCE = "user"
internal const val PIN_LAYER_TREE = "pins-tree"
internal const val PIN_LAYER_REMARKABLE = "pins-remarkable"
internal const val PIN_LAYER_SELECTED = "pins-selected"
internal const val USER_LAYER = "user"

/** Pin layers in the order a tap looks at them: the topmost pin wins (selected, then remarkable, then any tree). */
internal val PIN_LAYERS_TOP_FIRST = listOf(PIN_LAYER_SELECTED, PIN_LAYER_REMARKABLE, PIN_LAYER_TREE)

private const val OPENFREEMAP_TILES = "https://tiles.openfreemap.org/planet"
private const val OPENFREEMAP_GLYPHS = "https://tiles.openfreemap.org/fonts/{fontstack}/{range}.pbf"
private const val FONT_REGULAR = "Noto Sans Regular"
private const val FONT_ITALIC = "Noto Sans Italic"
private const val FONT_BOLD = "Noto Sans Bold"
private const val VECTOR = "openmaptiles"

/**
 * Pin sizes in dp (MapLibre's « pixels » are density-independent). Trees and remarkable trees shrink when zoomed out,
 * so the trees of a street read as a row of dots instead of one green band; full size at street level (MapLibre zoom
 * 16.5). The selected tree and the user keep their size.
 */
private val TREE_RADIUS = byZoom(13 to 2.5, 15 to 3.5, 16.5 to 6)
private val REMARKABLE_RADIUS = byZoom(13 to 5, 15 to 6, 16.5 to 8)
private val PIN_RING_WIDTH = byZoom(13 to 0.8, 15 to 1.2, 16.5 to 2)
private const val SELECTED_RADIUS = 9
private const val USER_RADIUS = 8
private const val RING_WIDTH = 2
private const val USER_RING_WIDTH = 3

/**
 * The style JSON for the map: OpenFreeMap vector tiles (OpenMapTiles schema) drawn in the Herbier [basemap] colours,
 * then the trees and the user's position from two GeoJSON sources ([PINS_SOURCE], [USER_SOURCE]) that the provider
 * fills. Zoom levels in the style are MapLibre's (512 px tiles).
 */
fun herbierStyle(
    basemap: BasemapColors,
    pins: PinColors,
): String =
    buildJsonObject {
        put("version", 8)
        put("name", "Herbier")
        put("glyphs", OPENFREEMAP_GLYPHS)
        putJsonObject("sources") {
            putJsonObject(VECTOR) {
                put("type", "vector")
                put("url", OPENFREEMAP_TILES)
            }
            put(PINS_SOURCE, emptyGeoJsonSource())
            put(CLUSTERS_SOURCE, emptyGeoJsonSource())
            put(USER_SOURCE, emptyGeoJsonSource())
        }
        put("layers", JsonArray(basemapLayers(basemap) + clusterLayers(pins) + pinLayers(pins)))
    }.toString()

private fun emptyGeoJsonSource() =
    buildJsonObject {
        put("type", "geojson")
        put("data", GeoJson.featureCollection(emptyList()))
    }

@Suppress("LongMethod") // the basemap reads best as one list of layers, back to front
private fun basemapLayers(c: BasemapColors): List<JsonObject> =
    listOf(
        layer("background", "background") { paint("background-color" to c.land) },
        fill("green-landcover", "landcover", c.green, minZoom = 9.0, filter = classIn("grass", "wood")),
        fill("green-park", "park", c.green),
        fill("green-cemetery", "landuse", c.green, minZoom = 11.0, filter = classIn("cemetery")),
        fill("water", "water", c.water, filter = notTunnel()),
        line("waterway", "waterway", c.water, width = interpolate(10 to 0.5, 18 to 6)),
        fill("building", "building", c.building, minZoom = 14.0, outline = c.casing),
        line(
            "road-casing",
            "transportation",
            c.casing,
            minZoom = 11.0,
            filter = all(notTunnel(), classIn("motorway", "trunk", "primary", "secondary", "tertiary")),
            width = interpolate(10 to 3, 20 to 24),
        ),
        line(
            "road-minor",
            "transportation",
            c.road,
            minZoom = 12.0,
            filter = all(notTunnel(), classIn("minor", "service", "track")),
            width = interpolate(13 to 1.8, 20 to 20),
        ),
        line(
            "road-major",
            "transportation",
            c.road,
            minZoom = 6.0,
            filter = all(notTunnel(), classIn("motorway", "trunk", "primary", "secondary", "tertiary")),
            width = interpolate(10 to 2, 20 to 20),
        ),
        line(
            "road-path",
            "transportation",
            c.road,
            minZoom = 14.0,
            filter = all(notTunnel(), classIn("path")),
            width = interpolate(14 to 0.8, 20 to 6),
        ),
        line(
            "rail",
            "transportation",
            c.rail,
            minZoom = 12.0,
            filter = all(notTunnel(), classIn("rail", "transit")),
            width = interpolate(14 to 1, 20 to 4),
            dashed = true,
        ),
        label("label-water", "water_name", c, font = FONT_ITALIC, size = 13, lineFollowing = true),
        label(
            "label-street-major",
            "transportation_name",
            c,
            minZoom = 12.5,
            size = 12,
            lineFollowing = true,
            filter = classIn("motorway", "trunk", "primary", "secondary", "tertiary"),
        ),
        label(
            "label-street-minor",
            "transportation_name",
            c,
            minZoom = 14.0,
            size = 11,
            lineFollowing = true,
            filter = classIn("minor", "service", "track"),
        ),
        label(
            "label-quarter",
            "place",
            c,
            minZoom = 12.0,
            size = 13,
            color = c.place,
            opacity = 0.7,
            filter = classIn("suburb", "quarter", "neighbourhood"),
        ),
        label(
            "label-town",
            "place",
            c,
            minZoom = 8.0,
            maxZoom = 13.0,
            size = 14,
            color = c.place,
            opacity = 0.7,
            filter = classIn("city", "town", "village"),
        ),
    )

/**
 * Groups of trees below street level: a deep green disc, ringed like the pins, whose area grows with the count (the
 * radius follows its square root, from 12 dp for a few trees to 28 dp for a couple of thousand), the count inside in
 * bold paper white.
 */
private fun clusterLayers(p: PinColors): List<JsonObject> {
    val radius =
        expression(
            "interpolate",
            expression("linear"),
            expression("sqrt", expression("get", "count")),
            1,
            12,
            10,
            15,
            30,
            20,
            60,
            28,
        )
    val disc =
        buildJsonObject {
            put("id", CLUSTER_LAYER)
            put("type", "circle")
            put("source", CLUSTERS_SOURCE)
            putJsonObject("paint") {
                put("circle-color", p.treeGroup)
                put("circle-opacity", CLUSTER_OPACITY)
                put("circle-radius", radius)
                put("circle-stroke-color", p.ring)
                put("circle-stroke-width", 1.5)
            }
        }
    val count =
        buildJsonObject {
            put("id", CLUSTER_COUNT_LAYER)
            put("type", "symbol")
            put("source", CLUSTERS_SOURCE)
            putJsonObject("layout") {
                put("text-field", expression("get", "label"))
                put("text-font", JsonArray(listOf(JsonPrimitive(FONT_BOLD))))
                put("text-size", 12)
                put("text-allow-overlap", true)
                put("text-ignore-placement", true)
            }
            putJsonObject("paint") { put("text-color", p.treeGroupLabel) }
        }
    return listOf(disc, count)
}

private const val CLUSTER_OPACITY = 0.92

private fun pinLayers(p: PinColors): List<JsonObject> =
    listOf(
        circle(PIN_LAYER_TREE, PINS_SOURCE, p.tree, TREE_RADIUS, p.ring, PIN_RING_WIDTH, eq("style", "TREE")),
        circle(
            PIN_LAYER_REMARKABLE,
            PINS_SOURCE,
            p.remarkable,
            REMARKABLE_RADIUS,
            p.ring,
            PIN_RING_WIDTH,
            eq("style", "REMARKABLE"),
        ),
        circle(PIN_LAYER_SELECTED, PINS_SOURCE, p.selected, SELECTED_RADIUS, p.ring, RING_WIDTH, eq("selected", true)),
        circle(USER_LAYER, USER_SOURCE, p.user, USER_RADIUS, p.ring, USER_RING_WIDTH, filter = null),
    )

// Layer builders

private fun layer(
    id: String,
    type: String,
    sourceLayer: String? = null,
    minZoom: Double? = null,
    maxZoom: Double? = null,
    filter: JsonElement? = null,
    body: MutableMap<String, JsonElement>.() -> Unit,
): JsonObject {
    val fields = mutableMapOf<String, JsonElement>("id" to JsonPrimitive(id), "type" to JsonPrimitive(type))
    sourceLayer?.let {
        fields["source"] = JsonPrimitive(VECTOR)
        fields["source-layer"] = JsonPrimitive(it)
    }
    minZoom?.let { fields["minzoom"] = JsonPrimitive(it) }
    maxZoom?.let { fields["maxzoom"] = JsonPrimitive(it) }
    filter?.let { fields["filter"] = it }
    fields.body()
    return JsonObject(fields)
}

private fun MutableMap<String, JsonElement>.paint(vararg values: Pair<String, Any>) {
    this["paint"] = JsonObject(values.associate { (key, value) -> key to value.toJson() })
}

private fun MutableMap<String, JsonElement>.layout(vararg values: Pair<String, Any>) {
    this["layout"] = JsonObject(values.associate { (key, value) -> key to value.toJson() })
}

private fun fill(
    id: String,
    sourceLayer: String,
    color: String,
    minZoom: Double? = null,
    filter: JsonElement? = null,
    outline: String? = null,
) = layer(id, "fill", sourceLayer, minZoom, filter = filter) {
    if (outline == null) paint("fill-color" to color) else paint("fill-color" to color, "fill-outline-color" to outline)
}

@Suppress("LongParameterList") // a line layer has that many knobs
private fun line(
    id: String,
    sourceLayer: String,
    color: String,
    minZoom: Double? = null,
    filter: JsonElement? = null,
    width: JsonElement,
    dashed: Boolean = false,
) = layer(id, "line", sourceLayer, minZoom, filter = filter) {
    layout("line-cap" to if (dashed) "butt" else "round", "line-join" to "round")
    if (dashed) {
        paint(
            "line-color" to color,
            "line-width" to width,
            "line-dasharray" to JsonArray(listOf(2, 2).map(::JsonPrimitive)),
        )
    } else {
        paint("line-color" to color, "line-width" to width)
    }
}

@Suppress("LongParameterList") // a label layer has that many knobs
private fun label(
    id: String,
    sourceLayer: String,
    colors: BasemapColors,
    minZoom: Double? = null,
    maxZoom: Double? = null,
    size: Int,
    font: String = FONT_REGULAR,
    lineFollowing: Boolean = false,
    color: String = colors.label,
    opacity: Double = 1.0,
    filter: JsonElement? = null,
) = layer(id, "symbol", sourceLayer, minZoom, maxZoom, filter) {
    val common =
        arrayOf<Pair<String, Any>>(
            "text-field" to frenchName(),
            "text-font" to JsonArray(listOf(JsonPrimitive(font))),
            "text-size" to size,
            "text-max-width" to 8,
        )
    if (lineFollowing) layout(*common, "symbol-placement" to "line") else layout(*common)
    paint(
        "text-color" to color,
        "text-halo-color" to colors.labelHalo,
        "text-halo-width" to 1.2,
        "text-opacity" to opacity,
    )
}

@Suppress("LongParameterList") // the pin look is that many values
private fun circle(
    id: String,
    source: String,
    color: String,
    radius: Any,
    ring: String,
    ringWidth: Any,
    filter: JsonElement?,
): JsonObject {
    val fields =
        mutableMapOf<String, JsonElement>(
            "id" to JsonPrimitive(id),
            "type" to JsonPrimitive("circle"),
            "source" to JsonPrimitive(source),
        )
    filter?.let { fields["filter"] = it }
    fields.paint(
        "circle-color" to color,
        "circle-radius" to radius,
        "circle-stroke-color" to ring,
        "circle-stroke-width" to ringWidth,
        "circle-pitch-alignment" to "map",
    )
    return JsonObject(fields)
}

// Expressions

/** The French name when the data has one (« Seine »), else the local one. */
private fun frenchName() = expression("coalesce", expression("get", "name:fr"), expression("get", "name"))

private fun classIn(vararg classes: String) =
    expression("match", expression("get", "class"), JsonArray(classes.map(::JsonPrimitive)), true, false)

private fun notTunnel() = expression("!=", expression("get", "brunnel"), "tunnel")

private fun all(vararg conditions: JsonElement) = expression("all", *conditions)

private fun eq(
    property: String,
    value: Any,
) = expression("==", expression("get", property), value)

/** Linear growth with the (MapLibre) zoom, between (zoom, value) stops. */
private fun byZoom(vararg stops: Pair<Number, Number>) =
    expression(
        "interpolate",
        expression("linear"),
        expression("zoom"),
        *stops.flatMap { (zoom, value) -> listOf(zoom, value) }.toTypedArray(),
    )

/** Exponential growth with the zoom, between (zoom, value) stops. */
private fun interpolate(vararg stops: Pair<Int, Number>) =
    expression(
        "interpolate",
        expression("exponential", 1.4),
        expression("zoom"),
        *stops.flatMap { (zoom, value) -> listOf(zoom, value) }.toTypedArray(),
    )

private fun expression(
    operator: String,
    vararg arguments: Any,
) = buildJsonArray {
    add(JsonPrimitive(operator))
    arguments.forEach { add(it.toJson()) }
}

private fun Any.toJson(): JsonElement =
    when (this) {
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        else -> error("Unsupported style value: $this")
    }

/** Small JSON helpers shared with the pin sources. */
internal object GeoJson {
    fun featureCollection(features: List<JsonObject>) =
        buildJsonObject {
            put("type", "FeatureCollection")
            putJsonArray("features") { features.forEach { add(it) } }
        }
}
