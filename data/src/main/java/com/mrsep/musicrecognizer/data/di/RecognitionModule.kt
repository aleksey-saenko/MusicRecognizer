package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.ConfigValidatorDo
import com.mrsep.musicrecognizer.data.remote.ConfigValidatorImpl
import com.mrsep.musicrecognizer.data.remote.RecognitionServiceFactoryDo
import com.mrsep.musicrecognizer.data.remote.RecognitionServiceFactoryImpl
import com.mrsep.musicrecognizer.data.remote.artwork.ArtworkFetcher
import com.mrsep.musicrecognizer.data.remote.artwork.ArtworkFetcherImpl
import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddWebSocketSession
import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddWebSocketSessionImpl
import com.mrsep.musicrecognizer.data.remote.enhancer.TrackMetadataEnhancerDo
import com.mrsep.musicrecognizer.data.remote.enhancer.odesli.OdesliMetadataEnhancer
import com.mrsep.musicrecognizer.data.remote.lyrics.LyricsFetcher
import com.mrsep.musicrecognizer.data.remote.lyrics.LyricsFetcherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface RecognitionModule {

    @Binds
    @Singleton
    fun bindAuddWebSocketSession(impl: AuddWebSocketSessionImpl): AuddWebSocketSession

    @Binds
    @Singleton
    fun bindServiceFactory(impl: RecognitionServiceFactoryImpl): RecognitionServiceFactoryDo

    @Binds
    @Singleton
    fun bindConfigValidator(impl: ConfigValidatorImpl): ConfigValidatorDo

    @Binds
    @Singleton
    fun bindLyricsFetcher(impl: LyricsFetcherImpl): LyricsFetcher

    @Binds
    @Singleton
    fun bindArtworkFetcher(impl: ArtworkFetcherImpl): ArtworkFetcher

    @Binds
    @Singleton
    fun bindTrackMetadataEnhancer(impl: OdesliMetadataEnhancer): TrackMetadataEnhancerDo
}
