package com.mrsep.musicrecognizer.core.metadata.di

import com.mrsep.musicrecognizer.core.metadata.artwork.ArtworkFetcher
import com.mrsep.musicrecognizer.core.metadata.artwork.ArtworkFetcherImpl
import com.mrsep.musicrecognizer.core.metadata.lyrics.LyricsFetcher
import com.mrsep.musicrecognizer.core.metadata.lyrics.LyricsFetcherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface MetadataModule {

    @Binds
    @Singleton
    fun bindLyricsFetcher(impl: LyricsFetcherImpl): LyricsFetcher

    @Binds
    @Singleton
    fun bindArtworkFetcher(impl: ArtworkFetcherImpl): ArtworkFetcher
}
