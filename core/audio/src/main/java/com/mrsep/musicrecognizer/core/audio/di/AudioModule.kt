package com.mrsep.musicrecognizer.core.audio.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.mrsep.musicrecognizer.core.audio.audioplayer.ExoPlayerController
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
    @OptIn(UnstableApi::class)
    fun bindPlayerController(impl: ExoPlayerController): PlayerController
}