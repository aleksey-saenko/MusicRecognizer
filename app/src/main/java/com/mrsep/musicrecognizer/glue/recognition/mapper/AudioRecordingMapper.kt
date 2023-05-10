package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingDataStrategy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import javax.inject.Inject

class AudioRecordingMapper @Inject constructor() :
    Mapper<AudioRecordingStrategy, AudioRecordingDataStrategy> {


    override fun map(input: AudioRecordingStrategy): AudioRecordingDataStrategy {
        return AudioRecordingDataStrategy(
            steps = input.steps.map { step -> AudioRecordingDataStrategy.Step(step.timestamp, step.splitter) },
            sendTotalAtEnd = input.sendTotalAtEnd
        )
    }

}