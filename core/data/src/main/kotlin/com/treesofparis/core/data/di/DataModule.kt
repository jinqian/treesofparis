package com.treesofparis.core.data.di

import android.content.Context
import androidx.room.Room
import com.treesofparis.core.data.db.TreeDao
import com.treesofparis.core.data.db.TreeDatabase
import com.treesofparis.core.data.db.UserDatabase
import com.treesofparis.core.data.location.LocationRepositoryImpl
import com.treesofparis.core.data.repository.FavoritesRepositoryImpl
import com.treesofparis.core.data.repository.TreeRepositoryImpl
import com.treesofparis.domain.repository.FavoritesRepository
import com.treesofparis.domain.repository.LocationRepository
import com.treesofparis.domain.repository.TreeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindTreeRepository(impl: TreeRepositoryImpl): TreeRepository

    companion object {
        @Provides
        @Singleton
        fun provideTreeDatabase(
            @ApplicationContext context: Context,
        ): TreeDatabase = buildTreeDatabase(context)

        @Provides
        fun provideTreeDao(db: TreeDatabase): TreeDao = db.treeDao()

        @Provides
        @Singleton
        fun provideUserDatabase(
            @ApplicationContext context: Context,
        ): UserDatabase = Room.databaseBuilder(context, UserDatabase::class.java, UserDatabase.FILE_NAME).build()

        @Provides
        @Singleton
        fun provideFavoritesRepository(db: UserDatabase): FavoritesRepository =
            FavoritesRepositoryImpl(db.favoriteDao(), clock = System::currentTimeMillis)

        @Provides
        @Singleton
        fun provideLocationRepository(
            @ApplicationContext context: Context,
        ): LocationRepository = LocationRepositoryImpl(context, clock = System::currentTimeMillis)
    }
}

/**
 * The tree data is replaceable: when [TreeDatabase]'s version (the data version) goes up, Room drops the old copy
 * and copies the new asset. Favorites are in [UserDatabase] and are not affected.
 */
internal fun buildTreeDatabase(
    context: Context,
    name: String = TreeDatabase.FILE_NAME,
): TreeDatabase =
    Room
        .databaseBuilder(context, TreeDatabase::class.java, name)
        .createFromAsset(TreeDatabase.ASSET_PATH)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
