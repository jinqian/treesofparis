package fr.arbresdeparis.core.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.arbresdeparis.core.data.BuildConfig
import fr.arbresdeparis.core.data.di.buildTreeDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Opens the real `trees.db` asset produced by data/scripts/update_trees.py through Room. */
@RunWith(RobolectricTestRunner::class)
class PrepackagedDatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val opened = mutableListOf<androidx.room.RoomDatabase>()

    @After
    fun tearDown() = opened.forEach { it.close() }

    @Test
    fun prepackagedAsset_opensWithoutSchemaMismatch() =
        runTest {
            val db = buildTreeDatabase(context, name = "fresh.db").also(opened::add)
            val tree = db.treeDao().byId(KNOWN_TREE_ID)
            assertNotNull(tree)
            assertEquals("PARIS 19E ARRDT", tree!!.arrondissement)
        }

    @Test
    fun prepackagedAsset_containsTreesManagedOutsideParis() =
        runTest {
            val db = buildTreeDatabase(context, name = "outside.db").also(opened::add)
            // Pantin cemetery (Seine-Saint-Denis)
            val pantin =
                db.treeDao().inBounds(
                    south = 48.900,
                    west = 2.400,
                    north = 48.912,
                    east = 2.424,
                    remarkableOnly = false,
                    genus = null,
                    limit = 10,
                )
            assertEquals(true, pantin.isNotEmpty())
            assertEquals(true, pantin.all { it.arrondissement == "SEINE-SAINT-DENIS" })
        }

    @Test
    fun prepackagedAsset_has181RemarkableTrees() =
        runTest {
            val db = buildTreeDatabase(context, name = "remarkable.db").also(opened::add)
            val c = fr.arbresdeparis.domain.model.GeoBounds.COVERAGE
            val remarkable =
                db.treeDao().inBounds(
                    c.south,
                    c.west,
                    c.north,
                    c.east,
                    remarkableOnly = true,
                    genus = null,
                    limit = 1_000,
                )
            assertEquals(181, remarkable.size)
            assertEquals(true, remarkable.all { it.remarkable })
        }

    @Test
    fun newAssetVersion_replacesOldData_keepsFavorites() =
        runTest {
            val name = "upgrade.db"
            writeOldCopy(name, version = BuildConfig.TREES_DB_VERSION - 1)
            val userDb = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build().also(opened::add)
            userDb.favoriteDao().insert(FavoriteEntity(treeId = KNOWN_TREE_ID, addedAt = 1))

            val db = buildTreeDatabase(context, name = name).also(opened::add)

            assertNull("old data must be replaced", db.treeDao().byId(OLD_ONLY_TREE_ID))
            assertNotNull(db.treeDao().byId(KNOWN_TREE_ID))
            assertEquals(true, userDb.favoriteDao().isFavorite(KNOWN_TREE_ID))
        }

    /** Simulates the copy an older app version left on the device. */
    private fun writeOldCopy(
        name: String,
        version: Int,
    ) {
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE trees (id INTEGER PRIMARY KEY NOT NULL, common_name TEXT NOT NULL)")
            db.execSQL("INSERT INTO trees VALUES ($OLD_ONLY_TREE_ID, 'Ancien')")
            db.version = version
        }
    }

    private companion object {
        const val KNOWN_TREE_ID = 243_481L // Platane, quai de la Seine
        const val OLD_ONLY_TREE_ID = 999_999_999L
    }
}
