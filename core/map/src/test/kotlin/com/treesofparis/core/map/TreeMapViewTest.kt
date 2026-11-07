package com.treesofparis.core.map

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeMapViewTest {
    @get:Rule
    val compose = createComposeRule()

    private class FakeProvider : MapProvider {
        var lastMarkers: List<MapMarker> = emptyList()
        var lastPalette: MapPalette? = null

        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            userLocation: GeoPoint?,
            config: MapConfig,
            palette: MapPalette,
            onMarkerClick: (Long) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) {
            lastMarkers = markers
            lastPalette = palette
            Text("fake map with ${markers.size} markers")
        }
    }

    @Test
    fun treeMapView_delegatesToProvidedMapProvider() {
        val provider = FakeProvider()
        val markers = listOf(MapMarker(id = 1, position = GeoPoint.PARIS_CENTER))
        val palette = MapPalette(Color.Green, Color.Yellow, Color.Black, Color.White, Color.Blue)
        compose.setContent {
            CompositionLocalProvider(LocalMapProvider provides provider) {
                TreeMapView(
                    initialCamera = MapCamera(GeoPoint.PARIS_CENTER, zoom = 16.0),
                    cameraRequest = null,
                    markers = markers,
                    userLocation = null,
                    config = MapConfig(GeoBounds.COVERAGE, minZoom = 11.0, maxZoom = 19.0),
                    palette = palette,
                    onMarkerClick = {},
                    onCameraChange = { _, _ -> },
                )
            }
        }
        compose.onNodeWithText("fake map with 1 markers").assertIsDisplayed()
        assertEquals(markers, provider.lastMarkers)
        assertEquals(palette, provider.lastPalette)
    }
}
