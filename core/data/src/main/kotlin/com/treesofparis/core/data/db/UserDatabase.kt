package com.treesofparis.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/** User-owned data, kept apart from the replaceable tree data. Needs real migrations when its schema changes. */
@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = true)
abstract class UserDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val FILE_NAME = "user.db"
    }
}
