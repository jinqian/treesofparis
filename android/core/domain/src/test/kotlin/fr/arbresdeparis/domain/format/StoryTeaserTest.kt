package fr.arbresdeparis.domain.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoryTeaserTest {
    @Test
    fun skipsTheBotanicalOpening() {
        assertEquals(
            "Planté en 1873, cet arbre est le témoin des plantations du parc.",
            storyTeaser(
                "Le platane appartient à la famille des Platanacées. Planté en 1873, cet arbre est le témoin des " +
                    "plantations du parc. Il mesure 30 m.",
            ),
        )
    }

    @Test
    fun keepsTheFirstSentenceWhenItIsNotBotanical() {
        assertEquals("Cet orme a été planté en 1935.", storyTeaser("Cet orme a été planté en 1935. Il est vieux."))
    }

    @Test
    fun aStoryOfOnlyItsFamily_keepsIt() {
        assertEquals(
            "Le saule appartient à la famille des Salicacées.",
            storyTeaser("Le saule appartient à la famille des Salicacées."),
        )
    }

    @Test
    fun noStory_noTeaser() {
        assertNull(storyTeaser(null))
        assertNull(storyTeaser("  "))
    }
}
