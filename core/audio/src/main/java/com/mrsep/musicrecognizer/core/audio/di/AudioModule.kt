package com.mrsep.musicrecognizer.core.audio.di

import com.mrsep.musicrecognizer.core.audio.audioplayer.MediaPlayerController
import com.mrsep.musicrecognizer.core.audio.audioplayer.PlayerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface AudioModule {

    @Binds
    @Singleton
    fun bindPlayerController(impl: MediaPlayerController): PlayerController
}