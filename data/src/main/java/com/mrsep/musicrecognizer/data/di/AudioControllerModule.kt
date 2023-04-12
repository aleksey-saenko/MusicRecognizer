package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.data.player.PlayerDataController
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderControllerNew
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderControllerOld
import com.mrsep.musicrecognizer.data.recorder.RecorderDataController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AudioControllerModule {

    @Binds
    @Singleton
    fun bindRecorderDataController(implementation: MediaRecorderControllerOld): RecorderDataController

    @Binds
    @Singleton
    fun bindMediaPlayerController(implementation: MediaPlayerController): PlayerDataController

}