package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.RecognitionSchemeDo
import com.mrsep.musicrecognizer.data.audiorecord.SoundAmplitudeSourceDo
import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterRecorderController @Inject constructor(
    private val audioRecordingControllerDo: AudioRecordingControllerDo,
    private val soundAmplitudeSourceDo: SoundAmplitudeSourceDo,
    private val recognitionSchemeMapper: Mapper<RecognitionScheme, RecognitionSchemeDo>
) : AudioRecorderController {

    override val maxAmplitudeFlow get() = soundAmplitudeSourceDo.amplitudeFlow

    override suspend fun audioRecordingFlow(scheme: RecognitionScheme): Flow<Result<ByteArray>> {
        return audioRecordingControllerDo.audioRecordingFlow(recognitionSchemeMapper.map(scheme))
    }

}