package fr.arbresdeparis.core.data.mapper

import fr.arbresdeparis.core.data.db.treeEntity
import fr.arbresdeparis.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreeMapperTest {
    @Test
    fun mapsNullableFields() {
        val tree = treeEntity(id = 7, lat = 48.1, lon = 2.2).copy(species = null, heightM = null).toDomain()
        assertNull(tree.species)
        assertNull(tree.heightM)
        assertEquals(GeoPoint(48.1, 2.2), tree.location)
    }

    @Test
    fun mapsAllFields() {
        val entity =
            treeEntity(
                id = 3,
            ).copy(species = "x-acerifolia", heightM = 20, circumferenceCm = 180, remarkable = true)
        val tree = entity.toDomain()
        assertEquals(3L, tree.id)
        assertEquals("x-acerifolia", tree.species)
        assertEquals(20, tree.heightM)
        assertEquals(180, tree.circumferenceCm)
        assertEquals(true, tree.remarkable)
        assertEquals("PARIS 5E ARRDT", tree.arrondissement)
    }
}
