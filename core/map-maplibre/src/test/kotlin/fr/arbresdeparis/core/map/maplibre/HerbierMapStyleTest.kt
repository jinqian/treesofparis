package fr.arbresdeparis.core.map.maplibre

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HerbierMapStyleTest {
    private val pins =
        PinColors(tree = "#4F7556", remarkable = "#C9962E", selected = "#1E2620", ring = "#FBFAF6", user = "#4A7FA8")

    private fun style(basemap: BasemapColors = BasemapColors.Light) =
        Json.parseToJsonElement(herbierStyle(basemap, pins)).jsonObject

    private fun JsonObject.layers() = getValue("layers").jsonArray.map { it.jsonObject }

    private fun JsonObject.string(key: String) = getValue(key).jsonPrimitive.content

    @Test
    fun readsOpenFreeMapVectorTilesAndGlyphs() {
        val style = style()
        val tiles =
            style
                .getValue("sources")
                .jsonObject
                .getValue("openmaptiles")
                .jsonObject
        assertEquals("vector", tiles.string("type"))
        assertEquals("https://tiles.openfreemap.org/planet", tiles.string("url"))
        assertEquals("https://tiles.openfreemap.org/fonts/{fontstack}/{range}.pbf", style.string("glyphs"))
    }

    @Test
    fun drawsTheBasemapFirst_thenTheTrees_thenTheUser() {
        val ids = style().layers().map { it.string("id") }
        assertEquals("background", ids.first())
        assertEquals(listOf(PIN_LAYER_TREE, PIN_LAYER_REMARKABLE, PIN_LAYER_SELECTED, USER_LAYER), ids.takeLast(4))
        // Taps look for the topmost pin first.
        assertEquals(listOf(PIN_LAYER_SELECTED, PIN_LAYER_REMARKABLE, PIN_LAYER_TREE), PIN_LAYERS_TOP_FIRST)
    }

    @Test
    fun leavesOutPointsOfInterestShieldsAndBorders() {
        val sourceLayers = style().layers().mapNotNull { it["source-layer"]?.jsonPrimitive?.content }.toSet()
        listOf("poi", "aerodrome_label", "housenumber", "mountain_peak", "boundary", "aeroway").forEach {
            assertTrue("$it should not be drawn", it !in sourceLayers)
        }
    }

    @Test
    fun everyColourIsFromThePalette() {
        listOf(BasemapColors.Light, BasemapColors.Dark).forEach { basemap ->
            val allowed = basemap.all + pins.all
            val used = colours(style(basemap))
            assertTrue("unexpected colours: ${used - allowed}", used.isNotEmpty() && allowed.containsAll(used))
        }
    }

    @Test
    fun lightAndDarkDifferOnlyByTheirColours() {
        val light = style(BasemapColors.Light)
        val dark = style(BasemapColors.Dark)
        assertEquals(light.layers().map { it.string("id") }, dark.layers().map { it.string("id") })
        assertNotEquals(colours(light), colours(dark))
    }

    @Test
    fun labelsAreInFrench() {
        val labels = style().layers().filter { it.string("type") == "symbol" }
        assertTrue(labels.isNotEmpty())
        labels.forEach { layer ->
            val field =
                layer
                    .getValue("layout")
                    .jsonObject
                    .getValue("text-field")
                    .toString()
            assertTrue("${layer.string("id")}: $field", field.contains("name:fr"))
        }
    }

    /** Every « #RRGGBB » or « rgba(…) » string anywhere in [element]. */
    private fun colours(element: JsonElement): Set<String> =
        when (element) {
            is JsonObject -> {
                element.values.flatMap(::colours).toSet()
            }

            is JsonArray -> {
                element.flatMap(::colours).toSet()
            }

            is JsonPrimitive -> {
                element.content
                    .takeIf { element.isString && (it.startsWith("#") || it.startsWith("rgba(")) }
                    ?.let(::setOf)
                    .orEmpty()
            }
        }
}
