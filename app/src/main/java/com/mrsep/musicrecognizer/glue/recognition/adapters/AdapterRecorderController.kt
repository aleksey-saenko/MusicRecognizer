package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.recorder.RecordDataResult
import com.mrsep.musicrecognizer.data.recorder.RecorderDataController
import com.mrsep.musicrecognizer.feature.recognition.domain.RecordResult
import com.mrsep.musicrecognizer.feature.recognition.domain.RecorderController
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import javax.inject.Inject

class AdapterRecorderController @Inject constructor(
    private val recorderDataController: RecorderDataController,
    private val recordResultToDomainMapper: Mapper<RecordDataResult, RecordResult>
) : RecorderController {

    override val maxAmplitudeFlow: Flow<Float>
        get() = recorderDataController.maxAmplitudeFlow

    override suspend fun recordAudioToFile(duration: Duration): RecordResult {
        return recordResultToDomainMapper.map(
            recorderDataController.recordAudioToFile(duration)
        )
    }

}