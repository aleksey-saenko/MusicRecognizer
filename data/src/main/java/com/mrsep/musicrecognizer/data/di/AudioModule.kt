package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceImpl
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoderController
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSourceImpl
import com.mrsep.musicrecognizer.data.player.MediaPlayerController
import com.mrsep.musicrecognizer.data.player.PlayerControllerDo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AudioModule {

    @Binds
    @Singleton
    fun bindMediaPlayerController(implementation: MediaPlayerController): PlayerControllerDo

    @Binds
    @Singleton
    fun bindSoundSource(implementation: SoundSourceImpl): SoundSource

    @Binds
    @Singleton
    fun bindSoundAmplitudeSource(implementation: SoundAmplitudeSourceImpl): SoundAmplitudeSourceDo

    @Binds
    @Singleton
    fun bindAudioRecordingController(implementation: AacEncoderController): AudioRecordingControllerDo

}