package com.treesofparis.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.treesofparis.core.data.db.TaxonLinkEntity
import com.treesofparis.core.data.db.TreeDatabase
import com.treesofparis.core.data.db.insert
import com.treesofparis.core.data.db.insertLinks
import com.treesofparis.core.data.db.treeEntity
import com.treesofparis.domain.model.WikipediaArticle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TreeRepositoryWikipediaTest {
    private val db =
        Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), TreeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private val repository = TreeRepositoryImpl(db.treeDao())

    @Before
    fun setUp() {
        db.insert(
            treeEntity(1, genus = "Platanus", species = "x hispanica"),
            treeEntity(2, genus = "Ulmus", species = null),
            treeEntity(3, genus = "Zelkova", species = "serrata"),
        )
        db.insertLinks(
            TaxonLinkEntity("Platanus", "x hispanica", "Platane commun", genusOnly = false),
            TaxonLinkEntity("Ulmus", "", "Orme", genusOnly = true),
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun getTree_carriesItsSpeciesArticle() =
        runTest {
            assertEquals(WikipediaArticle("Platane commun", isGenusArticle = false), repository.getTree(1)?.wikipedia)
        }

    @Test
    fun getTree_withoutSpecies_findsTheGenusArticle() =
        runTest {
            assertEquals(WikipediaArticle("Orme", isGenusArticle = true), repository.getTree(2)?.wikipedia)
        }

    @Test
    fun getTree_withoutAnyArticle_hasNone() =
        runTest {
            assertNull(repository.getTree(3)?.wikipedia)
        }
}
