package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.audioplayer.MediaPlayerController
import com.mrsep.musicrecognizer.data.audioplayer.PlayerControllerDo
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
    fun bindMediaPlayerController(impl: MediaPlayerController): PlayerControllerDo
}
