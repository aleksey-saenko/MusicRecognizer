package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingStrategyDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterRecorderController @Inject constructor(
    private val audioRecordingControllerDo: AudioRecordingControllerDo,
    private val soundAmplitudeSourceDo: SoundAmplitudeSourceDo,
    private val recordingStrategyMapper: Mapper<AudioRecordingStrategy, AudioRecordingStrategyDo>
) : AudioRecorderController {

    override val maxAmplitudeFlow get() = soundAmplitudeSourceDo.amplitudeFlow

    override suspend fun audioRecordingFlow(strategy: AudioRecordingStrategy): Flow<Result<ByteArray>> {
        return audioRecordingControllerDo.audioRecordingFlow(recordingStrategyMapper.map(strategy))
    }


}