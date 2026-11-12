package fr.arbresdeparis.core.map.maplibre

import androidx.compose.ui.graphics.Color
import fr.arbresdeparis.core.map.MapCluster
import fr.arbresdeparis.core.map.MapMarker
import fr.arbresdeparis.core.map.MapPalette
import fr.arbresdeparis.core.map.MarkerStyle
import fr.arbresdeparis.domain.model.GeoPoint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class PinsGeoJsonTest {
    @Test
    fun markers_becomePointsWithIdStyleAndSelection_longitudeFirst() {
        val json =
            markersGeoJson(
                listOf(
                    MapMarker(125046, GeoPoint(48.8527, 2.3510), selected = true, style = MarkerStyle.REMARKABLE),
                    MapMarker(7, GeoPoint(48.86, 2.34)),
                ),
            )
        val features =
            Json
                .parseToJsonElement(json)
                .jsonObject
                .getValue("features")
                .jsonArray
                .map { it.jsonObject }
        assertEquals(2, features.size)
        val first = features[0]
        assertEquals(
            "[2.351,48.8527]",
            first
                .getValue("geometry")
                .jsonObject
                .getValue("coordinates")
                .toString(),
        )
        val properties = first.getValue("properties").jsonObject
        assertEquals(
            125046L,
            properties
                .getValue("id")
                .jsonPrimitive.content
                .toLong(),
        )
        assertEquals("REMARKABLE", properties.getValue("style").jsonPrimitive.content)
        assertEquals("true", properties.getValue("selected").jsonPrimitive.content)
        assertEquals(
            "TREE",
            features[1]
                .getValue("properties")
                .jsonObject
                .getValue("style")
                .jsonPrimitive.content,
        )
    }

    @Test
    fun clusters_becomePointsWithIdCountAndLabel() {
        val json = clustersGeoJson(listOf(MapCluster(3, GeoPoint(48.85, 2.35), count = 1234, label = "1\u00A0234")))
        val feature =
            Json
                .parseToJsonElement(json)
                .jsonObject
                .getValue("features")
                .jsonArray
                .single()
                .jsonObject
        val properties = feature.getValue("properties").jsonObject
        assertEquals("3", properties.getValue("id").jsonPrimitive.content)
        assertEquals("1234", properties.getValue("count").jsonPrimitive.content)
        assertEquals("1\u00A0234", properties.getValue("label").jsonPrimitive.content)
    }

    @Test
    fun userLocation_isOnePoint_orNothing() {
        val one = Json.parseToJsonElement(userGeoJson(GeoPoint(48.85, 2.35))).jsonObject.getValue("features")
        assertEquals(1, one.jsonArray.size)
        val none = Json.parseToJsonElement(userGeoJson(null)).jsonObject.getValue("features")
        assertEquals(0, none.jsonArray.size)
    }

    @Test
    fun palette_becomesHexColours() {
        val palette =
            MapPalette(
                tree = Color(0xFF4F7556),
                remarkable = Color(0xFFC9962E),
                selected = Color(0xFF1E2620),
                ring = Color(0xFFFBFAF6),
                userLocation = Color(0xFF4A7FA8),
                treeGroup = Color(0xFF3F5F45),
                treeGroupLabel = Color(0xFFF4F3EC),
            )
        assertEquals(
            PinColors("#4F7556", "#C9962E", "#1E2620", "#FBFAF6", "#4A7FA8", "#3F5F45", "#F4F3EC"),
            palette.toPinColors(),
        )
    }

    @Test
    fun zoom_contractIsAWebMapZoomOn256DpTiles_mapLibreUses512() {
        assertEquals(15.0, 16.0.toMapLibreZoom(), 0.0)
        assertEquals(16.0, 15.0.toContractZoom(), 0.0)
    }
}
