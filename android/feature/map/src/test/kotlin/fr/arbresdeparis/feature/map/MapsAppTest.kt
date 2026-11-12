package fr.arbresdeparis.feature.map

import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class MapsAppTest {
    @Test
    fun geoUri_pinsTheTreeWithDotDecimals() {
        assertEquals("geo:48.856600,2.352200?q=48.856600,2.352200", geoUri(GeoPoint(48.8566, 2.3522)))
    }
}
