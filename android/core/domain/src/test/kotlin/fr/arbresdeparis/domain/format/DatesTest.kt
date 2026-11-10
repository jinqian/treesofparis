package fr.arbresdeparis.domain.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DatesTest {
    @Test
    fun formatParisDate_isTheDayInParis() {
        assertEquals("6 novembre 2026", formatParisDate(Instant.parse("2026-11-06T16:30:03Z")))
        // 23:30 UTC on 5 November is already 6 November in Paris.
        assertEquals("6 novembre 2026", formatParisDate(Instant.parse("2026-11-05T23:30:00Z")))
    }
}
