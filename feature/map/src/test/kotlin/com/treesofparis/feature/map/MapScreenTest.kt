package com.treesofparis.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.treesofparis.core.designsystem.theme.TreesTheme
import com.treesofparis.core.map.CameraRequest
import com.treesofparis.core.map.LocalMapProvider
import com.treesofparis.core.map.MapCamera
import com.treesofparis.core.map.MapConfig
import com.treesofparis.core.map.MapMarker
import com.treesofparis.core.map.MapProvider
import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private object StubMap : MapProvider {
        @Composable
        override fun Content(
            initialCamera: MapCamera,
            cameraRequest: CameraRequest?,
            markers: List<MapMarker>,
            userLocation: GeoPoint?,
            config: MapConfig,
            onMarkerClick: (Long) -> Unit,
            onCameraChange: (MapCamera, GeoBounds) -> Unit,
            modifier: Modifier,
        ) = Box(modifier)
    }

    private var messagesShown = 0
    private var settingsOpened = 0
    private var locateClicks = 0

    private fun show(state: MapUiState) =
        compose.setContent {
            CompositionLocalProvider(LocalMapProvider provides StubMap) {
                TreesTheme {
                    MapScreen(
                        state = state,
                        onCameraChange = { _, _ -> },
                        onMarkerClick = {},
                        onDetailDismiss = {},
                        onFavoriteToggle = {},
                        onLocateMe = { locateClicks++ },
                        onMessageDismiss = { messagesShown++ },
                        onOpenSettings = { settingsOpened++ },
                    )
                }
            }
        }

    private val base = MapUiState(initialCamera = MapViewModel.DEFAULT_CAMERA)

    @Test
    fun locateButton_invokesLocateMe() {
        show(base)
        compose.onNodeWithContentDescription("Me localiser").performClick()
        assertEquals(1, locateClicks)
    }

    @Test
    fun locateButton_whileLocating_showsProgress() {
        show(base.copy(isLocating = true))
        compose.onNodeWithContentDescription("Localisation…").assertIsDisplayed()
    }

    @Test
    fun outOfAreaMessage_isShownThenConsumed() {
        show(base.copy(message = MapMessage.OUT_OF_AREA))
        compose.onNodeWithText("Vous êtes en dehors de la zone couverte par la carte.").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 10_000) { messagesShown == 1 }
    }

    @Test
    fun permissionDenied_settingsActionOpensSettings() {
        show(base.copy(message = MapMessage.PERMISSION_DENIED))
        compose.onNodeWithText("Réglages").performClick()
        compose.waitUntil(timeoutMillis = 5_000) { settingsOpened == 1 }
    }
}
