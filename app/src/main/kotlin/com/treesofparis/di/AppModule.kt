package com.treesofparis.di

import com.treesofparis.BuildConfig
import com.treesofparis.core.map.MapProvider
import com.treesofparis.core.map.osmdroid.OsmdroidMapProvider
import com.treesofparis.domain.model.AppInfo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId

/** App-level wiring: the one place that picks the map implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindMapProvider(impl: OsmdroidMapProvider): MapProvider

    companion object {
        @Provides
        fun provideAppInfo(): AppInfo = AppInfo(versionName = BuildConfig.VERSION_NAME)

        /** « This month » is the month in Paris, wherever the phone is set. */
        @Provides
        fun provideClock(): Clock = Clock.system(ZoneId.of("Europe/Paris"))
    }
}
