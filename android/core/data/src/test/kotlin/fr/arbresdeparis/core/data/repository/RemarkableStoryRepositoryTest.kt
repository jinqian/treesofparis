package fr.arbresdeparis.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.core.data.db.RemarkableTreeEntity
import fr.arbresdeparis.core.data.db.TreeDatabase
import fr.arbresdeparis.domain.model.RemarkableStory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemarkableStoryRepositoryTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), TreeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private val repository = RemarkableStoryRepositoryImpl(db.remarkableStoryDao())

    private val orme =
        RemarkableTreeEntity(
            treeId = 267663,
            site = "Place Saint-Gervais",
            qualification = "Historique",
            summary = "Cet arbre est classé remarquable pour son caractère historique.",
            story = "Cet orme a été planté en 1935 sur la place Saint-Gervais.",
            plantingYear = null,
            label = "A.R.B.R.E.S en 2015",
            councilDecision = "2015 DEVE 152",
            signUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/267663.pdf",
            photoUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/Photos2023/1/267663.jpg",
            photoCredit = "Jean-Pierre Viguié / Ville de Paris",
        )

    @After
    fun tearDown() = db.close()

    @Test
    fun story_isTheTreesRow() =
        runTest {
            db.remarkableStoryDao().insert(orme)
            assertEquals(
                RemarkableStory(
                    treeId = 267663,
                    site = "Place Saint-Gervais",
                    qualification = "Historique",
                    summary = "Cet arbre est classé remarquable pour son caractère historique.",
                    story = "Cet orme a été planté en 1935 sur la place Saint-Gervais.",
                    plantingYear = null,
                    label = "A.R.B.R.E.S en 2015",
                    councilDecision = "2015 DEVE 152",
                    signUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/267663.pdf",
                    photoUrl = "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/Photos2023/1/267663.jpg",
                    photoCredit = "Jean-Pierre Viguié / Ville de Paris",
                ),
                repository.getStory(267663),
            )
        }

    @Test
    fun treeWithoutStory_hasNone() = runTest { assertNull(repository.getStory(1)) }

    @Test
    fun stories_areAllRows() =
        runTest {
            db.remarkableStoryDao().insert(orme)
            assertEquals(listOf(267663L), repository.getStories().map { it.treeId })
        }
}
