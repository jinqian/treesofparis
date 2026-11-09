package fr.arbresdeparis.feature.map

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.feature.map.mapper.toDetailUi
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class TreeReportTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val plane = tree(125046, lat = 48.852782626, lon = 2.351016048).toDetailUi(isFavorite = false)
    private val contact = ReportContactUi("arbresdeparis@gmail.com", "0.1.0", "6 novembre 2026")

    @Test
    fun body_leavesRoomForTheProblem_thenSignsWithEverythingToFindTheTree() {
        assertEquals(
            """
            Bonjour,

            Ce qui ne va pas (arbre abattu ou remplacé, mal placé, mauvaise essence, mesures fausses…) :


            —
            Arbre n° 125046 · Platane (Platanus x hispanica)
            Quai de la Seine · Paris 19e
            48.85278, 2.35102
            Arbres de Paris 0.1.0 · données du 6 novembre 2026 · Android 16
            """.trimIndent(),
            app.resources.treeReportBody(plane, contact, androidVersion = "16"),
        )
    }

    @Test
    fun body_withoutLatinNameNorDataDate_leavesThemOut() {
        val unknown = plane.copy(scientificName = null)
        val body = app.resources.treeReportBody(unknown, contact.copy(dataDate = null), androidVersion = "16")
        assertEquals(
            listOf("Arbre n° 125046 · Platane", "Arbres de Paris 0.1.0 · Android 16"),
            body.lines().filter { it.startsWith("Arbre") },
        )
    }

    @Test
    fun report_opensTheEmailAppWithTheTreeInTheSubject() {
        app.reportTreeError(plane, contact)

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertArrayEquals(arrayOf("arbresdeparis@gmail.com"), intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertEquals("Erreur sur l’arbre n° 125046 — Platane", intent.getStringExtra(Intent.EXTRA_SUBJECT))
    }
}
