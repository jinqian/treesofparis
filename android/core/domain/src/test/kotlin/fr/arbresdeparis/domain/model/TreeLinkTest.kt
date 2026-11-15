package fr.arbresdeparis.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreeLinkTest {
    @Test
    fun url_hasTheIdAndTheLocationToFiveDecimals() {
        assertEquals(
            "https://arbresdeparis.fr/arbre/?id=2002389&ll=48.86125,2.25321",
            TreeLink(2002389, GeoPoint(48.861249, 2.2532149)).url,
        )
    }

    @Test
    fun url_usesADotWhateverTheDefaultLocale() {
        val default = java.util.Locale.getDefault()
        java.util.Locale.setDefault(java.util.Locale.FRANCE)
        try {
            assertEquals(
                "https://arbresdeparis.fr/arbre/?id=1&ll=48.80000,2.30000",
                TreeLink(1, GeoPoint(48.8, 2.3)).url,
            )
        } finally {
            java.util.Locale.setDefault(default)
        }
    }

    @Test
    fun parse_readsBackWhatUrlWrites() {
        val link = TreeLink(192_078_000_001, GeoPoint(48.825, 2.19))
        assertEquals(link, TreeLink.parse(link.url))
    }

    @Test
    fun parse_acceptsWwwAndAPathWithoutItsSlash() {
        assertEquals(
            TreeLink(42, GeoPoint(48.85, 2.35)),
            TreeLink.parse("https://www.arbresdeparis.fr/arbre?ll=48.85,2.35&id=42"),
        )
    }

    @Test
    fun parse_withoutCoordinates_keepsTheId() {
        assertEquals(TreeLink(42, null), TreeLink.parse("https://arbresdeparis.fr/arbre/?id=42"))
        assertEquals(TreeLink(42, null), TreeLink.parse("https://arbresdeparis.fr/arbre/?id=42&ll=north,south"))
    }

    @Test
    fun parse_rejectsAnythingElse() {
        listOf(
            "https://arbresdeparis.fr/arbre/",
            "https://arbresdeparis.fr/arbre/?id=abc",
            "https://arbresdeparis.fr/arbre/?id=-3",
            "https://arbresdeparis.fr/maps/?arbre=42",
            "https://example.com/arbre/?id=42",
            "http://arbresdeparis.fr.example.com/arbre/?id=42",
            "not a url",
        ).forEach { assertNull(it, TreeLink.parse(it)) }
    }

    @Test
    fun parse_ignoresCoordinatesOutOfRange() {
        assertEquals(TreeLink(42, null), TreeLink.parse("https://arbresdeparis.fr/arbre/?id=42&ll=148.8,2.3"))
    }
}
