package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.domain.PlayerController
import com.mrsep.musicrecognizer.data.recorder.MediaRecorderController
import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognizeService
import com.mrsep.musicrecognizer.domain.RecognizeService
import com.mrsep.musicrecognizer.domain.RecorderController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AudioControllerModule {

    @Binds
    fun bindRecorderController(mediaRecorderController: MediaRecorderController): RecorderController

    @Binds
    fun bindPlayerController(mediaPlayerController: MediaPlayerController): PlayerController

    @Binds
    fun bindRecognizeService(auddRecognizeService: AuddRecognizeService): RecognizeService

}