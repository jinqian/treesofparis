package com.treesofparis.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.treesofparis.core.data.di.buildUserDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Favorites must survive every app upgrade. Each exported schema of [UserDatabase] (core/data/schemas) is created with
 * a favorite, then opened by the app's own builder: a version bump without a migration fails here.
 */
@RunWith(RobolectricTestRunner::class)
class UserDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), UserDatabase::class.java)

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val opened = mutableListOf<UserDatabase>()

    @After
    fun tearDown() = opened.forEach { it.close() }

    @Test
    fun everyOlderVersion_upgradesToTheCurrentOneKeepingTheFavorites() {
        for (version in 1 until UserDatabase.VERSION) {
            val name = "user-v$version.db"
            helper.createDatabase(name, version).use {
                it.execSQL("INSERT INTO favorites (tree_id, added_at) VALUES (42, 1000), (7, 2000)")
            }

            val db = buildUserDatabase(context, name).also(opened::add)
            val favorites = runBlocking { db.favoriteDao().observeAll().first() }

            assertEquals("from version $version", listOf(7L, 42L), favorites.map { it.treeId })
        }
    }

    @Test
    fun version1To2_addsAnEmptySnapshot() {
        helper.createDatabase("user-1-2.db", 1).use {
            it.execSQL("INSERT INTO favorites (tree_id, added_at) VALUES (42, 1000)")
        }
        helper.runMigrationsAndValidate("user-1-2.db", 2, true).use { db ->
            db.query("SELECT snapshot_common_name FROM favorites WHERE tree_id = 42").use { cursor ->
                cursor.moveToFirst()
                assertNull(cursor.getString(0))
            }
        }
    }

    @Test
    fun appDatabase_keepsNoChangesAsideInAWalFile() {
        // Room's default would be WAL here, whose last changes Android's backup of user.db misses.
        val default = Room.databaseBuilder(context, UserDatabase::class.java, "default.db").build().also(opened::add)
        assertTrue(default.openHelper.writableDatabase.isWriteAheadLoggingEnabled)

        val db = buildUserDatabase(context, "journal.db").also(opened::add)
        assertFalse(db.openHelper.writableDatabase.isWriteAheadLoggingEnabled)
    }
}
