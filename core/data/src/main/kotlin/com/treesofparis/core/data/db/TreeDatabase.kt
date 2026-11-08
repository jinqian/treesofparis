package com.treesofparis.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.treesofparis.core.data.BuildConfig

/**
 * Read-only tree data copied from the `trees.db` asset. Its version is the data version: a higher version makes
 * Room drop the old copy and copy the asset again (see [com.treesofparis.core.data.di.DataModule]).
 */
@Database(entities = [TreeEntity::class], version = BuildConfig.TREES_DB_VERSION, exportSchema = true)
abstract class TreeDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao

    internal abstract fun treeStatisticsDao(): TreeStatisticsDao

    companion object {
        const val FILE_NAME = "trees.db"
        const val ASSET_PATH = "trees.db"
    }
}
