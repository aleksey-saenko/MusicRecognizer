package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.audioplayer.MediaPlayerController
import com.mrsep.musicrecognizer.data.audioplayer.PlayerControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceImpl
import com.mrsep.musicrecognizer.data.audiorecord.encoder.AacEncoderController
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSourceImpl
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

    @Binds
    @Singleton
    fun bindSoundSource(impl: SoundSourceImpl): SoundSource

    @Binds
    @Singleton
    fun bindSoundAmplitudeSource(impl: SoundAmplitudeSourceImpl): SoundAmplitudeSourceDo

    @Binds
    @Singleton
    fun bindAudioRecordingController(impl: AacEncoderController): AudioRecordingControllerDo
}
