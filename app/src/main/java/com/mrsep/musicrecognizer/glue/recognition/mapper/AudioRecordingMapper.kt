package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.RecognitionSchemeDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme
import javax.inject.Inject

class AudioRecordingMapper @Inject constructor() :
    Mapper<RecognitionScheme, RecognitionSchemeDo> {

    override fun map(input: RecognitionScheme): RecognitionSchemeDo {
        return RecognitionSchemeDo(
            steps = input.steps.map { step -> RecognitionSchemeDo.Step(step.timestamp, step.splitter) },
            sendTotalAtEnd = input.sendTotalAtEnd
        )
    }
}
