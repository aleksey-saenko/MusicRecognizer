package com.mrsep.musicrecognizer.core.recognition.di

import com.mrsep.musicrecognizer.core.domain.recognition.ConfigValidator
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionServiceFactory
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancer
import com.mrsep.musicrecognizer.core.recognition.ConfigValidatorImpl
import com.mrsep.musicrecognizer.core.recognition.RecognitionServiceFactoryImpl
import com.mrsep.musicrecognizer.core.recognition.artwork.ArtworkFetcher
import com.mrsep.musicrecognizer.core.recognition.artwork.ArtworkFetcherImpl
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketSession
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketSessionImpl
import com.mrsep.musicrecognizer.core.recognition.enhancer.odesli.OdesliMetadataEnhancer
import com.mrsep.musicrecognizer.core.recognition.lyrics.LyricsFetcher
import com.mrsep.musicrecognizer.core.recognition.lyrics.LyricsFetcherImpl
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
    fun bindRecognitionServiceFactory(impl: RecognitionServiceFactoryImpl): RecognitionServiceFactory

    @Binds
    @Singleton
    fun bindConfigValidator(impl: ConfigValidatorImpl): ConfigValidator

    @Binds
    @Singleton
    fun bindTrackMetadataEnhancer(impl: OdesliMetadataEnhancer): TrackMetadataEnhancer


    @Binds
    @Singleton
    fun bindArtworkFetcher(impl: ArtworkFetcherImpl): ArtworkFetcher

    @Binds
    @Singleton
    fun bindLyricsFetcher(impl: LyricsFetcherImpl): LyricsFetcher

    @Binds
    @Singleton
    fun bindWebSocketSession(impl: WebSocketSessionImpl): WebSocketSession
}
