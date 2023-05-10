package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingController
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingDataStrategy
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSource
import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterRecorderController @Inject constructor(
    private val audioRecordingController: AudioRecordingController,
    private val soundAmplitudeSource: SoundAmplitudeSource,
    private val recordingStrategyMapper: Mapper<AudioRecordingStrategy, AudioRecordingDataStrategy>
) : AudioRecorderController {

    override val maxAmplitudeFlow get() = soundAmplitudeSource.amplitudeFlow

    override suspend fun audioRecordingFlow(strategy: AudioRecordingStrategy): Flow<Result<ByteArray>> {
        return audioRecordingController.audioRecordingFlow(recordingStrategyMapper.map(strategy))
    }


}