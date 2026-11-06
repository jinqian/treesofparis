package com.treesofparis.core.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlaceholderModule {
    @Provides
    fun provideName(): String = NAME

    const val NAME = "data"
}
