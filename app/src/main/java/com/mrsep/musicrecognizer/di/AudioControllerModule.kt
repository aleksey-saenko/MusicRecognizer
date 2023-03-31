package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.domain.PlayerController
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderControllerOld
import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.domain.RecorderController
import com.mrsep.musicrecognizer.domain.RecognitionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AudioControllerModule {

    @Binds
    fun bindRecorderController(implementation: MediaRecorderControllerOld): RecorderController

    @Binds
    fun bindPlayerController(implementation: MediaPlayerController): PlayerController

    @Binds
    fun bindRecognizeService(implementation: AuddRecognitionService): RecognitionService

}