package com.treesofparis.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * User-owned data, kept apart from the replaceable tree data. Every schema change needs a migration (never a
 * destructive fallback): `UserDatabaseMigrationTest` upgrades each exported schema to [VERSION] and fails otherwise.
 *
 * - 2: favorites remember what their tree is (`snapshot_*` columns).
 */
@Database(
    entities = [FavoriteEntity::class],
    version = UserDatabase.VERSION,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val VERSION = 2
        const val FILE_NAME = "user.db"
    }
}
