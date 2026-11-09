package fr.arbresdeparis.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * User-owned data, kept apart from the replaceable tree data. Every schema change after the first release needs a
 * version bump and a migration (never a destructive fallback): `UserDatabaseMigrationTest` opens each exported schema
 * with the app's builder and fails otherwise.
 */
@Database(
    entities = [FavoriteEntity::class],
    version = UserDatabase.VERSION,
    exportSchema = true,
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val VERSION = 1
        const val FILE_NAME = "user.db"
    }
}
