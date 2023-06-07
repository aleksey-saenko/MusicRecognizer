package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingStrategyDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import javax.inject.Inject

class AudioRecordingMapper @Inject constructor() :
    Mapper<AudioRecordingStrategy, AudioRecordingStrategyDo> {


    override fun map(input: AudioRecordingStrategy): AudioRecordingStrategyDo {
        return AudioRecordingStrategyDo(
            steps = input.steps.map { step -> AudioRecordingStrategyDo.Step(step.timestamp, step.splitter) },
            sendTotalAtEnd = input.sendTotalAtEnd
        )
    }

}